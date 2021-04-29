package com.nutrymaco.orm.schema;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.generator.annotations.Repository;
import com.nutrymaco.orm.query.Database;
import com.nutrymaco.orm.query.condition.Condition;
import com.nutrymaco.orm.query.create.CreateQueryExecutor;
import com.nutrymaco.orm.query.select.SelectQueryContext;
import com.nutrymaco.orm.schema.db.CassandraUserDefinedType;
import com.nutrymaco.orm.schema.db.Table;
import com.nutrymaco.orm.schema.lang.CollectionType;
import com.nutrymaco.orm.schema.lang.Entity;
import com.nutrymaco.orm.schema.lang.FieldRef;
import com.nutrymaco.orm.util.ClassUtil;

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

    private final static boolean CREATE_TABLE = ConfigurationOwner.getConfiguration().createTable();
    private final static Database database = ConfigurationOwner.getConfiguration().database();

    private final static Set<Table> tables = new HashSet<>();
    private final static Map<Entity, List<Table>> entityTableMap = new HashMap<>();
    private final static Map<String, CassandraUserDefinedType> udtByName = new HashMap<>();

    static {
        System.out.println("Schema initializing ...");
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

    public static Table getTableForQueryContext(SelectQueryContext queryContext) {
        // выбираем таблицу по параметрам или возвращаем null
        // эта проверка должна проводиться до начала программы, чтобы
        // были нужные таблицы

        var needTableName = getTableNameForQueryContext(queryContext);

        return entityTableMap.entrySet().stream()
                .filter(entry -> entry.getKey().equals(queryContext.getEntity()))
                .flatMap(entry -> entry.getValue().stream())
                .filter(table -> table.name().equals(needTableName))
                .findFirst()
                .orElseGet(() -> createTableForQueryContext(queryContext));
    }

    public static Table createTableForQueryContext(SelectQueryContext queryContext) {
        var creator = new TableCreator(queryContext);
        var table = creator.createTable();

        if (CREATE_TABLE) {
            CreateQueryExecutor.INSTANCE.createTable(table);
        }

        updateCache(table, queryContext.getEntity());

        return table;
    }



    public static String getColumnNameByFieldRef(FieldRef fieldRef) {
        if (fieldRef.path().contains(".")) {
            return (fieldRef.path().substring(fieldRef.path().indexOf(".") + 1)
                    + "."
                    + fieldRef.field().getName()).toLowerCase().replaceAll("\\.", "_");
        } else {
            return fieldRef.field().getName().toLowerCase();
        }
    }

    private static void updateCache(Table table, Entity resultEntity) {
        tables.add(table);
        if (entityTableMap.containsKey(resultEntity)) {
            entityTableMap.get(resultEntity).add(table);
        } else {
            var tableList = new ArrayList<Table>();
            tableList.add(table);
            entityTableMap.put(resultEntity, tableList);
        }
    }

    private static List<Entity> getAllReferenceInEntity(Entity entity) {
        return entity.getFields().stream()
                .filter(field -> !field.isPrimitive())
                .map(field -> {
                    if (field.getType() instanceof CollectionType type) {
                        return (Entity) type.getEntryType();
                    } else {
                        return (Entity) field.getType();
                    }
                }).collect(Collectors.toList());

    }

    // todo проверять уникальность ?
    static String getTableNameForQueryContext(SelectQueryContext queryContext) {
        var entityName = queryContext.getEntity().getName();
        var conditions = queryContext.getConditions();
        var conditionPart = conditions.stream()
                .map(Condition::fieldRef)
                .flatMap(List::stream)
                .distinct()
                .map(fieldRef -> {
                    var pathParts = fieldRef.path().split("\\.");
                    var lastPathPart = pathParts.length == 1 ? "" :  pathParts[pathParts.length - 1];
                    lastPathPart = lastPathPart.toLowerCase();
                    return capitalize(lastPathPart) + capitalize(fieldRef.field().getName());
                })
                .collect(Collectors.joining("And"));
        return entityName + "By" + conditionPart;
    }

    public static List<Table> getTablesByClass(Class<?> clazz) {
        var className = clazz.getSimpleName().replace("Record", "");
        return tables.stream()
                .filter(table -> table.name().startsWith(className))
                .collect(Collectors.toList());
    }

    public static Table getTableByName(String tableName) {
        return tables.stream()
                .filter(table -> table.name().equals(tableName))
                .findFirst()
                .orElseThrow();
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
