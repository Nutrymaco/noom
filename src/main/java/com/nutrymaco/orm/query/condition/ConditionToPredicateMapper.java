package com.nutrymaco.orm.query.condition;

import java.util.function.Predicate;

public interface ConditionToPredicateMapper {
    static ConditionToPredicateMapper getInstance(Condition condition) {
        return new ConditionToPredicateMapperImpl(condition);
    }

    Predicate<Object> mapConditionToPredicate();
}
