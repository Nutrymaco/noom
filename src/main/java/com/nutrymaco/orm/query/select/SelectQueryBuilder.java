package com.nutrymaco.orm.query.select;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.query.condition.Condition;
import com.nutrymaco.orm.schema.Schema;
import com.nutrymaco.orm.schema.db.Table;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class SelectQueryBuilder {
    private final static String KEYSPACE = ConfigurationOwner.getConfiguration().keyspace();

    private final Table table;
    private final List<Condition> conditions;

    private SelectQueryBuilder(Table table, List<Condition> conditions) {
        this.table = table;
        this.conditions = conditions;
    }

    //todo - закэшировать запрос ?, но нельязя по полному запросу - из условий надо убрать конкретные значения
    static SelectQueryBuilder from(Table table, List<Condition> conditions) {
        return new SelectQueryBuilder(
                table, conditions);
    }

    static SelectQueryBuilder from(SelectQueryContext context) {
        return new SelectQueryBuilder(Schema.getInstance().getTableForQueryContext(context), context.getConditions());
    }

    String getQuery() {
        return "SELECT * FROM %s.%s WHERE %s ALLOW FILTERING"
                .formatted(KEYSPACE, table.name(), getStringCondition(conditions));
    }

    private static String getStringCondition(List<Condition> conditions) {
        return conditions.stream()
                .map(Condition::getCql)
                .collect(Collectors.joining(" and "));
    }
}
