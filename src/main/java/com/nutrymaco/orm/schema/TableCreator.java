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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TableCreator {
    private final SelectQueryContext queryContext;
    private final Entity entity;
    private final Table.TableBuilder tableBuilder = Table.builder();
    private final Map<Field, Column> columnByField = new HashMap<>();
    private final Set<Column> uniqueColumns = new HashSet<>();


    TableCreator(SelectQueryContext queryContext) {
        this.queryContext = queryContext;
        this.entity = queryContext.getEntity();
    }

    Table createTable() {
        var udt = getUserDefinedTypeForEntity(entity, new ArrayList<>());
        tableBuilder.setColumns(udt.columns());

        var conditionColumns = getConditionColumns();

        var tableName = Schema.getTableNameForQueryContext(queryContext);

        var table = tableBuilder
                .setName(tableName)
                .addColumns(conditionColumns.stream().filter(column -> !udt.columns().contains(column)).collect(Collectors.toSet()))
                .setPartitionColumns(Set.of(conditionColumns.get(0)))
                .setClusteringColumns(
                        Stream.of(conditionColumns.stream()
                                        .skip(1)
                                        .filter(column -> !uniqueColumns.contains(column)),
                                uniqueColumns.stream())
                            .flatMap(stream -> stream)
                            .collect(Collectors.toSet()))
                .build();

        return table;
    }

    private List<Column> getConditionColumns() {
        return queryContext.getConditions().stream()
                .map(Condition::fieldRef)
                .flatMap(List::stream)
                .map(fieldRef -> {
                    if (fieldRef.field().getEntity().equals(entity)) {
                        return columnByField.get(fieldRef.field());
                    }
                    var column = columnByField.get(fieldRef.field());
                    var prefix = Arrays.stream(fieldRef.path().split("\\."))
                                                .skip(1)
                                                .map(String::toLowerCase)
                                                .collect(Collectors.joining("_", "", "_"));
                    return new Column(prefix + column.name(), column.type());
                })
                .collect(Collectors.toList());
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
