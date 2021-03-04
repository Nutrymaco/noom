package com.nutrymaco.orm.query;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.nutrymaco.orm.query.insert.InsertBuilder;
import com.nutrymaco.orm.query.insert.InsertQueryExecutor;
import com.nutrymaco.orm.query.select.SelectBuilder;
import com.nutrymaco.orm.query.select.SelectQueryContext;
import com.nutrymaco.orm.query.select.WhereBuilder;
import com.nutrymaco.orm.schema.lang.Entity;

public final class Query {
    public static WhereBuilder select(Entity<?> entity) {
        SelectQueryContext<?> context = new SelectQueryContext<>();
        context.setEntity(entity);
        return new WhereBuilder(context);
    }

    public static <E> InsertQueryExecutor.InsertResultHandler insert(E object) {
        var executor = InsertQueryExecutor.of(object);
        return executor.execute();
    }
}
