package com.nutrymaco.orm.schema.db;

import java.util.List;
import java.util.stream.Collectors;

public final class Table {

    private final String name;
    private final List<Column> columns;
    private final List<Column> primaryColumns;
    private final List<Column> clusteringColumns;

    private Table(TableBuilder tableBuilder) {
        this.name = tableBuilder.getName();
        this.columns = tableBuilder.getColumns();
        this.primaryColumns = tableBuilder.getPrimaryColumns();
        this.clusteringColumns = tableBuilder.getClusteringColumns();
    }

    public static TableBuilder builder() {
        return new TableBuilder();
    }

    public String getName() {
        return name;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public List<Column> getPrimaryColumns() {
        return primaryColumns;
    }

    public List<Column> getClusteringColumns() {
        return clusteringColumns;
    }

    public static class TableBuilder {
        private String name;
        private List<Column> columns;
        private List<Column> primaryColumns;
        private List<Column> clusteringColumns;

        private String getName() {
            return name;
        }

        private List<Column> getColumns() {
            return columns;
        }

        private List<Column> getPrimaryColumns() {
            return primaryColumns;
        }

        private List<Column> getClusteringColumns() {
            return clusteringColumns;
        }

        public TableBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public TableBuilder setColumns(List<Column> columns) {
            this.columns = columns;
            return this;
        }

        public TableBuilder setPrimaryColumns(List<Column> primaryColumns) {
            this.primaryColumns = primaryColumns;
            return this;
        }

        public TableBuilder setClusteringColumns(List<Column> clusteringColumns) {
            this.clusteringColumns = clusteringColumns;
            return this;
        }

        public Table build() {
            return new Table(this);
        }
    }

    public Column getIdColumn() {
        return clusteringColumns.get(0);
    }

    @Override
    public String toString() {
        return "Table{" +
                "name='" + name + '\n' +
                "columns=[\n" +
                columns.stream()
                        .map(column -> String.format("%s %s", column.type().getName(), column.name()))
                        .collect(Collectors.joining("\n")) +
                "]\n-->primaryColumns=" + primaryColumns +
                "\n-->clusteringColumns=" + clusteringColumns +
                '}';
    }
}
