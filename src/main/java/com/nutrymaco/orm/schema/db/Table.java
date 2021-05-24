package com.nutrymaco.orm.schema.db;

import com.nutrymaco.orm.schema.lang.Entity;
import com.nutrymaco.orm.schema.db.table.TableNameGenerator;

import java.util.Set;
import java.util.stream.Collectors;

public final class Table {

    private final String name;
    private final Set<Column> columns;
    private final PrimaryKey primaryKey;
    private final Entity entity;

    private Table(TableBuilder tableBuilder) {
        this.columns = tableBuilder.getColumns();
        primaryKey = new PrimaryKey(tableBuilder.getPartitionColumns(), tableBuilder.getClusteringColumns());
        this.name = TableNameGenerator.getInstance(tableBuilder.getEntity(), primaryKey).generateName();
        this.entity = tableBuilder.getEntity();
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

    public Entity entity() {
        return entity;
    }

    public static class TableBuilder {
        private Set<Column> columns;
        private Set<Column> partitionColumns;
        private Set<Column> clusteringColumns;
        private Entity entity;

        public Set<Column> getColumns() {
            return columns;
        }

        private Set<Column> getPartitionColumns() {
            return partitionColumns == null ? Set.of() : partitionColumns;
        }

        private Set<Column> getClusteringColumns() {
            return clusteringColumns == null ? Set.of() : clusteringColumns;
        }

        private Entity getEntity() {
            return entity;
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

        public TableBuilder setEntity(Entity entity) {
            this.entity = entity;
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
