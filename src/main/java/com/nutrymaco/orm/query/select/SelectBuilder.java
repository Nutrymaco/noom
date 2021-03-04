package com.nutrymaco.orm.query.select;

import com.nutrymaco.orm.schema.lang.Entity;

public class SelectBuilder {
    private final SelectQueryContext context;

    public SelectBuilder(SelectQueryContext context) {
        this.context = context;
    }

    public WhereBuilder select(Entity entity) {
        context.setEntity(entity);
        return new WhereBuilder(context);
    }

}
