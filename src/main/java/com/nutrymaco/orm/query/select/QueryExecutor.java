package com.nutrymaco.orm.query.select;

import com.datastax.oss.driver.api.core.data.GettableByName;
import com.datastax.oss.driver.api.core.data.UdtValue;
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
    private static final List<Class<?>> PRIMITIVES = List.of(
            Integer.class, Long.class, String.class
    );
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
                .map(row -> rowToObject(row, resultClass))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static <E> E rowToObject(final GettableByName row, Class<E> resultClass) {
        final Constructor<E> constructor;
        try {
            constructor = resultClass.getConstructor(
                    resultClass.getConstructors()[0].getParameterTypes()
            );
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
        try {
            return constructor.newInstance(Arrays.stream(constructor.getParameters())
                    .map(parameter -> {
                        Object result;
                        if ((result = getPrimitiveRowValue(row::get, parameter)) != null) {
                            return result;
                        } else if (parameter.getType().isAssignableFrom(List.class)) {
                            final var genericType =
                                    ((ParameterizedType) parameter.getParameterizedType())
                                            .getActualTypeArguments()[0];
                            if ((result = getPrimitiveRowValue(row::getList, parameter)) != null) {
                                return result;
                            }
                            return Objects.requireNonNull(row.getList(parameter.getName(), UdtValue.class))
                                    .stream()
                                    .map(udt -> rowToObject(udt, (Class<?>) genericType))
                                    .collect(Collectors.toList());
                        } else {
                            return rowToObject(row.get(parameter.getName(), UdtValue.class), parameter.getType());
                        }
                    }).toArray());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Object getPrimitiveRowValue(Getter getter, Parameter parameter) {
        if (PRIMITIVES.contains(parameter.getType()) || parameter.getType().isPrimitive()) {
            return getter.get(parameter.getName(), parameter.getType());
        }
        return null;
    }

    private interface Getter {
        Object get(String name, Class<?> clazz);
    }
}
