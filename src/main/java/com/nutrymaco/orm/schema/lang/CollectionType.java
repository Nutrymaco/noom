package com.nutrymaco.orm.schema.lang;

public final class CollectionType implements Type {
    private final Collection collection;
    private final Type entryType;

    private CollectionType(Collection collection, Type entryType) {
        this.collection = collection;
        this.entryType = entryType;
    }

    public static CollectionType of(Collection collection, Type entryType) {
        return new CollectionType(collection, entryType);
    }

    public Type getEntryType() {
        return entryType;
    }

    public Collection getCollection() {
        return collection;
    }

    @Override
    public String toString() {
        return String.format("%s<%s>", collection, entryType.getName());
    }

    @Override
    public String getName() {
        return toString();
    }
}
