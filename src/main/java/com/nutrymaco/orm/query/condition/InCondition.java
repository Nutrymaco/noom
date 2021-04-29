package com.nutrymaco.orm.query.condition;

import com.nutrymaco.orm.schema.Schema;
import com.nutrymaco.orm.schema.lang.FieldRef;

import java.util.List;

public class InCondition<T> extends AbstractCondition {

    private final FieldRef field;
    private final List<T> value;

    public InCondition(FieldRef field, List<T> value) {
        this.field = field;
        this.value = value;
    }

    public List<FieldRef> fieldRef() {
        return List.of(field);
    }

    public List<T> value() {
        return value;
    }

    @Override
    public String getCql() {
        return String.format(
                "%s in (%s)",
                Schema.getColumnNameByFieldRef(field), VALUE_MAPPER.getValueAsString(value)
        );
    }
}
