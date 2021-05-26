package com.nutrymaco.orm.query.condition;

import com.nutrymaco.orm.schema.Schema;
import com.nutrymaco.orm.schema.lang.FieldRef;

import java.util.List;

public final class GreaterOrEqualsCondition<T extends Comparable<T>> extends AbstractCondition implements GreaterCondition {

    private final FieldRef<T> fieldRef;
    private final T value;

    public GreaterOrEqualsCondition(FieldRef<T> fieldRef, T value) {
        this.fieldRef = fieldRef;
        this.value = value;
    }

    public RangeCondition le(T value) {
        return new RangeCondition(this, new LessOrEqualsCondition(fieldRef, value));
    }

    public Condition lt(T value) {
        return new RangeCondition(this, new LessThanCondition(fieldRef, value));
    }

    @Override
    public String getCql() {
        return String.format(
                "%s >= %s",
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
