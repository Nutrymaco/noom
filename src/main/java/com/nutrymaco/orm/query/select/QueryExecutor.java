package com.nutrymaco.orm.query.select;

import com.datastax.oss.driver.api.core.data.GettableByName;
import com.datastax.oss.driver.api.core.data.UdtValue;
import com.datastax.oss.driver.api.core.session.Session;
import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.query.Database;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class QueryExecutor<E> {
    private final Database database = ConfigurationOwner.getConfiguration().database();
    private final Class<E> resultClass;

    private QueryExecutor(Class<E> resultClass) {
        this.resultClass = resultClass;
    }

    static <E> QueryExecutor<E> of(Class<E> resultClass) {
        return new QueryExecutor<>(resultClass);
    }

    public List<E> execute(String query) {
        var rows = database.execute(query);
        return rows.stream()
                .map(row -> RowToObjectMapper.getInstance(row, resultClass).mapToObject())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
