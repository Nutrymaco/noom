package com.nutrymaco.orm.query.condition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConditionValidator {

    private final List<Condition> conditions;

    public ConditionValidator(List<Condition> conditions) {
        this.conditions = conditions;
    }


    public List<Condition> validateConditions() {
        var conditionsByField = conditions.stream()
                .flatMap(condition -> condition.fieldRef().stream().map(fieldRef -> Map.entry(fieldRef.field(), condition)))
                .collect(Collectors.toMap(
                        entry -> entry.getKey(),
                        entry -> List.of(entry.getValue()),
                        (left, right) -> {
                            var all = new ArrayList<>(List.copyOf(left));
                            all.addAll(right);
                            return all;
                        }
                ));

        var reducedConditions = conditionsByField.entrySet().stream()
                .map(entry -> {
                    var field = entry.getKey();
                    var conditions = entry.getValue();
                    return switch (conditions.size()) {
                        case 1 -> conditions.get(0);
                        case 2 -> {
                            if (conditions.stream().allMatch(condition -> condition instanceof ComparisonCondition)) {
                                yield new RangeCondition(
                                        (ComparisonCondition) conditions.get(0),
                                        (ComparisonCondition) conditions.get(1)
                                );
                            }
                            throw new RuntimeException(String.format(
                                    "too much conditions for field - %s",
                                    field.getName()
                            ));
                        }
                        default -> throw new RuntimeException(String.format(
                                "too much conditions for field - %s",
                                field.getName()
                        ));
                    };
                })
                .collect(Collectors.toList());

        var conditionsByType = reducedConditions.stream()
                .collect(Collectors.groupingBy(Condition::getClass));

        List.of(
                GreaterOrEqualsCondition.class, GreaterThanCondition.class,
                LessOrEqualsCondition.class, GreaterThanCondition.class,
                InCondition.class, RangeCondition.class)
                .forEach(clazz -> {
                    if (conditionsByType.getOrDefault(clazz, List.of()).size() > 1) {
                        throw new RuntimeException("more than one %s condition".formatted(clazz.getSimpleName()));
                    }
                });

        var sortedConditions = new ArrayList<Condition>();
        sortedConditions.addAll(conditionsByType.getOrDefault(EqualsCondition.class, List.of()));

        sortedConditions.addAll(conditionsByType.getOrDefault(CompositeInCondition.class, List.of()));

        sortedConditions.addAll(conditionsByType.getOrDefault(InCondition.class, List.of()));

        sortedConditions.addAll(conditionsByType.getOrDefault(RangeCondition.class, List.of()));

        sortedConditions.addAll(conditionsByType.getOrDefault(GreaterCondition.class, List.of()));
        sortedConditions.addAll(conditionsByType.getOrDefault(GreaterOrEqualsCondition.class, List.of()));
        sortedConditions.addAll(conditionsByType.getOrDefault(GreaterThanCondition.class, List.of()));

        sortedConditions.addAll(conditionsByType.getOrDefault(LessCondition.class, List.of()));
        sortedConditions.addAll(conditionsByType.getOrDefault(LessOrEqualsCondition.class, List.of()));
        sortedConditions.addAll(conditionsByType.getOrDefault(GreaterThanCondition.class, List.of()));

        return sortedConditions;
    }
}
