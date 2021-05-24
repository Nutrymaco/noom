package com.nutrymaco.orm.schema.db.table;

import com.nutrymaco.orm.schema.db.Table;
import com.nutrymaco.orm.schema.db.UserDefinedTypeFactory;
import com.nutrymaco.orm.schema.lang.Entity;

class BaseTableCreator implements TableCreator {
    private final Entity entity;
    private final Table.TableBuilder tableBuilder = Table.builder();

    BaseTableCreator(Entity entity) {
        this.entity = entity;
    }

    public Table createTable() {
        var udtFactory = new UserDefinedTypeFactory(entity);
        var udt = udtFactory.getUserDefinedTypeForEntity();
        tableBuilder.setColumns(udt.columns())
                .setEntity(entity);
        var table = tableBuilder
                .setColumns(udt.columns())
                .setPartitionColumns(udtFactory.getUniqueColumns())
                .build();
        return table;
    }
}
