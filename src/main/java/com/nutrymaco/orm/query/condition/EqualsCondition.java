package com.nutrymaco.orm.query.condition;

import com.nutrymaco.orm.schema.lang.Field;
import com.nutrymaco.orm.schema.lang.FieldRef;

public interface EqualsCondition {
    FieldRef field();
    Object value();
}
