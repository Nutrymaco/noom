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

    static TableCreator of(Entity entity, Set<FieldRef> primaryKeyFields) {
        return new TableCreatorImpl(entity, primaryKeyFields);
    }

    static TableCreator of(Entity entity) {
        return new BaseTableCreator(entity);
    }

    Table createTable();
}
