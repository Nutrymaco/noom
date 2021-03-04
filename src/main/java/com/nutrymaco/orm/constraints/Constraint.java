package com.nutrymaco.orm.constraints;

import com.nutrymaco.orm.constraints.annotations.LessThan;
import com.nutrymaco.orm.constraints.annotations.Match;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public sealed interface Constraint permits LessThanConstraint, MatchConstraint {
    boolean isMatch(Object object);

    static Constraint of(Field field) {
        final var annotation = field.getAnnotations()[0];
        if (annotation instanceof LessThan lessThan) {
            return new LessThanConstraint(field.getName(), lessThan.value());
        } else if (annotation instanceof Match match) {
            return new MatchConstraint(field.getName(), match.regex());
        } else {
            throw new RuntimeException(
                    String.format("not constraint annotation - %s", annotation.toString()));
        }
    }
}
