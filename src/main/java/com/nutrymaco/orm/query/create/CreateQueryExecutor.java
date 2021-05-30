package com.nutrymaco.orm.query.create;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.migration.SynchronisationManager;
import com.nutrymaco.orm.query.Database;
import com.nutrymaco.orm.schema.db.CassandraList;
import com.nutrymaco.orm.schema.db.CassandraType;
import com.nutrymaco.orm.schema.db.CassandraUserDefinedType;
import com.nutrymaco.orm.schema.db.Column;
import com.nutrymaco.orm.schema.db.Table;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public enum CreateQueryExecutor {
    INSTANCE,
    DUMMY {
        @Override
        public void createTable(Table table) {
            logger.info("table : %s not created in db because accessToDb = false");
        }
    };
    private final static Database database = ConfigurationOwner.getConfiguration().database();
    private final static String KEYSPACE = ConfigurationOwner.getConfiguration().keyspace();
    private final static SynchronisationManager synchronisationManager = SynchronisationManager.getInstance();
    private final static Logger logger = Logger.getLogger(CreateQueryExecutor.class.getSimpleName());
    public static final boolean ACCESS_TO_DB = ConfigurationOwner.getConfiguration().accessToDB();

    private final Map<String, Table> createdTables = new HashMap<>();
    private final Set<CassandraUserDefinedType> createdUDT = new HashSet<>();

    public static CreateQueryExecutor getInstance() {
        if (ACCESS_TO_DB) {
            return INSTANCE;
        } else {
            return DUMMY;
        }
    }

    public void createTable(Table table) {
        if (createdTables.containsKey(table.name())) {
            return;
        }
        logger.info("start create table : %s".formatted(table.name()));

        final var query = new StringBuilder();

        query.append("CREATE TABLE ").append(KEYSPACE)
                .append(".").append(table.name()).append("(\n");
        query.append(getStringForColumns(table));
        query.append(",\n");
        query.append(getStringForPrimaryKey(table));
        query.append(")\n");

        database.execute(query.toString());

        synchronisationManager.addTable(table);

        logger.info("created table : %s".formatted(table.name()));
        createdTables.put(table.name(), table);
    }

    private String getStringForColumns(Table table) {
        final Function<Column, String> stringForColumn =
                column -> getColumnName(column) + " " + getCQLOfColumnType(column.type());

        final var columns = new StringBuilder();

        columns.append(
                table.primaryKey().partitionColumns().stream()
                    .map(stringForColumn)
                    .collect(Collectors.joining(",\n", "", ",\n"))
        );

        columns.append(
                table.columns().stream()
                        .filter(column -> !table.primaryKey().partitionColumns().contains(column))
                        .map(stringForColumn)
                        .collect(Collectors.joining(",\n"))
        );

        return columns.toString();
    }

    private String getStringForPrimaryKey(Table table) {
        final var primaryKey = new StringBuilder();
        primaryKey.append("PRIMARY KEY ((");

        primaryKey.append(
                table.primaryKey().partitionColumns().stream()
                        .map(CreateQueryExecutor::getColumnName)
                        .collect(Collectors.joining(", "))
        );
        primaryKey.append(")");
        if (!table.primaryKey().clusteringColumns().isEmpty()) {
            primaryKey.append(
                    table.primaryKey().clusteringColumns().stream()
                        .map(CreateQueryExecutor::getColumnName)
                        .collect(Collectors.joining(", ", ", ", ""))
            );
        }
        primaryKey.append(")");

        return primaryKey.toString();
    }

    private void createUserDefinedType(CassandraUserDefinedType udt) {
        if (createdUDT.contains(udt)) {
            return;
        }
        final var query = new StringBuilder();
        query.append("CREATE TYPE ").append(KEYSPACE)
                .append(".").append(udt.getName()).append("(\n");
        query.append(udt.columns().stream()
                .map(column -> {
                    if (column.type() instanceof CassandraUserDefinedType userDefinedType) {
                        createUserDefinedType(userDefinedType);
                    } else if (column.type() instanceof CassandraList cassandraList &&
                                cassandraList.type() instanceof CassandraUserDefinedType userDefinedType) {
                        createUserDefinedType(userDefinedType);
                    }
                    return getColumnName(column) + " " + getCQLOfColumnType(column.type());
                })
                .collect(Collectors.joining(",\n")));
        query.append("\n)\n");
        database.execute(query.toString());
        createdUDT.add(udt);
    }


    private String getCQLOfColumnType(CassandraType columnType) {
        if (columnType instanceof CassandraUserDefinedType userDefinedType) {
            createUserDefinedType(userDefinedType);
            return String.format("FROZEN <%s>", userDefinedType.getName());
        } else if (columnType instanceof CassandraList cassandraList &&
                cassandraList.type() instanceof CassandraUserDefinedType userDefinedType) {
            createUserDefinedType(userDefinedType);
            return String.format("list <FROZEN<%s>>", userDefinedType.getName());
        }
        return columnType.getName();
    }

    private static String getColumnName(Column column) {
        return column.name().replace(".", "_");
    }
}
