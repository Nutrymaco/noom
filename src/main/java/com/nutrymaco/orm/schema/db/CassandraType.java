package com.nutrymaco.orm.schema.db;

public sealed interface CassandraType permits CassandraBaseType, CassandraList, CassandraUserDefinedType {
    String getName();
}
