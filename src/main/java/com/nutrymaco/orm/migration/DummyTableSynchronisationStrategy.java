package com.nutrymaco.orm.migration;

import com.nutrymaco.orm.schema.db.Table;

class DummyTableSynchronisationStrategy implements TableSynchronizationStrategy {
    @Override
    public boolean isSync(Table table) {
        return true;
    }

    @Override
    public void addTable(Table table) {

    }
}
