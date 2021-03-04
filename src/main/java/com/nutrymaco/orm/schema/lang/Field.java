package com.nutrymaco.orm.schema.lang;

public class Field {
    private final Entity entity;
    private final String name;
    private final Type type;

    private Field(Entity entity, String name, Type type) {
        this.entity = entity;
        this.name = name;
        this.type = type;
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

//    public boolean isCollection() {
//        return type instanceof CollectionType;
//    }

//    public Condition eq(Object value) {
//        return new EqualsCondition(this, value);
//    }

    public static Field of(Entity entity, String name, Type type) {
        return new Field(entity, name, type);
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
