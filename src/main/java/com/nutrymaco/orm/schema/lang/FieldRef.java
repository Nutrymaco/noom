package com.nutrymaco.orm.schema.lang;

import com.nutrymaco.orm.query.condition.Condition;
import com.nutrymaco.orm.query.condition.EqualsCondition;
import com.nutrymaco.orm.query.condition.GreaterOrEqualsCondition;
import com.nutrymaco.orm.query.condition.InCondition;

import java.util.List;

public record FieldRef(Field field, String path) {
    public Condition eq(Object value) {
        return new EqualsCondition(this, value);
    }
    public GreaterOrEqualsCondition ge(Object value) {
        return new GreaterOrEqualsCondition(this, value);
    }
    public <T> InCondition<T> in(List<T> value) {
        return new InCondition<>(this, value);
    }
}
