package com.nutrymaco.orm.schema.db;

public final class CassandraList implements CassandraType {

    private final CassandraType type;

    private CassandraList(CassandraType type) {
        this.type = type;
    }

    public static CassandraList valueOf(CassandraType type) {
        return new CassandraList(type);
    }

    public CassandraType type() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("List<%s>", type);
    }

    @Override
    public String getName() {
        return String.format("list<%s>", type.getName());
    }
}
