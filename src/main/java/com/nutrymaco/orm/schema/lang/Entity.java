package com.nutrymaco.orm.schema.lang;

import com.nutrymaco.orm.constraints.Constraint;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class Entity implements Type {
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

    public static <T> Entity of(String name, List<Field> fields) {
        return new Entity(name, fields);
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

    public Optional<Field> getFieldByEntity(Entity entity) {
        return fields.stream()
                .filter(field -> field.getPureType().equals(entity))
                .findFirst();
    }

    public void setConstraints(List<Constraint> constraints) {
        this.constraints =constraints;
    }

    public boolean isMatch(Object object) {
        return constraints.stream()
                .allMatch(c -> c.isMatch(object));
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
