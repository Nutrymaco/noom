package com.nutrymaco.orm.query.select;

import com.nutrymaco.orm.query.condition.Condition;

import java.util.Collection;
import java.util.List;

public interface InMemoryFilter<R> {

    static <R> InMemoryFilter<R> getInstance(Collection<R> objects, Collection<Condition> conditions) {
        return new InMemoryFilterImpl<>(objects, conditions);
    }

    List<R> filter();

}
