package com.nutrymaco.orm.query.select;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.query.condition.Condition;
import com.nutrymaco.orm.schema.Schema;
import com.nutrymaco.orm.schema.lang.Entity;

import java.util.List;
import java.util.stream.Collectors;


/**
 * test - {@link com.nutrymaco.orm.tests.query.SelectQueryBuilderTest}
 */
public class SelectQueryBuilder {
    private final static String KEYSPACE = ConfigurationOwner.getConfiguration().keyspace();
    private final static Schema schema = Schema.getInstance();

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

        var query =
                String.format("SELECT * FROM %s.%s WHERE %s", KEYSPACE,
                        table.name(),
                        condition.stream().map(Condition::getCql).collect(Collectors.joining(" and "))
                );

        return query;
    }

    private static String getColumnName(String name) {
        return name.replace(".", "_").toLowerCase();
    }

}
