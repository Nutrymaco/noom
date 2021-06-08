package com.nutrymaco.orm.query.condition;

import java.util.function.Predicate;

public interface ConditionToPredicateMapper {
    static ConditionToPredicateMapper getInstance() {
        return new ConditionToPredicateMapperImpl();
    }

    Predicate<Object> mapConditionToPredicate(Condition condition);
}
