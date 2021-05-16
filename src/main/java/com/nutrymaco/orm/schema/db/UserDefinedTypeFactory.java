package com.nutrymaco.orm.schema.db;

import com.datastax.oss.driver.api.core.type.UserDefinedType;
import com.nutrymaco.orm.schema.lang.BaseType;
import com.nutrymaco.orm.schema.lang.CollectionType;
import com.nutrymaco.orm.schema.lang.Entity;
import com.nutrymaco.orm.schema.lang.Field;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UserDefinedTypeFactory {

    private final Entity entity;
    private final Set<Column> uniqueColumns = new HashSet<>();
    private final Map<Field, Column> columnByField = new HashMap<>();

    public UserDefinedTypeFactory(Entity entity) {
        this.entity = entity;
    }

    public CassandraUserDefinedType getUserDefinedTypeForEntity() {
        return getUserDefinedTypeForEntity(entity, new ArrayList<>());
    }

    public Set<Column> getUniqueColumns() {
        return uniqueColumns;
    }

    public Map<Field, Column> getColumnByField() {
        return columnByField;
    }

    private CassandraUserDefinedType getUserDefinedTypeForEntity(Entity entity, List<Entity> exceptEntities) {
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
                .sorted()
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
