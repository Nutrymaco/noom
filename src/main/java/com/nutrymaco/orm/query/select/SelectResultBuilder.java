package com.nutrymaco.orm.query.select;

import java.util.List;

public class SelectResultBuilder {
    private final SelectQueryContext context;

    public SelectResultBuilder(SelectQueryContext context) {
        this.context = context;
    }


    public <E> List<E> fetchInto(Class<E> clazz) {
        final var queryBuilder = SelectQueryBuilder.from(context);
        final var executor = SelectQueryExecutor.of(clazz);
        final var query = queryBuilder.getQuery();
        return executor.execute(query);
    }

    public String getCql() {
        return SelectQueryBuilder.from(context)
                .getQuery();
    }
}
