package com.nutrymaco.orm.migration;

import com.nutrymaco.orm.schema.db.Table;

class DummyMigrationTableManager implements MigrationTableManager {
    @Override
    public void syncId(Table table, Object id) {

    }

    @Override
    public long getCountOfIds(Table table) {
        return 0;
    }

    @Override
    public boolean isIdTableExists(Table table) {
        return false;
    }
}
