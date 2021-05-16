package com.nutrymaco.orm.schema.lang;

import com.nutrymaco.orm.query.condition.Condition;
import com.nutrymaco.orm.query.condition.EqualsCondition;
import com.nutrymaco.orm.query.condition.GreaterOrEqualsCondition;
import com.nutrymaco.orm.query.condition.InCondition;

import java.util.List;

public record FieldRef<T>(Field<T> field, String path) {
    public Condition eq(T value) {
        return new EqualsCondition(this, value);
    }
    public GreaterOrEqualsCondition<T> ge(T value) {
        return new GreaterOrEqualsCondition<>(this, value);
    }
    public <I> InCondition<I> in(List<I> value) {
        return new InCondition<>(this, value);
    }
}
