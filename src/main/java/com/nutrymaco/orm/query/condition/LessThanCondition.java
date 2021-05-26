package com.nutrymaco.orm.query.condition;

import com.nutrymaco.orm.schema.Schema;
import com.nutrymaco.orm.schema.lang.FieldRef;

import java.util.List;

public class LessThanCondition<T extends Comparable<T>> extends AbstractCondition implements LessCondition {
    private final FieldRef fieldRef;
    private final T value;

    public LessThanCondition(FieldRef fieldRef, T value) {
        this.fieldRef = fieldRef;
        this.value = value;
    }

    public RangeCondition ge(T value) {
        return new RangeCondition(new GreaterOrEqualsCondition(fieldRef, value), this);
    }

    public RangeCondition gt(T value) {
        return new RangeCondition(new GreaterThanCondition(fieldRef, value), this);
    }

    @Override
    public String getCql() {
        return String.format(
                "%s < %s",
                Schema.getColumnNameByFieldRef(fieldRef), VALUE_MAPPER.getValueAsString(value)
        );
    }

    @Override
    public List<FieldRef> fieldRef() {
        return List.of(fieldRef);
    }

    public T value() {
        return value;
    }
}
