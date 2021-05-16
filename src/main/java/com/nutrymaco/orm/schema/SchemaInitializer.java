package com.nutrymaco.orm.schema;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.query.Database;
import com.nutrymaco.orm.schema.db.Table;

import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

//todo - пофиксить название таблицы и вытекающие последствия
public class SchemaInitializer {
    private static final Database database = ConfigurationOwner.getConfiguration().database();
    private static final String KEYSPACE = ConfigurationOwner.getConfiguration().keyspace();
    private static final Logger logger = Logger.getLogger(SchemaInitializer.class.getSimpleName());

    public Schema getSchema() {
        logger.info("start schema initializing");
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
        return TableCreator.of(tableName).createTable();
    }

    record ColumnContext(String name, String typeName, boolean isClustering, boolean isPartition) {}
}