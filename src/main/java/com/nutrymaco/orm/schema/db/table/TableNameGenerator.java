package com.nutrymaco.orm.schema.db.table;

import com.nutrymaco.orm.schema.db.PrimaryKey;
import com.nutrymaco.orm.schema.lang.Entity;


public interface TableNameGenerator {

    static TableNameGenerator getInstance(Entity entity, PrimaryKey primaryKey) {
        return new TableNameGeneratorImpl(entity, primaryKey);
    }

    String generateName();

}
