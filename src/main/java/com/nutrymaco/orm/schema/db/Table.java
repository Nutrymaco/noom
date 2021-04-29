package com.nutrymaco.orm.schema.db;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class Table {

    private final String name;
    private final Set<Column> columns;
    private final PrimaryKey primaryKey;

    private Table(TableBuilder tableBuilder) {
        this.name = tableBuilder.getName();
        this.columns = tableBuilder.getColumns();
        primaryKey = new PrimaryKey(tableBuilder.getPartitionColumns(), tableBuilder.getClusteringColumns());
    }

    public static TableBuilder builder() {
        return new TableBuilder();
    }

    public String name() {
        return name;
    }

    public Set<Column> columns() {
        return columns;
    }

    public PrimaryKey primaryKey() {
        return primaryKey;
    }

    public static class TableBuilder {
        private String name;
        private Set<Column> columns;
        private Set<Column> partitionColumns;
        private Set<Column> clusteringColumns;

        private String getName() {
            return name;
        }

        public Set<Column> getColumns() {
            return columns;
        }

        private Set<Column> getPartitionColumns() {
            return partitionColumns;
        }

        private Set<Column> getClusteringColumns() {
            return clusteringColumns;
        }

        public TableBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public TableBuilder setColumns(Set<Column> columns) {
            this.columns = columns;
            return this;
        }

        public TableBuilder addColumns(Set<Column> columns) {
            this.columns.addAll(columns);
            return this;
        }

        public TableBuilder setPartitionColumns(Set<Column> partitionColumns) {
            this.partitionColumns = partitionColumns;
            return this;
        }

        public TableBuilder setClusteringColumns(Set<Column> clusteringColumns) {
            this.clusteringColumns = clusteringColumns;
            return this;
        }

        public Table build() {
            return new Table(this);
        }
    }

    @Override
    public String toString() {
        return "Table{" +
                "name='" + name + '\n' +
                "columns=[\n" +
                columns.stream()
                        .map(column -> String.format("%s %s", column.type().getName(), column.name()))
                        .collect(Collectors.joining("\n")) +
                "]\n-->primaryColumns=" + primaryKey.partitionColumns() +
                "\n-->clusteringColumns=" + primaryKey.clusteringColumns() +
                '}';
    }
}
