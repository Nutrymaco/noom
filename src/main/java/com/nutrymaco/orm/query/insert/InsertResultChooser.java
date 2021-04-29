package com.nutrymaco.orm.query.insert;

import java.util.List;
import java.util.Optional;

public class InsertResultChooser {

    private final Object object;

    public InsertResultChooser(Object object) {
        this.object = object;
    }

    public InsertResultHandler execute() {
        var insertQueryBuilder = InsertQueryBuilder.of(object);
        var executor = InsertQueryExecutor.of(insertQueryBuilder);
        return executor.execute();
    }

    public Optional<List<String>> getCql() {
        var insertQueryBuilder = InsertQueryBuilder.of(object);
        return insertQueryBuilder.getCql();
    }
}
