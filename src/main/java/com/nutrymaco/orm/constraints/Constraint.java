package com.nutrymaco.orm.constraints;

import com.nutrymaco.orm.constraints.annotations.LessThan;
import com.nutrymaco.orm.constraints.annotations.Match;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public sealed interface Constraint permits LessThanConstraint, MatchConstraint {
    boolean isMatch(Object object);

    static List<Constraint> of(Field field) {
        return Arrays.stream(field.getAnnotations())
                .map(annotation -> {
                    if (annotation instanceof LessThan lessThan) {
                        return new LessThanConstraint(field.getName(), lessThan.value());
                    } else if (annotation instanceof Match match) {
                        return new MatchConstraint(field.getName(), match.regex());
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableList());

    }
}
