package com.nutrymaco.orm.query.mapper;

import java.util.function.Function;

public class ValueMapper {
    public String getValueAsString(Object value) {
        if (value instanceof String) {
            return "'" + value + "'";
        }

        return value.toString();
    }
}
