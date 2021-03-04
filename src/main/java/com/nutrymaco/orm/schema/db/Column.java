package com.nutrymaco.orm.schema.db;

public record Column(String name, CassandraType type) {
    public static Column of(String name, CassandraType type) {
        return new Column(name, type);
    }
}
