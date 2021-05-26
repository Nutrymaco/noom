package com.nutrymaco.orm.query.select;

import com.nutrymaco.orm.query.condition.ComparisonCondition;
import com.nutrymaco.orm.query.condition.Condition;
import com.nutrymaco.orm.query.condition.EqualsCondition;
import com.nutrymaco.orm.query.condition.RangeCondition;
import com.nutrymaco.orm.util.ClassUtil;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class InMemoryFilterImpl<R> implements InMemoryFilter<R> {

    private final Collection<R> objects;
    private final Collection<Condition> conditions;

    public InMemoryFilterImpl(Collection<R> objects, Collection<Condition> conditions) {
        this.objects = objects;
        this.conditions = conditions;
    }

    @Override
    public List<R> filter() {
        return null;
    }
}
