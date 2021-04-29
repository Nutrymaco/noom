package com.nutrymaco.orm.schema.db;

import com.nutrymaco.orm.schema.lang.BaseType;

public enum CassandraBaseType implements CassandraType {
    INTEGER, DOUBLE, STRING, DATE;

    public static CassandraBaseType of(BaseType baseType) {
        return switch (baseType) {
            case INTEGER -> INTEGER;
            case DOUBLE -> DOUBLE;
            case STRING -> STRING;
            case DATE -> DATE;
        };
    }

    @Override
    public String getName() {
        return switch (this) {
            case INTEGER -> "int";
            case DOUBLE -> "double";
            case STRING -> "text";
            case DATE -> "date";
        };
    }
}
