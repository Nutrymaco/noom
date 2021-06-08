package com.nutrymaco.orm.query.select;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.metadata.token.TokenRange;
import com.datastax.oss.driver.internal.core.metadata.token.Murmur3Token;
import com.datastax.oss.driver.internal.core.metadata.token.Murmur3TokenRange;
import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.query.Database;
import com.nutrymaco.orm.query.condition.Condition;
import com.nutrymaco.orm.query.condition.ConditionToPredicateMapper;
import com.nutrymaco.orm.schema.db.Column;
import com.nutrymaco.orm.schema.db.Table;
import com.nutrymaco.orm.schema.lang.FieldRef;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

// todo - add syncing callback for syncing entity ?
public class TableTraveler<R> {

    private static final String KEYSPACE = ConfigurationOwner.getConfiguration().keyspace();

    private static final CqlSession session = ConfigurationOwner.getConfiguration().session();
    private static final Database database = ConfigurationOwner.getConfiguration().database();
    private static final Logger logger = Logger.getLogger(TableTraveler.class.getSimpleName());

    private final long limit;
    private final Table table;
    private final Collection<Condition> conditions;
    private final Class<R> resultClass;

    public TableTraveler(Table table, Collection<Condition> conditions, Class<R> resultClass, long limit) {
        this.limit = limit;
        this.table = table;
        this.conditions = conditions;
        this.resultClass = resultClass;
    }

    public TableTraveler(Table table, Collection<Condition> conditions, Class<R> resultClass) {
        this.limit = Long.MAX_VALUE;
        this.table = table;
        this.conditions = conditions;
        this.resultClass = resultClass;
    }

    // todo - optimize:
    // 1) add filtering on db side
    // 2) add opportunity to choose fields
    public void traverseTable(Consumer<R> callback) {
        var tokenRanges = session.getMetadata().getTokenMap().orElseThrow().getTokenRanges();
        logger.info("start traverse table : %s".formatted(table.name()));
        long curLimit = limit;
        long allResultCount = 0;

        var conditionPartitioner = new ConditionPartitioner(table, conditions);
        var dbSideConditions = conditionPartitioner.getDbSideConditions();
        var inMemoryFilter = InMemoryFilter.getInstance(
                conditionPartitioner.getInMemoryConditions());
        var dbSideConditionsString = dbSideConditions.stream()
                .map(Condition::getCql)
                .collect(joining(" and "));

        for (TokenRange tokenRange : tokenRanges) {
            long start, end;
            if (tokenRange instanceof Murmur3TokenRange murmurTokenRange) {
                start = ((Murmur3Token) murmurTokenRange.getStart()).getValue();
                end = ((Murmur3Token) murmurTokenRange.getEnd()).getValue();
            } else {
                logger.info("not expected token range");
                throw new IllegalStateException("not expected token range");
            }


            var token = table.primaryKey().partitionColumns().stream()
                    .map(Column::name)
                    .collect(joining(", ", "token(", ")"));

            var rows = database.execute("""
                    SELECT * FROM %s.%s WHERE %s > %s and %s < %s %s ALLOW FILTERING
                    """.formatted(KEYSPACE, table.name(), token, start, token, end,
                        (dbSideConditions.isEmpty() ? "" : "and ") + dbSideConditionsString
                    ));

            long resultCount = rows.stream()
                    .map(row -> RowToObjectMapper.getInstance(row, resultClass).mapToObject())
                    .filter(Objects::nonNull)
                    .filter(inMemoryFilter)
                    .peek(callback)
                    .limit(curLimit)
                    .count();
            allResultCount += resultCount;
            curLimit -= resultCount;
            if (curLimit <= 0) {
                logger.info("end traverse table (by limit) : %s, result count : %s"
                        .formatted(table.name(), allResultCount));
                return;
            }
        }
        logger.info("end traverse table : %s, result count : %s".formatted(table.name(), allResultCount));
    }

}
