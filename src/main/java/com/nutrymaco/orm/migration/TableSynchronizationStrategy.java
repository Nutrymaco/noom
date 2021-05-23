package com.nutrymaco.orm.migration;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.schema.db.Table;

interface TableSynchronizationStrategy {

    boolean enableSynchronisation = ConfigurationOwner.getConfiguration().enableSynchronisation();

    // todo add getting implementation from configuration
    // в конфигурации должен быть продльюсер объекта стратегии, чтобы
    // пользователи могли нормально реализовать процесс инжектинга
    // method which return implementation based on configuration
    static TableSynchronizationStrategy getInstance() {
        if (enableSynchronisation) {
            return new PercentageOrientedSynchronisationStrategy();
        } else {
            return new DummyTableSynchronisationStrategy();
        }

    }

    boolean isSync(Table table);

    void addTable(Table table);
}
