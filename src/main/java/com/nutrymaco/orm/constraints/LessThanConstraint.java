package com.nutrymaco.orm.constraints;

import java.lang.reflect.Field;

public final class LessThanConstraint implements Constraint {

    private final String fieldName;
    private final int value;
    private Field field;
    private int objectValue;

    public LessThanConstraint(String fieldName, int value) {
        this.fieldName = fieldName;
        this.value = value;
    }


    @Override
    public boolean isMatch(Object object) {
        if (field == null) {
            try {
                field = object.getClass().getField(fieldName);
                objectValue = field.getInt(object);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(
                        String.format("cant find field - %s", fieldName)
                );
            } catch (IllegalAccessException e) {
                throw new RuntimeException(
                        String.format("cant access to field - %s", fieldName)
                );
            }
        }
        return objectValue < value;
    }
}
