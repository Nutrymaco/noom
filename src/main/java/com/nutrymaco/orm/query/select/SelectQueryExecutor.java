package com.nutrymaco.orm.query.select;

import com.datastax.oss.driver.api.core.data.GettableByName;
import com.datastax.oss.driver.api.core.data.UdtValue;
import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.query.Database;
import com.nutrymaco.orm.query.condition.EqualsCondition;
import com.nutrymaco.orm.query.create.CreateQueryExecutor;
import com.nutrymaco.orm.schema.Schema;
import com.nutrymaco.orm.schema.db.Column;
import com.nutrymaco.orm.schema.db.Table;
import com.nutrymaco.orm.schema.lang.Entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class SelectQueryExecutor<E> {
    private static final List<Class<?>> PRIMITIVES = List.of(
            Integer.class, Long.class, String.class
    );
    private final static String keyspace = ConfigurationOwner.getConfiguration().keyspace();

    private final Database database = ConfigurationOwner.getConfiguration().database();
    private final Entity entity;
    private final EqualsCondition condition;
    private final Class<E> resultClass;

    private SelectQueryExecutor(Entity entity, EqualsCondition condition, Class<E> resultClass) {
        this.entity = entity;
        this.condition = condition;
        this.resultClass = resultClass;
    }

    public static <E> SelectQueryExecutor<E> from(SelectQueryContext<E> queryContext) {
        return new SelectQueryExecutor<E>(
                queryContext.getEntity(),
                queryContext.getCondition(),
                queryContext.getResultClass()
        );
    }

    public List<E> execute() {
        var table = Schema.getTableForQueryContext(new SelectQueryContext<E>(
                entity, condition, resultClass
        ));
        CreateQueryExecutor.INSTANCE.createTable(table);
        if (condition.value() instanceof String string && string.isEmpty()) {
            return List.of();
        }
        var conditionFieldName = getColumnName(table.getPrimaryColumns().get(0));
        var query = String.format("SELECT * FROM %s.%s WHERE %s = %s", keyspace,
                table.getName(), conditionFieldName, getValueAsString(condition.value()));
        var rows = database.execute(query);

        return rows.stream()
                .map(row -> rowToObject(row, resultClass))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static <E> Optional<E> getObjectById(Table table,
                                                Object id,
                                                Class<E> resultClass,
                                                Database database) {
        var conditionFieldName = getColumnName(table.getIdColumn());
        var query = String.format("SELECT * FROM %s.%s WHERE %s = %s", keyspace,
                table.getName(), conditionFieldName, getValueAsString(id));
        var rows = database.execute(query);

        return rows.stream()
                .map(row -> rowToObject(row, resultClass))
                .filter(Objects::nonNull)
                .findFirst();
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

    private static String getColumnName(Column column) {
        return column.name().replace(".", "_");
    }

    private static String getValueAsString(Object object) {
        if (object instanceof String) {
            return "'" + object + "'";
        }

        return object.toString();
    }

    private interface Getter {
        Object get(String name, Class<?> clazz);
    }
}
