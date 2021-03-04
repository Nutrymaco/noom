package com.nutrymaco.orm.query.select;

import com.nutrymaco.orm.query.condition.EqualsCondition;

public class WhereBuilder {
    private final SelectQueryContext context;

    public WhereBuilder(SelectQueryContext context) {
        this.context = context;
    }

    public FetchBuilder where(EqualsCondition condition) {
        context.setCondition(condition);
        return new FetchBuilder(context);
    }
}
