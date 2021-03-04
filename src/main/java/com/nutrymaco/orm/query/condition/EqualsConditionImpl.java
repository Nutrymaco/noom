package com.nutrymaco.orm.query.condition;

import com.nutrymaco.orm.schema.lang.FieldRef;

public record EqualsConditionImpl(FieldRef field1, Object value) implements EqualsCondition {

    @Override
    public FieldRef field() {
        return field1();
    }
}
