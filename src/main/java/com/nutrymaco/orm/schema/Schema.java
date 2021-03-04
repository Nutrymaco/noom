package com.nutrymaco.orm.schema;

import com.nutrymaco.orm.generator.annotations.Repository;
import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.query.Database;
import com.nutrymaco.orm.query.select.SelectQueryContext;
import com.nutrymaco.orm.schema.db.CassandraBaseType;
import com.nutrymaco.orm.schema.db.CassandraList;
import com.nutrymaco.orm.schema.db.CassandraUserDefinedType;
import com.nutrymaco.orm.schema.db.Column;
import com.nutrymaco.orm.schema.db.Table;
import com.nutrymaco.orm.schema.lang.BaseType;
import com.nutrymaco.orm.schema.lang.CollectionType;
import com.nutrymaco.orm.schema.lang.Entity;
import com.nutrymaco.orm.schema.lang.Field;
import com.nutrymaco.orm.util.ClassUtil;
import com.nutrymaco.orm.util.StringUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.nutrymaco.orm.util.StringUtil.capitalize;

public final class Schema {
    private final static Database database = ConfigurationOwner.getConfiguration().database();
    private final static Set<Table> tables = new HashSet<>();
    private final static Map<Entity, List<Table>> entityTableMap = new HashMap<>();
    private final static Map<String, CassandraUserDefinedType> udtByName = new HashMap<>();

