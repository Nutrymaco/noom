package com.nutrymaco.orm.query.condition;

import com.nutrymaco.orm.schema.Schema;
import com.nutrymaco.orm.schema.lang.FieldRef;

import java.util.List;

public final class GreaterThanCondition<T extends Comparable<T>> extends AbstractCondition implements GreaterCondition {

    private final FieldRef<T> fieldRef;
    private final T value;

    public GreaterThanCondition(FieldRef<T> fieldRef, T value) {
        this.fieldRef = fieldRef;
        this.value = value;
    }

    @Override
    public String getCql() {
        return String.format(
                "%s > %s",
                getColumnNameByFieldRef(fieldRef), VALUE_MAPPER.getValueAsString(value)
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
