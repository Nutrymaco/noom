package com.nutrymaco.orm.query.condition;

import com.nutrymaco.orm.schema.Schema;
import com.nutrymaco.orm.schema.lang.FieldRef;

import java.util.List;

public class LessOrEqualsCondition extends AbstractCondition implements LessCondition {

    private final FieldRef fieldRef;
    private final Object value;

    public LessOrEqualsCondition(FieldRef fieldRef, Object value) {
        this.fieldRef = fieldRef;
        this.value = value;
    }

    public RangeCondition gt(Object value) {
        return new RangeCondition(new GreaterThanCondition(fieldRef, value), this);
    }

    public RangeCondition le(Object value) {
        return new RangeCondition(new GreaterOrEqualsCondition(fieldRef, value), this);
    }

    @Override
    public String getCql() {
        return String.format(
                "%s <= %s",
                Schema.getColumnNameByFieldRef(fieldRef), VALUE_MAPPER.getValueAsString(value)
        );
    }

    @Override
    public List<FieldRef> fieldRef() {
        return List.of(fieldRef);
    }
}
