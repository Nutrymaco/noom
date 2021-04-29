package com.nutrymaco.orm.query.select;

import com.nutrymaco.orm.query.condition.Condition;

import java.util.Arrays;

public class WhereBuilder {
    private final SelectQueryContext context;

    public WhereBuilder(SelectQueryContext context) {
        this.context = context;
    }

    public SelectResultBuilder where(Condition ... conditions) {
        context.setConditions(Arrays.asList(conditions));
        return new SelectResultBuilder(context);
    }
}
