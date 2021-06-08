package com.nutrymaco.orm.query.select;

import com.nutrymaco.orm.query.condition.ComparisonCondition;
import com.nutrymaco.orm.query.condition.Condition;
import com.nutrymaco.orm.query.condition.ConditionToPredicateMapper;
import com.nutrymaco.orm.query.condition.EqualsCondition;
import com.nutrymaco.orm.query.condition.RangeCondition;
import com.nutrymaco.orm.util.ClassUtil;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class InMemoryFilterImpl<R> implements InMemoryFilter<R> {

    private final Predicate<R> predicate;

    public InMemoryFilterImpl(Collection<Condition> conditions) {
        var conditionMapper = ConditionToPredicateMapper.getInstance();
        // это не проблема, потому что предикат не смотрит на класс все равно
        predicate = (Predicate<R>) conditions.stream()
                .map(conditionMapper::mapConditionToPredicate)
                .reduce(Predicate::and)
                // если список был пустым??
                .orElse(obj -> true);
    }

    @Override
    public List<R> filter(List<R> objects) {
        return objects.stream()
                .filter(predicate)
                .toList();
    }

    @Override
    public boolean test(R r) {
        return predicate.test(r);
    }
}
