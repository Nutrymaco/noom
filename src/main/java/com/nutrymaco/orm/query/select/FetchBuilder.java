package com.nutrymaco.orm.query.select;

import java.util.List;

public class FetchBuilder {
    private final SelectQueryContext<?> context;

    public FetchBuilder(SelectQueryContext<?> context) {
        this.context = context;
    }

    public <E> List<E> fetchInto(Class<E> clazz) {
        SelectQueryContext<E> newContext = context.setResultClass(clazz);
        var executor = SelectQueryExecutor.from(newContext);
        return executor.execute();
    }
}
