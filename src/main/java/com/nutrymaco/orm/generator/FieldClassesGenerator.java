package com.nutrymaco.orm.generator;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.config.InternalConfiguration;
import com.nutrymaco.orm.schema.lang.BaseType;
import com.nutrymaco.orm.schema.lang.CollectionType;
import com.nutrymaco.orm.schema.lang.Entity;
import com.nutrymaco.orm.schema.lang.EntityFactory;
import com.nutrymaco.orm.schema.lang.Field;
import com.nutrymaco.orm.util.ClassUtil;
import com.nutrymaco.orm.util.StringUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

// нерекурсивная генерация
class FieldClassesGenerator {
    static final String PUBLIC = "public";
    static final String PRIVATE = "private";
    static final String FINAL = "final";
    static final String STATIC = "static";
    static final String NEW = "new ";
    static final String RETURN = "return ";
    static final String VAR = "var ";
    static final String PUBLIC_FINAL = PUBLIC + " " + FINAL + " ";
    static final String PRIVATE_FINAL = PRIVATE + " " + FINAL + " ";
    static final String PUBLIC_STATIC_FINAL = PUBLIC + " " + STATIC + " " + FINAL + " ";
    static final String PRIVATE_STATIC_FINAL = PRIVATE + " " + STATIC + " " + FINAL + " ";

    @SuppressWarnings("SpellCheckingInspection")
    static final String FIELDREF = "FieldRef ";

    static final String PACKAGE = ConfigurationOwner.getConfiguration().packageName();
    static final String SRC_PATH = InternalConfiguration.srcPath();

    public static void generate() {
        final var classes = ClassUtil.getClasses().stream()
                .filter(clazz -> clazz.isAnnotationPresent(com.nutrymaco.orm.generator.annotations.Entity.class))
                .toArray(Class<?>[]::new);
        generate(classes).save();
    }

    private static TextManager generate(Class<?> ... classes) {
        return new TextManager(Arrays.stream(classes)
                .map(EntityFactory::from)
                .flatMap(entity -> {
                    final var newEntities = new ArrayList<Entity>();
                    newEntities.add(entity);
                    for (Field field : entity.getFields()) {
                        if (!field.isPrimitive()) {
                            final Entity curEntity;
                            if (field.getType() instanceof Entity) {
                                curEntity = (Entity) field.getType();
                            } else {
                                curEntity = (Entity) ((CollectionType)field.getType()).getEntryType();
                            }
                            final var entityName = curEntity.getName() + "In" + entity.getName();
                            final var initialFields =
                                    new ArrayList<>(curEntity.getFields());
                            curEntity.getFieldByEntity(entity)
                                    .ifPresent(initialFields::remove);
                            newEntities.add(
                                    Entity.of(entityName, initialFields)
                            );
                        }
                    }
                    return newEntities.stream();
                })
                .collect(Collectors.toMap(
                        entity -> entity,
                        FieldClassesGenerator::buildClassFromEntity
                )));
    }

    public static class TextManager {
        private final Map<Entity, String> texts;

        public TextManager(Map<Entity, String> texts) {
            this.texts = texts;
        }

        public void print() {
            texts.forEach((k, v) -> System.out.printf("%s\n\n", v));
        }

