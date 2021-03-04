package com.nutrymaco.orm.query.insert;

public class InsertBuilder {
    public <E> InsertQueryExecutor.InsertResultHandler insert(E object) {
        var executor = InsertQueryExecutor.of(object);
        return executor.execute();
    }
}
