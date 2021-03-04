package com.nutrymaco.orm.schema.lang;

import com.nutrymaco.orm.query.condition.EqualsCondition;
import com.nutrymaco.orm.query.condition.EqualsConditionImpl;

public record FieldRef(Field field, String path) {
    public EqualsCondition eq(Object value) {
        return new EqualsConditionImpl(this, value);
    }
}