    static {
        System.out.println("in static init");
        ClassUtil.getClasses().stream()
                .filter(clazz -> clazz.isAnnotationPresent(Repository.class))
                .forEach(clazz -> {
                    try {
                        final var repository = clazz.getConstructor().newInstance();
                        Arrays.stream(clazz.getDeclaredMethods().clone())
                                .filter(method -> method.getModifiers() == Modifier.PUBLIC)
                                .forEach(method -> {
                                    var parameterType = method.getParameterTypes()[0];
                                    try {
                                        if (parameterType.isPrimitive()) {
                                            method.invoke(repository, 0);
                                        } else {
                                            method.invoke(repository, "");
                                        }
                                    } catch (IllegalAccessException | InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                });
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
                    }

                });

    }

    public static CassandraUserDefinedType getUserDefinedTypeForEntity(Entity<?> entity,
                                                                       List<Entity<?>> exceptEntities) {
        var columns = entity.getFields().stream()
                .filter(field -> {
                    if (field.getType() instanceof BaseType) {
                        return true;
                    } else if (field.getType() instanceof CollectionType collectionType) {
                        var entryType = collectionType.getEntryType();
                        if (entryType instanceof Entity entryEntity) {
                            return !exceptEntities.contains(entryEntity);
                        }
                        return true;
                    } else {
                        var type = (Entity) field.getType();
                        return !exceptEntities.contains(type);
                    }
                })
                .map(field -> {
                    final var columnName = field.getName();

                    if (field.getType() instanceof BaseType baseType) {
                        final var columnType = CassandraBaseType.of(baseType);
                        return Column.of(columnName, columnType);
                    } else if (field.getType() instanceof CollectionType collection) {
                        final var entryType = collection.getEntryType();
                        if (entryType instanceof BaseType baseType) {
                            final var cassandraList =
                                    CassandraList.valueOf(CassandraBaseType.of(baseType));
                            return Column.of(columnName, cassandraList);
                        } else {
                            final var updateExceptEntities = new ArrayList<>(exceptEntities);
                            updateExceptEntities.add(entity);
                            final var cassandraList =
                                    CassandraList.valueOf(getUserDefinedTypeForEntity((Entity) entryType, updateExceptEntities));
                            return Column.of(columnName, cassandraList);
                        }
                    } else {
                        var updateExceptEntities = new ArrayList<>(exceptEntities);
                        updateExceptEntities.add(entity);
                        var columnUserDefinedType =
                                getUserDefinedTypeForEntity((Entity) field.getType(), updateExceptEntities);
                        return Column.of(columnName, columnUserDefinedType);
                    }
                })
                .collect(Collectors.toList());

        var name = entity.getName() + "__" + columns.stream()
                .map(Column::name)
                .collect(Collectors.joining("_"));

        var udt = new CassandraUserDefinedType(name, columns);
        udtByName.put(name, udt);
        return udt;
    }

    public static Table getTableForQueryContext(SelectQueryContext queryContext) {
        // выбираем таблицу по параметрам или возвращаем null
        // эта проверка должна проводиться до начала программы, чтобы
        // были нужные таблицы

        var needTableName = getTableNameForQueryContext(queryContext);

        var needTable = tables.stream()
                .filter(table -> table.getName().equals(needTableName))
                .findFirst()
                .orElseGet(() -> createTableForQueryContext(queryContext));

        return needTable;
    }

    public static Table createTableForQueryContext(SelectQueryContext queryContext) {
        final var resultEntity = queryContext.getEntity();
        final var exceptedEntities = new ArrayList<Entity<?>>();
        final var columns = getUserDefinedTypeForEntity(resultEntity, exceptedEntities).columns();

        final var conditionField = queryContext.getCondition().field().field();
        final var conditionFieldPath = queryContext.getCondition().field().path().toLowerCase();
        final String columnName;
        if (conditionFieldPath.contains(".")) {
            columnName = conditionFieldPath.substring(conditionFieldPath.indexOf(".") + 1) + "." + conditionField.getName().toLowerCase();
        } else {
            columnName = conditionField.getName().toLowerCase();
        }
        final var conditionColumn =
                Column.of(columnName, CassandraBaseType.of((BaseType) conditionField.getType()));

        final var tableName = getTableNameForQueryContext(queryContext);

        final var idField = resultEntity.getFieldByName("id");
        final var idColumnName = getNameForColumnFromField(idField);
        final var idColumn =
                Column.of(idColumnName, CassandraBaseType.of((BaseType) idField.getType()));

        columns.remove(conditionColumn);
        final var table = Table.builder()
                .setName(tableName)
                .setColumns(columns)
                .setPrimaryColumns(List.of(conditionColumn))
                .setClusteringColumns(List.of(idColumn, conditionColumn))
                .build();
        tables.add(table);
        if (entityTableMap.containsKey(resultEntity)) {
            entityTableMap.get(resultEntity).add(table);
        } else {
            var tableList = new ArrayList<Table>();
            tableList.add(table);
            entityTableMap.put(resultEntity, tableList);
        }
        return table;
    }

    private static String getNameForColumnFromField(Field field) {
        return field.getName();
    }

    private static List<Entity<?>> getAllReferenceInEntity(Entity<?> entity) {
        return entity.getFields().stream()
                .filter(field -> !field.isPrimitive())
                .map(field -> {
                    if (field.getType() instanceof CollectionType type) {
                        return (Entity<?>) type.getEntryType();
                    } else {
                        return (Entity<?>) field.getType();
                    }
                }).collect(Collectors.toList());

    }

    private static String getTableNameForQueryContext(SelectQueryContext queryContext) {
        var entityName = queryContext.getEntity().getName();
        var conditionFieldRef = queryContext.getCondition().field();
        var conditionFieldName = Arrays.stream(
                conditionFieldRef
                        .path()
                        .split("\\.")).skip(1)
                .map(String::toLowerCase)
                .map(StringUtil::capitalize)
                .collect(Collectors.joining());
        return entityName + "By" + conditionFieldName + capitalize(conditionFieldRef.field().getName());
    }

    public static List<Table> getTablesByClass(Class<?> clazz) {
        var className = clazz.getSimpleName().replace("Record", "");
        return tables.stream()
                .filter(table -> table.getName().startsWith(className))
                .collect(Collectors.toList());
    }

    public List<Table> getTablesByEntity(Entity entity) {
        return entityTableMap.get(entity);
    }

    @Override
    public String toString() {
        return "Schema{" +
                "tables=" + tables +
                '}';
    }
}
