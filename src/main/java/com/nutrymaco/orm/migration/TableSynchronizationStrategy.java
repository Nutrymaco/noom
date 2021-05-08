package com.nutrymaco.orm.migration;

import com.nutrymaco.orm.schema.db.Table;

public interface TableSynchronizationStrategy {

    // method which return implementation based on configuration
    static TableSynchronizationStrategy getInstance() {
        return new PercentageOrientedSynchronisationStrategy();
    }

    boolean isSync(Table table);

    void addTable(Table table);
}
