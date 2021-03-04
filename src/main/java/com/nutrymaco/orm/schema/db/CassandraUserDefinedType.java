package com.nutrymaco.orm.schema.db;

import java.util.List;

public record CassandraUserDefinedType(String name, List<Column> columns) implements CassandraType {
    @Override
    public String getName() {
        return name;
    }
}
