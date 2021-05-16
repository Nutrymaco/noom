package com.nutrymaco.orm.schema;

import com.nutrymaco.orm.query.select.SelectQueryContext;
import com.nutrymaco.orm.schema.db.Table;
import com.nutrymaco.orm.schema.lang.Entity;
import com.nutrymaco.orm.schema.lang.FieldRef;

import java.util.Set;

public interface TableCreator {

    static TableCreator of(SelectQueryContext queryContext) {
        return new TableCreatorImpl(queryContext);
    }

    static TableCreator of(String tableName) {
        return new TableFromDBCreator(tableName);
    }

    static TableCreator of(Entity entity) {
        return new BaseTableCreator(entity);
    }

    Table createTable();
}
