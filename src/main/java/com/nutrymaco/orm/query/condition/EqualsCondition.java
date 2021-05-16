package com.nutrymaco.orm.query.condition;

import com.nutrymaco.orm.schema.Schema;
import com.nutrymaco.orm.schema.lang.FieldRef;

import java.util.Collections;
import java.util.List;

public class EqualsCondition extends AbstractCondition {

    private final FieldRef fieldRef;
    private final Object value;

    public EqualsCondition(FieldRef fieldRef, Object value) {
        this.fieldRef = fieldRef;
        this.value = value;
    }

    @Override
    public String getCql() {
        return String.format(
                "%s = %s",
                Schema.getColumnNameByFieldRef(fieldRef), VALUE_MAPPER.getValueAsString(value)
        );
    }

    @Override
    public List<FieldRef> fieldRef() {
        return Collections.singletonList(fieldRef);
    }
}

