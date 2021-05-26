package com.nutrymaco.orm.query.select;

import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.data.GettableByName;

public interface RowToObjectMapper<R> {

    static <R> RowToObjectMapper<R> getInstance(GettableByName row, Class<R> resultClass) {
        return new RowToObjectMapperImpl<>(row, resultClass);
    }

    R mapToObject();

}
