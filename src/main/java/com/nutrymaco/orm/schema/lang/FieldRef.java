package com.nutrymaco.orm.schema.lang;

import com.nutrymaco.orm.query.condition.Condition;
import com.nutrymaco.orm.query.condition.EqualsCondition;
import com.nutrymaco.orm.query.condition.GreaterOrEqualsCondition;
import com.nutrymaco.orm.query.condition.GreaterThanCondition;
import com.nutrymaco.orm.query.condition.InCondition;
import com.nutrymaco.orm.query.condition.LessOrEqualsCondition;
import com.nutrymaco.orm.query.condition.LessThanCondition;

import java.util.List;

public record FieldRef<T>(Field<T> field, String path) {
    public Condition eq(T value) {
        return new EqualsCondition(this, value);
    }

    public <C extends Comparable<C>> GreaterOrEqualsCondition<C> ge(C value) {
        return new GreaterOrEqualsCondition<C>((FieldRef<C>) this, value);
    }

    public <C extends Comparable<C>> GreaterThanCondition<C> gt(C value) {
        return new GreaterThanCondition<C>((FieldRef<C>) this, value);
    }

    public <C extends Comparable<C>> LessThanCondition<C> lt(C value) {
        return new LessThanCondition<>((FieldRef<C>) this, value);
    }

    public <C extends Comparable<C>>LessOrEqualsCondition<C> le(C value) {
        return new LessOrEqualsCondition<>((FieldRef<C>) this, value);
    }

//    public <I> InCondition<I> in(List<I> value) {
//        return new InCondition<>(this, value);
//    }
}
