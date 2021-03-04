package com.nutrymaco.orm.schema.db;

import com.nutrymaco.orm.schema.lang.BaseType;

public enum CassandraBaseType implements CassandraType {
    INTEGER, STRING, DATE;

    public static CassandraBaseType of(BaseType baseType) {
        return switch (baseType) {
            case INTEGER, LONG -> INTEGER;
            case STRING -> STRING;
            case DATE -> DATE;
        };
    }

    @Override
    public String getName() {
        return switch (this) {
            case INTEGER -> "int";
            case STRING -> "text";
            case DATE -> "date";
        };
    }
}
