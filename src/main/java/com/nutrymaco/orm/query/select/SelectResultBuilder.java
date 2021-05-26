package com.nutrymaco.orm.query.select;

import com.nutrymaco.orm.migration.SynchronisationManager;

import java.util.List;

public class SelectResultBuilder {
    private static final SynchronisationManager synchronisationManager = SynchronisationManager.getInstance();
    private final SelectQueryContext context;

    public SelectResultBuilder(SelectQueryContext context) {
        this.context = context;
    }


    public <E> List<E> fetchInto(Class<E> clazz) {
        final var res = new SelectQueryExecutor<>(context, clazz).execute();
        res.forEach(o -> synchronisationManager.syncObject(context.getEntity(), o));
        return res;
    }

    public String getCql() {
        return SelectQueryBuilder.from(context)
                .getQuery();
    }
}
