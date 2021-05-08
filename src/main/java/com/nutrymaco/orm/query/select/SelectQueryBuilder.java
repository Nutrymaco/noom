package com.nutrymaco.orm.query.select;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.migration.TableSyncManager;
import com.nutrymaco.orm.query.Database;
import com.nutrymaco.orm.query.Query;
import com.nutrymaco.orm.query.condition.Condition;
import com.nutrymaco.orm.query.insert.InsertQueryBuilder;
import com.nutrymaco.orm.schema.Schema;
import com.nutrymaco.orm.schema.lang.Entity;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * test - {@link com.nutrymaco.orm.tests.query.SelectQueryBuilderTest}
 */
public class SelectQueryBuilder {
    private final static String KEYSPACE = ConfigurationOwner.getConfiguration().keyspace();
    private final static boolean TRY_TO_SYNC_TABLES = ConfigurationOwner.getConfiguration().accessToDB();
    private final static Database database = ConfigurationOwner.getConfiguration().database();
    private final static Logger logger = Logger.getLogger(SelectQueryBuilder.class.getSimpleName());
    private final static Schema schema = Schema.getInstance();

    private final TableSyncManager tableSyncManager = TableSyncManager.getInstance();
    private final Entity entity;
    private final List<Condition> condition;

    private SelectQueryBuilder(Entity entity, List<Condition> condition) {
        this.entity = entity;
        this.condition = condition;
    }

    //todo - закэшировать запрос ?, но нельязя по полному запросу - из условий надо убрать конкретные значения
    static <E> SelectQueryBuilder from(SelectQueryContext queryContext) {
        return new SelectQueryBuilder(
                queryContext.getEntity(),
                queryContext.getConditions()
        );
    }

    String getQuery() {
        // fixme глянуть зачем я создаю новый объект
        var table = schema.getTableForQueryContext(
                new SelectQueryContext(entity, condition)
        );

        if (!TRY_TO_SYNC_TABLES || tableSyncManager.isSync(table)) {
            logger.info("select from table : %s".formatted(table.name()));
            return "SELECT * FROM %s.%s WHERE %s"
                    .formatted(KEYSPACE, table.name(), getStringCondition(condition));
        }

        var nearestTable = tableSyncManager.getNearestTable(table);
        logger.info("table : %s not sync so query by nearest table : %s".formatted(table.name(), nearestTable.name()));
        return "SELECT * FROM %s.%s WHERE %s ALLOW FILTERING"
                .formatted(KEYSPACE, nearestTable.name(), getStringCondition(condition));
    }

    private static String getStringCondition(List<Condition> conditions) {
        return conditions.stream().map(Condition::getCql).collect(Collectors.joining(" and "));
    }
}
