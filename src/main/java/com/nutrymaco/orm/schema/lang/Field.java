package com.nutrymaco.orm.schema.lang;

public class Field {
    private final Entity entity;
    private final String name;
    private final Type type;
    private final boolean isUnique;

    private Field(Entity entity, String name, Type type) {
        this(entity, name, type, false);
    }

    private Field(Entity entity, String name, Type type, boolean isUnique) {
        this.entity = entity;
        this.name = name;
        this.type = type;
        this.isUnique = isUnique;
    }

    public String getName() {
        return name;
    }

    public Entity getEntity() {
        return entity;
    }

    public Type getType() {
        return type;
    }

    public boolean isPrimitive() {
        if (type instanceof BaseType) {
            return true;
        } else if (type instanceof CollectionType collectionType) {
            return collectionType.getEntryType() instanceof BaseType;
        }
        return false;
    }

    public Type getPureType() {
        if (type instanceof CollectionType collectionType) {
            return collectionType.getEntryType();
        } else {
            return type;
        }
    }

    public static Field of(Entity entity, String name, Type type, boolean isId) {
        return new Field(entity, name, type, isId);
    }

    public boolean isUnique() {
        return isUnique;
    }

    @Override
    public String toString() {
        return "Field{" +
                "entity=" + (entity == null ? "" : entity.getName()) +
                ", name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}
