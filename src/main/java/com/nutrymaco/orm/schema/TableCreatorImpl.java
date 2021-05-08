package com.nutrymaco.orm.schema;

import com.nutrymaco.orm.query.condition.Condition;
import com.nutrymaco.orm.query.select.SelectQueryContext;
import com.nutrymaco.orm.schema.db.CassandraUserDefinedType;
import com.nutrymaco.orm.schema.db.Column;
import com.nutrymaco.orm.schema.db.Table;
import com.nutrymaco.orm.schema.db.UserDefinedTypeFactory;
import com.nutrymaco.orm.schema.lang.Entity;
import com.nutrymaco.orm.schema.lang.Field;
import com.nutrymaco.orm.schema.lang.FieldRef;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TableCreatorImpl implements TableCreator {
    private final Table.TableBuilder tableBuilder = Table.builder();
    private final Map<Field, Column> columnByField;
    private final Set<Column> uniqueColumns;

    private final Entity entity;
    private final Set<Column> conditionColumns;
    private final CassandraUserDefinedType udt;
    private final Set<FieldRef> primaryKeyFields;

    public TableCreatorImpl(SelectQueryContext context) {
        this.entity = context.getEntity();
        var udtFactory = new UserDefinedTypeFactory(entity);
        this.udt = udtFactory.getUserDefinedTypeForEntity(entity);
        this.columnByField = udtFactory.getColumnByField();
        this.uniqueColumns = udtFactory.getUniqueColumns();
        this.primaryKeyFields = context.getConditions().stream()
                .map(Condition::fieldRef)
                .flatMap(List::stream)
                .collect(Collectors.toSet());
        this.conditionColumns = getConditionColumns(primaryKeyFields);
    }

    public TableCreatorImpl(Entity entity, Set<FieldRef> primaryKeyFields) {
        this.entity = entity;
        var udtFactory = new UserDefinedTypeFactory(entity);
        this.udt = udtFactory.getUserDefinedTypeForEntity(entity);
        this.columnByField = udtFactory.getColumnByField();
        this.uniqueColumns = udtFactory.getUniqueColumns();
        this.conditionColumns = getConditionColumns(primaryKeyFields);
        this.primaryKeyFields = primaryKeyFields;
    }

    public Table createTable() {
        tableBuilder.setColumns(udt.columns())
                .setEntity(entity);
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
}
