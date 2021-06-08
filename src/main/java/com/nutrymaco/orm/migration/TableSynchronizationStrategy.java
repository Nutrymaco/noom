package com.nutrymaco.orm.migration;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.query.select.SelectQueryContext;
import com.nutrymaco.orm.schema.db.Table;

import java.util.ServiceLoader;

public interface TableSynchronizationStrategy {

    boolean enableSynchronisation = ConfigurationOwner.getConfiguration().enableSynchronisation();
    TableSynchronizationStrategy instance = enableSynchronisation
            ? ServiceLoader.load(TableSynchronizationStrategy.class)
                    .findFirst()
                    .orElseGet(PercentageOrientedSynchronisationStrategy::getInstance)
            : new DummyTableSynchronisationStrategy();

    static TableSynchronizationStrategy getInstance() {
        return instance;
    }

    boolean isSync(Table table);

    default boolean isTableSyncByQueryContext(Table table, SelectQueryContext queryContext) {
        return isSync(table);
    }

    void addTable(Table table);
}
