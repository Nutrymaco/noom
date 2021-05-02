package com.nutrymaco.orm.schema;

import com.nutrymaco.orm.query.condition.Condition;
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
import com.nutrymaco.orm.schema.lang.FieldRef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TableCreator {
    private final Table.TableBuilder tableBuilder = Table.builder();
    private final Map<Field, Column> columnByField = new HashMap<>();
    private final Set<Column> uniqueColumns = new HashSet<>();

    private final Entity entity;
    private final Set<Column> conditionColumns;
    private final CassandraUserDefinedType udt;
    private final Set<FieldRef> primaryKeyFields;

    public TableCreator(SelectQueryContext context) {
        this.entity = context.getEntity();
        this.udt = getUserDefinedTypeForEntity(entity, new ArrayList<>());
        this.primaryKeyFields = context.getConditions().stream()
                .map(Condition::fieldRef)
                .flatMap(List::stream)
                .collect(Collectors.toSet());
        this.conditionColumns = getConditionColumns(primaryKeyFields);
    }

    public TableCreator(Entity entity, Set<FieldRef> primaryKeyFields) {
        this.entity = entity;
        this.udt = getUserDefinedTypeForEntity(entity, new ArrayList<>());
        this.conditionColumns = getConditionColumns(primaryKeyFields);
        this.primaryKeyFields = primaryKeyFields;
    }

    Table createTable() {
        tableBuilder.setColumns(udt.columns());
        var tableName = Schema.getTableNameForQueryContext(entity, primaryKeyFields);

        var table = tableBuilder
                .setName(tableName)
                .addColumns(conditionColumns.stream().filter(column -> !udt.columns().contains(column)).collect(Collectors.toSet()))
                .setPartitionColumns(conditionColumns)
                .setClusteringColumns(uniqueColumns)
                .build();

        return table;
    }

    private Set<Column> getConditionColumns(Set<FieldRef> conditionFields) {
        return conditionFields.stream()
                .map(fieldRef -> {
                    if (fieldRef.field().getEntity().equals(entity)) {
                        return columnByField.get(fieldRef.field());
                    }
                    var column = columnByField.get(fieldRef.field());
                    var pathParts = fieldRef.path().split("\\.");
                    var prefix = Arrays.stream(pathParts)
                            .map(String::toLowerCase)
                            .skip(pathParts.length == 1 ? 0 : 1)
                            .collect(Collectors.joining("_", "", "_"));
                    return new Column(prefix + column.name(), column.type());
                })
                .collect(Collectors.toSet());
    }

    public CassandraUserDefinedType getUserDefinedTypeForEntity(Entity entity, List<Entity> exceptEntities) {
        var columns = entity.getFields().stream()
                .filter(field -> {
                    var fieldType = field.getType();
                    if (fieldType instanceof BaseType) {
                        return true;
                    } else if (fieldType instanceof CollectionType collectionType) {
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
                .map(field -> getColumnByField(entity, exceptEntities, field))
                .collect(Collectors.toSet());

        var name = entity.getName() + "__" + columns.stream()
                .map(Column::name)
                .collect(Collectors.joining("_"));


        return new CassandraUserDefinedType(name, columns);
    }

    private Column getColumnByField(Entity entity, List<Entity> exceptEntities, Field field) {
        final var columnName = field.getName();
        final Column column;

        if (field.getType() instanceof BaseType baseType) {
            var columnType = CassandraBaseType.of(baseType);
            column = Column.of(columnName, columnType);
        } else if (field.getType() instanceof CollectionType collection) {
            var entryType = collection.getEntryType();
            if (entryType instanceof BaseType baseType) {
                var cassandraList =
                        CassandraList.valueOf(CassandraBaseType.of(baseType));
                column = Column.of(columnName, cassandraList);
            } else {
                var updateExceptEntities = new ArrayList<>(exceptEntities);
                updateExceptEntities.add(entity);
                var cassandraList =
                        CassandraList.valueOf(getUserDefinedTypeForEntity((Entity) entryType, updateExceptEntities));
                column = Column.of(columnName, cassandraList);
            }
        } else {
            var updateExceptEntities = new ArrayList<>(exceptEntities);
            updateExceptEntities.add(entity);
            var columnUserDefinedType =
                    getUserDefinedTypeForEntity((Entity) field.getType(), updateExceptEntities);
            column = Column.of(columnName, columnUserDefinedType);
        }

        if (this.entity.equals(entity) && field.isUnique()) {
            uniqueColumns.add(column);
        }
        columnByField.put(field, column);
        return column;
    }
}
