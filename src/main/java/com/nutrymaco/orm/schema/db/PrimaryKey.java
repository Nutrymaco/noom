package com.nutrymaco.orm.schema.db;

import java.util.HashSet;
import java.util.Set;

public class PrimaryKey {
    private final Set<Column> columns = new HashSet<>();
    private final Set<Column> partitionColumns;
    private final Set<Column> clusteringColumns;

    public PrimaryKey(Set<Column> partitionColumns, Set<Column> clusteringColumns) {
        this.partitionColumns = partitionColumns;
        this.clusteringColumns = clusteringColumns;
        columns.addAll(partitionColumns);
        columns.addAll(clusteringColumns);
    }

    public Set<Column> columns() {
        return columns;
    }

    public Set<Column> partitionColumns() {
        return partitionColumns;
    }

    public Set<Column> clusteringColumns() {
        return clusteringColumns;
    }
}
