package com.nutrymaco.orm.schema;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.generator.annotations.Repository;
import com.nutrymaco.orm.query.condition.Condition;
import com.nutrymaco.orm.query.create.CreateQueryExecutor;
import com.nutrymaco.orm.query.select.SelectQueryContext;
import com.nutrymaco.orm.schema.db.Table;
import com.nutrymaco.orm.schema.lang.CollectionType;
import com.nutrymaco.orm.schema.lang.Entity;
import com.nutrymaco.orm.schema.lang.FieldRef;
import com.nutrymaco.orm.util.ClassUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.nutrymaco.orm.util.StringUtil.capitalize;

public class Schema {

    private static final Logger logger = Logger.getLogger(Schema.class.getSimpleName());
    private static Schema instance;
    private final static boolean CREATE_TABLE = ConfigurationOwner.getConfiguration().accessToDB();

    private final Set<Table> tables;
    private final Map<Entity, List<Table>> entityTableMap = new HashMap<>();
    private final Set<Entity> isBaseTableCreated = new HashSet<>();
    private boolean isWarmed = false;

    protected Schema(Set<Table> tables) {
        this.tables = tables;
    }

    //todo - race condition
    public static Schema getInstance() {
        if (instance == null) {
            var initializer = new SchemaInitializer();
            instance = initializer.getSchema();
        }
        return instance;
    }

    //todo - refactor
    private void warm() {
        if (isWarmed) {
            return;
        }
        logger.info("start schema prepare via invoking repository methods");
        ClassUtil.getEntityAndModelClasses().stream()
                .filter(clazz -> clazz.isAnnotationPresent(Repository.class))
                .forEach(clazz -> {
                    try {
                        final var repository = clazz.getConstructor().newInstance();
                        Arrays.stream(clazz.getDeclaredMethods().clone())
                                .filter(method -> method.getModifiers() == Modifier.PUBLIC)
                                .forEach(method -> {
                                    // todo - support more than 1 arguments and also 0 arguments
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
        isWarmed = true;
    }

    public Table getTableForQueryContext(SelectQueryContext queryContext) {
        // выбираем таблицу по параметрам или возвращаем null
        // эта проверка должна проводиться до начала программы, чтобы
        // были нужные таблицы
        warm();
        createBaseTable(queryContext.getEntity());
        var needTableName = getTableNameForQueryContext(queryContext);
        return entityTableMap.entrySet().stream()
                .filter(entry -> entry.getKey().equals(queryContext.getEntity()))
                .flatMap(entry -> entry.getValue().stream())
                .filter(table -> table.name().equals(needTableName))
                .findFirst()
                .orElseGet(() -> createTableForQueryContext(queryContext));
    }

    private void createBaseTable(Entity entity) {
        if (isBaseTableCreated.contains(entity)) {
            return;
        }
        var tableCreator = TableCreator.of(entity);
        var table  = tableCreator.createTable();
        if (CREATE_TABLE) {
            CreateQueryExecutor.INSTANCE.createTable(table);
        }
        updateCache(table, entity);
        isBaseTableCreated.add(entity);
    }

    public Table createTableForQueryContext(SelectQueryContext queryContext) {
        warm();
        createBaseTable(queryContext.getEntity());

        var creator = new TableCreatorImpl(queryContext);
        var table = creator.createTable();

        if (CREATE_TABLE) {
            CreateQueryExecutor.INSTANCE.createTable(table);
        }

        updateCache(table, queryContext.getEntity());

        return table;
    }

    public static String getColumnNameByFieldRef(FieldRef<?> fieldRef) {
        if (fieldRef.path().contains(".")) {
            return (fieldRef.path().substring(fieldRef.path().indexOf(".") + 1)
                    + "."
                    + fieldRef.field().getName()).toLowerCase().replaceAll("\\.", "_");
        } else {
            return fieldRef.field().getName().toLowerCase();
        }
    }

    private void updateCache(Table table, Entity resultEntity) {
        tables.add(table);
        if (entityTableMap.containsKey(resultEntity)) {
            entityTableMap.get(resultEntity).add(table);
        } else {
            var tableList = new ArrayList<Table>();
            tableList.add(table);
            entityTableMap.put(resultEntity, tableList);
        }
    }

    private List<Entity> getAllReferenceInEntity(Entity entity) {
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
        return getTableNameForQueryContext(queryContext.getEntity(),
                queryContext.getConditions().stream()
                        .map(Condition::fieldRef)
                        .flatMap(List::stream)
                        .collect(Collectors.toUnmodifiableSet()));
    }

    static String getTableNameForQueryContext(Entity entity, Set<FieldRef> additionalFields) {
        var entityName = entity.getName();
        var conditionPart = additionalFields.stream()
                .sorted(Comparator.comparing(f -> f.field().getName()))
                .map(fieldRef -> {
                    var pathParts = fieldRef.path().split("\\.");
                    var lastPathPart = pathParts[pathParts.length - 1];
                    lastPathPart = pathParts.length == 1 ? "" : lastPathPart.toLowerCase();
                    return capitalize(lastPathPart) + capitalize(fieldRef.field().getName());
                })
                .collect(Collectors.joining("And"));
        return entityName + "By" + conditionPart;
    }

    public List<Table> getTablesByClass(Class<?> clazz) {
        warm();
        var className = clazz.getSimpleName().replace("Record", "");
        return tables.stream()
                .filter(table -> table.name().startsWith(className))
                .collect(Collectors.toList());
    }

    public Table getTableByName(String tableName) {
        warm();
        return tables.stream()
                .filter(table -> table.name().equalsIgnoreCase(tableName))
                .findFirst()
                .orElseThrow();
    }

    public List<Table> getTablesByEntity(Entity entity) {
        warm();
        return Optional.ofNullable(entityTableMap.get(entity))
                .orElse(List.of());
    }

    public Set<Table> getTables() {
        return tables;
    }

    @Override
    public String toString() {
        return "Schema{" +
                "tables=" + tables +
                '}';
    }
}
