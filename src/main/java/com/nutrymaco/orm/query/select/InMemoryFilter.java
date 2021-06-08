package com.nutrymaco.orm.query.select;

import com.nutrymaco.orm.query.condition.Condition;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public interface InMemoryFilter<R> extends Predicate<R> {

    static <R> InMemoryFilter<R> getInstance(Collection<Condition> conditions, Class<R> resultClass) {
        return new InMemoryFilterImpl<>(conditions);
    }

    static InMemoryFilter<Object> getInstance(Collection<Condition> conditions) {
        return new InMemoryFilterImpl<>(conditions);
    }

    List<R> filter(List<R> objects);

}
