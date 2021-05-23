package com.nutrymaco.orm.schema;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.query.Database;
import com.nutrymaco.orm.schema.db.Column;
import com.nutrymaco.orm.schema.db.Table;
import com.nutrymaco.orm.schema.db.UserDefinedTypeFactory;
import com.nutrymaco.orm.schema.lang.Entity;
import com.nutrymaco.orm.schema.lang.EntityFactory;
import com.nutrymaco.orm.schema.lang.Field;
import com.nutrymaco.orm.schema.lang.FieldRef;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TableFromDBCreator implements TableCreator {

    private static final Database database = ConfigurationOwner.getConfiguration().database();
    private static final Logger logger = Logger.getLogger(TableFromDBCreator.class.getSimpleName());

    private final String tableName;
    private final Table.TableBuilder tableBuilder = Table.builder();
    private Map<Field, Column> columnByField;
    private Entity entity;

    public TableFromDBCreator(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public Table createTable() {
        logger.info("start putting in memory table for table name : %s".formatted(tableName));
        var primaryKeyColumns = database.execute(
                "SELECT * FROM system_schema.columns WHERE table_name = '%s' ALLOW FILTERING;".formatted(tableName)).stream()
                .map(row -> new SchemaInitializer.ColumnContext(row.getString("column_name"), row.getString("type"),
                        Objects.equals(row.getString("kind"), "clustering"),
                        Objects.equals(row.getString("kind"), "partition_key")))
                .collect(Collectors.toSet());

        this.entity = EntityFactory.getByTableName(tableName);
        if (tableName.equalsIgnoreCase(entity.getName())) {
            logger.info("it is base table");
            return TableCreator.of(entity).createTable();
        }
        var primaryKeyColumnNames = Arrays.stream(tableName.substring(entity.getName().length() + 2)
                .split("and"))
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<FieldRef> primaryKeyFields = primaryKeyColumns.stream()
                .filter(column -> primaryKeyColumnNames.stream()
                        .anyMatch(name -> column.name().replaceAll("_", "").endsWith(name)))
                .map(column -> {
                    var columnNameParts = column.name().split("_");

                    if (columnNameParts.length == 1) {
                        return new FieldRef<>(entity.getFieldByName(column.name()), column.name());
                    }

                    var entityName = column.name().split("_")[columnNameParts.length - 2];
                    var fieldName = columnNameParts[columnNameParts.length - 1];

                    var columnEntity = EntityFactory.getByTableName(entityName);
                    var field = columnEntity.getFieldByName(fieldName);

                    return new FieldRef<>(field,
                            entity.getName() + "." + column.name()
                                    .substring(0, column.name().lastIndexOf("_"))
                                    .replaceAll("_", "\\."));
                })
                .collect(Collectors.toSet());

        var udtFactory = new UserDefinedTypeFactory(entity);
        var udt = udtFactory.getUserDefinedTypeForEntity();
        this.columnByField = udtFactory.getColumnByField();
        var uniqueColumns = udtFactory.getUniqueColumns();
        var conditionColumns = getConditionColumns(primaryKeyFields);

        var partitionColumns = conditionColumns.stream()
                .filter(c ->
                        primaryKeyColumns.stream()
                                .filter(context -> context.name().equalsIgnoreCase(c.name()))
                                .findFirst().orElseThrow()
                                .isPartition())
                .collect(Collectors.toSet());

        var clusteringColumns = conditionColumns.stream()
                .filter(c ->
                        primaryKeyColumns.stream()
                                .filter(context -> context.name().equalsIgnoreCase(c.name()))
                                .findFirst().orElseThrow()
                                .isClustering())
                .collect(Collectors.toSet());

        return tableBuilder
                .setName(Schema.getTableNameForQueryContext(entity, primaryKeyFields))
                .setEntity(entity)
                .setColumns(udt.columns())
                .addColumns(conditionColumns.stream()
                        .filter(c -> !udt.columns().contains(c))
                        .collect(Collectors.toSet()))
                .setPartitionColumns(partitionColumns)
                .setClusteringColumns(Stream.of(uniqueColumns, clusteringColumns)
                        .flatMap(Set::stream)
                        .collect(Collectors.toSet()))
                .build();
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
