package com.nutrymaco.orm.migration;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.schema.db.Table;

interface MigrationTableManager {

    boolean enableSynchronisation = ConfigurationOwner.getConfiguration().enableSynchronisation();

    static MigrationTableManager getInstance() {
        if (enableSynchronisation) {
            return MigrationTableManagerImpl.getInstance();
        } else {
            return new DummyMigrationTableManager();
        }
    }

    // убирает из таблицы айдишек для миграции
    void syncId(Table table, Object id);

    long getCountOfIds(Table table);

    boolean isIdTableExists(Table table);
}
