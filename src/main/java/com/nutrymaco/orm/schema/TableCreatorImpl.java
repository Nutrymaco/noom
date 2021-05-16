package com.nutrymaco.orm.schema;

import com.nutrymaco.orm.query.condition.Condition;
import com.nutrymaco.orm.query.condition.EqualsCondition;
import com.nutrymaco.orm.query.select.SelectQueryContext;
import com.nutrymaco.orm.schema.db.Column;
import com.nutrymaco.orm.schema.db.Table;
import com.nutrymaco.orm.schema.db.UserDefinedTypeFactory;
import com.nutrymaco.orm.schema.lang.Entity;
import com.nutrymaco.orm.schema.lang.Field;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TableCreatorImpl implements TableCreator {
    private final SelectQueryContext queryContext;
    private final Entity entity;
    private final Table.TableBuilder tableBuilder = Table.builder();
    private Map<Field, Column> columnByField;
    private Set<Column> uniqueColumns;


    TableCreatorImpl(SelectQueryContext queryContext) {
        this.queryContext = queryContext;
        this.entity = queryContext.getEntity();
    }

    @Override
    public Table createTable() {
        var udtFactory = new UserDefinedTypeFactory(entity);
        var udt = udtFactory.getUserDefinedTypeForEntity();
        this.columnByField = udtFactory.getColumnByField();
        this.uniqueColumns = udtFactory.getUniqueColumns();
        tableBuilder.setColumns(udt.columns());

        var conditionColumns = getConditionColumns();

        var tableName = Schema.getTableNameForQueryContext(queryContext);

        var partitionColumns = queryContext.getConditions().stream()
                .filter(cond -> cond instanceof EqualsCondition)
                .map(cond -> columnByField.get(cond.fieldRef().get(0).field()))
                .collect(Collectors.toSet());

        var notEqualsPartitionColumns = conditionColumns.stream()
                .filter(c -> !partitionColumns.contains(c))
                .collect(Collectors.toSet());

        var table = tableBuilder
                .setName(tableName)
                .setEntity(entity)
                .addColumns(conditionColumns.stream().filter(column -> !udt.columns().contains(column)).collect(Collectors.toSet()))
                .setPartitionColumns(partitionColumns)
                .setClusteringColumns(Stream.of(notEqualsPartitionColumns, uniqueColumns).flatMap(Set::stream).collect(Collectors.toSet()))
                .build();

        return table;
    }

    private Set<Column> getConditionColumns() {
        return queryContext.getConditions().stream()
                .map(Condition::fieldRef)
                .flatMap(List::stream)
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
                    var newColumn = new Column(prefix + column.name(), column.type());
                    columnByField.put(fieldRef.field(), newColumn);
                    return newColumn;
                })
                .collect(Collectors.toSet());
    }
}
