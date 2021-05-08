package com.nutrymaco.orm.schema;

import com.nutrymaco.orm.schema.db.Table;
import com.nutrymaco.orm.schema.db.UserDefinedTypeFactory;
import com.nutrymaco.orm.schema.lang.Entity;

import java.util.ArrayList;

import static com.nutrymaco.orm.util.StringUtil.capitalize;

class BaseTableCreator implements TableCreator {
    private final Entity entity;
    private final Table.TableBuilder tableBuilder = Table.builder();

    BaseTableCreator(Entity entity) {
        this.entity = entity;
    }

    public Table createTable() {
        var udtFactory = new UserDefinedTypeFactory(entity);
        var udt = udtFactory.getUserDefinedTypeForEntity(entity);
        tableBuilder.setColumns(udt.columns())
                .setEntity(entity);
        var tableName = capitalize(entity.getName());
        var table = tableBuilder
                .setName(tableName)
                .setColumns(udt.columns())
                .setPartitionColumns(udtFactory.getUniqueColumns())
                .build();
        return table;
    }
}
