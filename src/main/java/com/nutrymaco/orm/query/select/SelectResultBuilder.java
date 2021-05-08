package com.nutrymaco.orm.query.select;

import com.nutrymaco.orm.migration.TableSyncManager;

import java.util.List;

public class SelectResultBuilder {
    private static final TableSyncManager tableSyncManager = TableSyncManager.getInstance();
    private final SelectQueryContext context;

    public SelectResultBuilder(SelectQueryContext context) {
        this.context = context;
    }


    public <E> List<E> fetchInto(Class<E> clazz) {
        final var queryBuilder = SelectQueryBuilder.from(context);
        final var executor = QueryExecutor.of(clazz);
        final var query = queryBuilder.getQuery();
        final var res = executor.execute(query);
        res.forEach(o -> tableSyncManager.syncObject(context.getEntity(), o));
        return res;
    }

    public String getCql() {
        return SelectQueryBuilder.from(context)
                .getQuery();
    }
}
