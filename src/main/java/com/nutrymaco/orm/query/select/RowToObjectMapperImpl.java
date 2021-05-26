package com.nutrymaco.orm.query.select;

import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.data.GettableByName;
import com.datastax.oss.driver.api.core.data.UdtValue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class RowToObjectMapperImpl<R> implements RowToObjectMapper<R> {

    private static final List<Class<?>> PRIMITIVES = List.of(
            Integer.class, Long.class, String.class
    );

    private final GettableByName row;
    private final Class<R> resultClass;

    RowToObjectMapperImpl(GettableByName row, Class<R> resultClass) {
        this.row = row;
        this.resultClass = resultClass;
    }

    @Override
    public R mapToObject() {
        return rowToObject(row, resultClass);
    }

    private static <E> E rowToObject(final GettableByName row, Class<E> resultClass) {
        final Constructor<E> constructor;
        try {
            constructor = resultClass.getDeclaredConstructor(
                    resultClass.getDeclaredConstructors()[0].getParameterTypes()
            );
            constructor.setAccessible(true);
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
