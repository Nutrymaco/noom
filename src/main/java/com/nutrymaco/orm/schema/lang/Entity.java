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

    Entity(String name, List<Field> fields) {
        this(name);
        setFields(fields);
    }

    Entity(String name) {
        this.name = name;
        this.constraints = Collections.emptyList();
        this.fields = Collections.emptyList();
    }

    public static Entity of(String name, List<Field> fields) {
        return new Entity(name, fields);
    }

    public String getName() {
        return name;
    }

    public List<Field> getFields() {
        return fields;
    }

    public <T> Field<T> getFieldByName(String fieldName) {
        for (var field: fields) {
            if (field.getName().equalsIgnoreCase(fieldName)) {
                return (Field<T>) field;
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

    public boolean isMatch(Object object) {
        return constraints.stream()
                .allMatch(c -> c.isMatch(object));
    }

    void setFields(List<Field> fields) {
        if (fields.stream().filter(Field::isUnique).count() > 1) {
            throw new IllegalStateException("count of unique columns must be 1 or 0");
        }
        this.fields = Collections.unmodifiableList(fields);
    }

    void setConstraints(List<Constraint> constraints) {
        this.constraints =constraints;
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