        public void save() {
            try {
                Files.createDirectory(Paths.get(SRC_PATH +
                        PACKAGE.replace(".", "/") + "/fields/"));
            } catch (IOException ignored) {

            }
            texts.forEach((entity, text) -> {
                Path filePath = Paths.get(SRC_PATH +
                        PACKAGE.replace(".", "/") + "/fields/_" + entity.getName() + ".java");

                var header = getHeaderForEntity(entity);
                byte[] classToBytes = (header + text).getBytes();

                try {
                    Files.write(filePath, classToBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private static String getHeaderForEntity(Entity entity) {
        var header = new StringBuilder();
        header.append("package ").append(PACKAGE).append(".fields").append(";\n");
        header.append("""
                import com.nutrymaco.orm.schema.lang.EntityFactory;
                import com.nutrymaco.orm.schema.lang.Entity;
                import com.nutrymaco.orm.schema.lang.FieldRef;
                """);
        header.append("\n");
        final var indexOfIn = entity.getName().indexOf("In");
        final String entityName;
        if (indexOfIn != -1) {
            entityName = entity.getName().substring(0, indexOfIn);
        } else {
            entityName = entity.getName();
        }
        header.append("import ").append(PACKAGE).append(".model.")
                .append(StringUtil.capitalize(entityName)).append(";\n");
        header.append("\n");
        return header.toString();
    }

    private static String buildClassFromEntity(Entity entity) {
        final StringBuilder classString = new StringBuilder();
        final var fullClassName = "_" + StringUtil.capitalize(entity.getName());
        final var indexOfIn = entity.getName().indexOf("In");
        final String entityName;
        if (indexOfIn != -1) {
            entityName = entity.getName().substring(0, indexOfIn);
        } else {
            entityName = entity.getName();
        }
        classString.append("public class ")
                .append(fullClassName)
                .append("{\n");

        classString.append("\t")
                .append(PRIVATE_FINAL)
                .append("String path;\n");

        classString.append("\t")
                .append(PUBLIC_STATIC_FINAL)
                .append("Entity ")
                .append(entityName.toUpperCase()).append("_ENTITY")
                .append(" = ")
                .append("EntityFactory.from(")
                .append(StringUtil.capitalize(entityName))
                .append(".class")
                .append(");\n");

        entity.getFields().forEach(field -> {
            if (field.isPrimitive()) {
                insertBaseType(classString, field);
            } else {
                insertReference(classString, entityName, (Entity) field.getPureType());
            }
        });

        classString.append("\t").append(PUBLIC_STATIC_FINAL)
                .append(fullClassName)
                .append(" ")
                .append(entityName.toUpperCase()).append(" ")
                .append(" = ")
                .append(NEW)
                .append(fullClassName)
                .append("(\"").append(entityName.toUpperCase()).append("\")")
                .append(";\n");

        classString.append("\t").append(fullClassName)
                .append("(String path) {\n");

        entity.getFields().forEach(field -> {
            if (field.getType() instanceof BaseType) {
                classString.append("\t\t");
                initializeBaseTypeField(classString, field);
            } else if (field.getType() instanceof CollectionType collectionType) {
                var entryType = collectionType.getEntryType();
                if (entryType instanceof BaseType) {
                    classString.append("\t\t");
                    initializeBaseTypeField(classString, field);
                }
            }
        });

        classString.append("\t\tthis.path = path;\n\t}\n");

        classString.append("\t").append(fullClassName)
                .append(" from(String add) {\n")
                .append("\t\t").append(VAR).append("copy")
                .append(" = ")
                .append(NEW).append(fullClassName)
                .append("(add + \".\" + path);\n");
        entity.getFields().stream()
                .filter(f -> !f.isPrimitive())
                .forEach(field -> {
                    final Entity type;
                    if (field.getType() instanceof CollectionType collectionType) {
                        type = (Entity) collectionType.getEntryType();
                    } else {
                        type = (Entity) field.getType();
                    }
                    classString.append("\t\tcopy.")
                            .append(type.getName().toUpperCase())
                            .append(" = ")
                            .append(type.getName().toUpperCase())
                            .append(".from(add);\n");
                });
        classString.append("\t\t")
                .append(RETURN).append("copy;").append("\n");
        classString.append("\t}\n");

        classString.append("}\n");

        return classString.toString();
    }

    private static void insertReference(StringBuilder stringBuilder,
                                        String fromEntityName, Entity entity) {
        stringBuilder.append("\t").append(PUBLIC).append(" ")
                .append("_")
                .append(StringUtil.capitalize(entity.getName()))
                .append("In")
                .append(StringUtil.capitalize(fromEntityName))
                .append(" ")
                .append(entity.getName().toUpperCase())
                .append(" = ")
                .append("_")
                .append(StringUtil.capitalize(entity.getName())).append("In").append(StringUtil.capitalize(fromEntityName))
                .append(".")
                .append(entity.getName().toUpperCase())
                .append(".from(\"").append(fromEntityName.toUpperCase()).append("\");")
                .append("\n");
    }

    private static void insertBaseType(StringBuilder stringBuilder,
                                       Field field) {
        stringBuilder.append("\t").append(PUBLIC_FINAL)
                .append(FIELDREF)
                .append(field.getName().toUpperCase())
                .append(";\n");
    }

    private static void initializeBaseTypeField(StringBuilder stringBuilder,
                                                Field field) {
        stringBuilder.append(field.getName().toUpperCase())
                .append(" = ")
                .append(NEW).append(FIELDREF)
                .append("(").append(field.getEntity().getName().toUpperCase()).append("_ENTITY")
                .append(".getFieldByName(\"")
                .append(field.getName().toLowerCase())
                .append("\"), path);\n");
    }


}
