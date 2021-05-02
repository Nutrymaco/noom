package com.nutrymaco.orm.schema.db;

import java.util.Set;

public final record CassandraUserDefinedType(String name, Set<Column> columns) implements CassandraType {
    @Override
    public String getName() {
        return name;
    }
}
