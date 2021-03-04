package com.nutrymaco.orm.schema.lang;

public enum Collection {
    LIST("List"), SET("Set");
    private final String string;

    Collection(String string) {
        this.string = string;
    }

    public String toStringWithType(String type) {
        return String.format("%s<%s>", string, type);
    }

    @Override
    public String toString() {
        return string;
    }
}
