package com.nutrymaco.orm.query.condition;

import com.nutrymaco.orm.util.ClassUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

// fixme MOVIE.ACTOR.NAME -> ACTOR.NAME
public class ConditionToPredicateMapperImpl implements ConditionToPredicateMapper {

    private static final Map<Class<? extends Condition>, Function<? extends Condition, Predicate<Object>>> conditionToPredicateMap = new HashMap<>();

    private static final Function<EqualsCondition, Predicate<Object>> equalsPredicate = (EqualsCondition equalsCondition) -> obj -> {
        var fieldRef = equalsCondition.fieldRef().get(0);
        var fieldValues = ClassUtil.getValueByField(obj, fieldRef);
        var needValue = equalsCondition.value();
        return fieldValues.stream()
                .anyMatch(needValue::equals);
    };

    private static final Function<GreaterCondition, Predicate<Object>> greaterPredicate = (GreaterCondition greaterCondition) -> obj -> {
        var fieldRef = greaterCondition.fieldRef().get(0);
        var fieldValues = ClassUtil.getValueByField(obj, fieldRef);

        if (greaterCondition instanceof GreaterThanCondition<?> gt) {
            var value = gt.value();
            return fieldValues.stream()
                    .map(fieldValue -> (Comparable) fieldValue)
                    .anyMatch(fieldValue -> fieldValue.compareTo(value) > 0);
        } else {
            var ge = (GreaterOrEqualsCondition<?>) greaterCondition;
            var value = ge.value();
            return fieldValues.stream()
                    .map(fieldValue -> (Comparable) fieldValue)
                    .anyMatch(fieldValue -> fieldValue.compareTo(value) >= 0);

        }
    };

    private static final Function<LessCondition, Predicate<Object>> lessPredicate = (LessCondition lessCondition) -> obj -> {
        var fieldRef = lessCondition.fieldRef().get(0);
        var fieldValues = ClassUtil.getValueByField(obj, fieldRef);

        if (lessCondition instanceof LessThanCondition<?> lt) {
            var value = lt.value();
            return fieldValues.stream()
                    .map(fieldValue -> (Comparable) fieldValue)
                    .anyMatch(fieldValue -> fieldValue.compareTo(value) < 0);
        } else {
            var le = (LessOrEqualsCondition<?>) lessCondition;
            var value = le.value();
            return fieldValues.stream()
                    .map(fieldValue -> (Comparable) fieldValue)
                    .anyMatch(fieldValue -> fieldValue.compareTo(value) >= 0);

        }
    };

    private static final Function<RangeCondition, Predicate<Object>> rangePredicate = (RangeCondition rangeCondition) -> obj ->
            greaterPredicate.apply(rangeCondition.from())
                    .and(lessPredicate.apply(rangeCondition.to()))
                    .test(obj);


    public ConditionToPredicateMapperImpl() {
    }


    @Override
    public Predicate<Object> mapConditionToPredicate(Condition condition) {
        if (condition instanceof EqualsCondition equalsCondition) {
            return equalsPredicate.apply(equalsCondition);
        } else if (condition instanceof GreaterCondition greaterCondition) {
            return greaterPredicate.apply(greaterCondition);
        } else if (condition instanceof LessCondition lessCondition) {
            return lessPredicate.apply(lessCondition);
        } else if (condition instanceof RangeCondition rangeCondition) {
            return rangePredicate.apply(rangeCondition);
        } else {
            throw new UnsupportedOperationException("condition : %s not supported".formatted(condition.getClass()));
        }
    }
}
