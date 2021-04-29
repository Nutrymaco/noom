package com.nutrymaco.orm.schema.lang;

import java.util.Optional;

public enum BaseType implements Type {
    INTEGER("Integer"),
    DOUBLE("Double"),
    STRING("String"),
    DATE("Date");
    private final String string;

    BaseType(String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }

    public static Optional<BaseType> from(String type) {
        return switch (type) {
            case "int" -> Optional.of(INTEGER);
            case "double" -> Optional.of(DOUBLE);
            case "java.lang.String" -> Optional.of(STRING);
            case "java.time.OffsetDateTime" -> Optional.of(DATE);
            default -> Optional.empty();
        };
    }

    @Override
    public String getName() {
        return toString();
    }
}
