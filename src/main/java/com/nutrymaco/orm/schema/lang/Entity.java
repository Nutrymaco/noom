package com.nutrymaco.orm.schema.lang;

import com.nutrymaco.orm.constraints.Constraint;

import java.awt.*;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class Entity<T> implements Type {
    private final String name;
    private List<Field> fields;
    private List<Constraint> constraints;

    Entity(String name, List<Field> fields, List<Constraint> constraints) {
        this(name, fields);
        this.constraints = constraints;
    }

    Entity(String name, List<Field> fields) {
        this(name);
        this.fields = Collections.unmodifiableList(fields);
    }

    Entity(String name) {
        this.name = name;
        this.constraints = Collections.emptyList();
        this.fields = Collections.emptyList();
    }

    public static <T> Entity<T> of(String name, List<Field> fields) {
        return new Entity<>(name, fields);
    }

    public String getName() {
        return name;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public Field getFieldByName(String fieldName) {
        for (var field: fields) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        throw new RuntimeException(
                String.format("field by name - %s not found", fieldName));
    }

    public Field getFieldByEntity(Entity<?> entity) {
        return fields.stream()
                .filter(field -> field.getPureType().equals(entity))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        String.format("field by entity - %s not found", entity.getName())
                ));
    }

    public void setConstraints(List<Constraint> constraints) {
        this.constraints =constraints;
    }

    // maybe is not best in performance
    // R - record
    public boolean isMatch(Object object) {
        return constraints.stream()
                .map(c -> c.isMatch(object))
                .filter(b -> !b)
                .findFirst()
                .orElse(true);
    }

    @Override
    public String toString() {
        return "Entity{" +
                "name='" + name + '\'' +
                ", fields=\n[" +
                fields.stream()
                        .map(f -> String.format("%s %s,", f.getType().getName(), f.getName()))
                        .collect(Collectors.joining("\n")) +
                "]\n}";
    }
}
