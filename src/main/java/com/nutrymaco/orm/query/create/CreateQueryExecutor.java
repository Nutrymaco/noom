package com.nutrymaco.orm.query.create;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.query.Database;
import com.nutrymaco.orm.schema.db.CassandraList;
import com.nutrymaco.orm.schema.db.CassandraUserDefinedType;
import com.nutrymaco.orm.schema.db.Column;
import com.nutrymaco.orm.schema.db.Table;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum CreateQueryExecutor {
    INSTANCE(new HashSet<>(), new HashSet<>());
    private final Set<Table> createdTables;
    private final Set<CassandraUserDefinedType> createdUDT;
    private final Database database = ConfigurationOwner.getConfiguration().database();
    private final String keyspace = ConfigurationOwner.getConfiguration().keyspace();


    CreateQueryExecutor(Set<Table> createdTables, Set<CassandraUserDefinedType> createdUDT) {
        // потом тут будет запрос к бд на проверку
        this.createdTables = createdTables;
        this.createdUDT = createdUDT;
    }

    public void createTable(Table table) {
        if (createdTables.contains(table)) {
            return;
        }
        final var query = new StringBuilder();
        query.append("CREATE TABLE ").append(keyspace)
                .append(".").append(table.getName()).append("(\n");
        final var primaryColumn = table.getPrimaryColumns().get(0);
        query.append(getColumnName(primaryColumn)).append(" ")
                .append(getCQLOfColumnType(primaryColumn)).append(",\n");
        query.append(table.getColumns().stream()
                .map(column -> {
                    if (column.type() instanceof CassandraUserDefinedType userDefinedType) {
                        createUserDefinedType(userDefinedType);
                    } else if (column.type() instanceof CassandraList cassandraList &&
                            cassandraList.type() instanceof CassandraUserDefinedType userDefinedType) {
                        createUserDefinedType(userDefinedType);
                    }
                    return getColumnName(column) + " " + getCQLOfColumnType(column);
                })
                .collect(Collectors.joining(",\n")));
        query.append(",\n")
                .append("PRIMARY KEY ").append("(").append(getColumnName(primaryColumn))
                .append(", ").append(getColumnName(table.getIdColumn())).append(")\n");
        query.append(")\n");
        database.execute(query.toString());
        createdTables.add(table);
    }

    private void createUserDefinedType(CassandraUserDefinedType udt) {
        if (createdUDT.contains(udt)) {
            return;
        }
        final var query = new StringBuilder();
        query.append("CREATE TYPE ").append(keyspace)
                .append(".").append(udt.getName()).append("(\n");
        query.append(udt.columns().stream()
                .map(column -> {
                    if (column.type() instanceof CassandraUserDefinedType userDefinedType) {
                        createUserDefinedType(userDefinedType);
                    } else if (column.type() instanceof CassandraList cassandraList &&
                                cassandraList.type() instanceof CassandraUserDefinedType userDefinedType) {
                        createUserDefinedType(userDefinedType);
                    }
                    return getColumnName(column) + " " + getCQLOfColumnType(column);
                })
                .collect(Collectors.joining(",\n")));
        query.append("\n)\n");
        database.execute(query.toString());
        createdUDT.add(udt);
    }


    private String getCQLOfColumnType(Column column) {
        if (column.type() instanceof CassandraUserDefinedType userDefinedType) {
            return String.format("FROZEN <%s>", userDefinedType.getName());
        } else if (column.type() instanceof CassandraList cassandraList &&
                cassandraList.type() instanceof CassandraUserDefinedType userDefinedType) {
            return String.format("list <FROZEN<%s>>", userDefinedType.getName());
        }
        return column.type().getName();
    }

    private static String getColumnName(Column column) {
        return column.name().replace(".", "_");
    }
}
