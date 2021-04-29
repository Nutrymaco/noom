package com.nutrymaco.orm.schema.lang;

public class Field<T> {
    private final Entity entity;
    private final String name;
    private final Type type;
    private final boolean isUnique;
    private final Class<? extends T> clazz;

    private Field(Entity entity, String name, Type type, Class<? extends T> clazz) {
        this(entity, name, type, false, clazz);
    }

    private Field(Entity entity, String name, Type type, boolean isUnique, Class<? extends T> clazz) {
        this.entity = entity;
        this.name = name;
        this.type = type;
        this.isUnique = isUnique;
        this.clazz = clazz;
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

    public Class<? extends T> clazz() {
        return clazz;
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

    public static <T> Field<T> of(Entity entity, String name, Type type, boolean isId, Class<? extends T> fieldType) {
        return new Field<T>(entity, name, type, isId, fieldType);
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
