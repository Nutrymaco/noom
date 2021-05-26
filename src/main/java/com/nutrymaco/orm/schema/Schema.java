package com.nutrymaco.orm.schema;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.generator.annotations.Repository;
import com.nutrymaco.orm.migration.SynchronisationManager;
import com.nutrymaco.orm.query.create.CreateQueryExecutor;
import com.nutrymaco.orm.query.select.SelectQueryContext;
import com.nutrymaco.orm.schema.db.Table;
import com.nutrymaco.orm.schema.lang.CollectionType;
import com.nutrymaco.orm.schema.lang.Entity;
import com.nutrymaco.orm.schema.lang.FieldRef;
import com.nutrymaco.orm.schema.db.table.TableCreator;
import com.nutrymaco.orm.util.ClassUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
        tables.forEach(table -> {
            Optional.ofNullable(entityTableMap.get(table.entity()))
                    .ifPresentOrElse(list -> list.add(table),
                            () -> {
                                var list = new ArrayList<Table>();
                                list.add(table);
                                entityTableMap.put(table.entity(), list);
                            });
        });
    }

    //todo - race condition
    public static Schema getInstance() {
        if (instance == null) {
            var initializer = new SchemaInitializer();
            instance = initializer.getSchema();
            // todo - consider to do warm here once
//            instance.warm();
        }
        return instance;
    }

    //todo - refactor
    private void warm() {
        if (isWarmed) {
            return;
        }
        isWarmed = true;
        logger.info("start schema prepare via invoking repository methods");
        ClassUtil.getRepositoryClasses()
                .filter(clazz -> clazz.isAnnotationPresent(Repository.class))
                .forEach(clazz -> {
                    try {
                        final var repository = clazz.getConstructor().newInstance();
                        Arrays.stream(clazz.getDeclaredMethods())
                                .filter(method -> method.getModifiers() == Modifier.PUBLIC)
                                .forEach(method -> {
                                    try {
                                        logger.info("invoking method : %s".formatted(method.getName()));
                                        ClassUtil.invokeMethodWithDefaultArguments(repository, method);
                                    } catch (IllegalAccessException | InvocationTargetException e) {
                                        logger.info("error while invoking method : %s".formatted(method.getName()));
                                        logger.fine(e.getMessage());
                                    }
                                });
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
                    }
                });

        for (var table : tables) {
            SynchronisationManager.getInstance().addTable(table);
        }

        logger.info("finish schema prepare with tables : %s"
                .formatted(tables.stream().map(Table::name).collect(Collectors.joining(", "))));
    }

    public Table getTableForQueryContext(SelectQueryContext queryContext) {
        warm();

        // todo do this more эффективно
        var tableRequirements = TableCreator.of(queryContext).createTable();

        return entityTableMap.getOrDefault(queryContext.getEntity(), List.of()).stream()
                .filter(table -> table.primaryKey().partitionColumns()
                        .equals(tableRequirements.primaryKey().partitionColumns()))
                .filter(table -> table.primaryKey().columns()
                        .containsAll(tableRequirements.primaryKey().columns()))
                .findFirst()
                .orElseGet(() -> createTableForQueryContext(queryContext));
    }

    public Table createTableForQueryContext(SelectQueryContext queryContext) {
        warm();

        var creator = TableCreator.of(queryContext);
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

    public List<Table> getTablesByClass(Class<?> clazz) {
        warm();
        var className = clazz.getSimpleName().replace("Record", "");
        return tables.stream()
                .filter(table -> table.name().toLowerCase().startsWith(className.toLowerCase()))
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
