package com.nutrymaco.orm.constraints;


import java.lang.reflect.Field;
import java.util.regex.Pattern;

public final class MatchConstraint implements Constraint {

    private final Pattern pattern;
    private final String fieldName;
    private Field field;
    private String value;

    public MatchConstraint(String fieldName, String value) {
        this.pattern = Pattern.compile(value);
        this.fieldName = fieldName;
    }

    @Override
    public boolean isMatch(Object object) {
        if (field == null) {
            try {
                field = object.getClass().getField(fieldName);
                value = (String) field.get(object);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(
                        String.format("cant find field with name - %s", fieldName)
                );
            } catch (IllegalAccessException e) {
                throw new RuntimeException(
                        String.format("cant access to field - %s", fieldName)
                );
            }
        }

        return pattern.matcher(value).matches();
    }
}
