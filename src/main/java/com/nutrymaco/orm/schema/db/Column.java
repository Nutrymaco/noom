package com.nutrymaco.orm.schema.db;

import java.util.ArrayList;
import java.util.List;

public record Column(String name, CassandraType type) {
    public static Column of(String name, CassandraType type) {
        return new Column(name, type);
    }
}
