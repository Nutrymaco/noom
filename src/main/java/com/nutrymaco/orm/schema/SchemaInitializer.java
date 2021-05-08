package com.nutrymaco.orm.schema;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.migration.TableSyncManager;
import com.nutrymaco.orm.query.Database;
import com.nutrymaco.orm.schema.db.Table;
import com.nutrymaco.orm.schema.lang.EntityFactory;
import com.nutrymaco.orm.schema.lang.FieldRef;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

//todo - пофиксить название таблицы и вытекающие последствия
public class SchemaInitializer {
    private static final Database database = ConfigurationOwner.getConfiguration().database();
    private static final String KEYSPACE = ConfigurationOwner.getConfiguration().keyspace();
    private static final Logger logger = Logger.getLogger(SchemaInitializer.class.getSimpleName());

    public Schema getSchema() {
        Set<Table> tables = database.execute(
                "SELECT * FROM system_schema.tables WHERE keyspace_name = '%s';".formatted(KEYSPACE))
                .stream()
                .map(row -> row.getString("table_name"))
                .map(this::getTableByName)
                .collect(Collectors.toSet());
        logger.info(() -> "initialize schema with tables : %s".formatted(tables.stream().map(Table::name).collect(Collectors.joining(", "))));
        return new Schema(tables);
    }

    private Table getTableByName(String tableName) {
        var primaryKeyColumns = database.execute(
                "SELECT * FROM system_schema.columns WHERE table_name = '%s' ALLOW FILTERING;".formatted(tableName)).stream()
                .map(row -> new ColumnContext(row.getString("column_name"), row.getString("type"),
                        Objects.equals(row.getString("kind"), "clustering"),
                        Objects.equals(row.getString("kind"), "partition_key")))
                .collect(Collectors.toSet());

        var entity = EntityFactory.getByTableName(tableName);
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

        var tableCreator = new TableCreatorImpl(entity, primaryKeyFields);
        return tableCreator.createTable();
    }

    record ColumnContext(String name, String typeName, boolean isClustering, boolean isPartition) {}
}