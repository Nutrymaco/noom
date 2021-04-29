package com.nutrymaco.orm.generator;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.config.InternalConfiguration;
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

class RecordGenerator {
    private final static String RECORD = "record ";

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
                    var entities = entity.getFields().stream()
                            .filter(f -> !f.isPrimitive())
                            .map(field -> (Entity) field.getPureType())
                            .collect(Collectors.toMap(
                                    entityFromField -> {
                                        final var fieldOrNull = entityFromField.getFieldByEntity(entity);
                                        return fieldOrNull
                                                .map(field -> getNameForRecordWithoutField(entityFromField, field))
                                                .orElseGet(() -> getNameForRecord(entityFromField));

                                    },
                                    entityFromField -> {
                                        final var fieldOrNull = entityFromField.getFieldByEntity(entity);
                                        return fieldOrNull
                                                .map(field -> getEntityWithoutField(entityFromField, field))
                                                .orElse(entityFromField);
                                    }));


                    entities.put(entity.getName() + "Record", entity);
                    return entities.entrySet().stream();
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> getRecordStringFromEntity(entry.getKey(), entry.getValue()),
                        (v1, v2) -> v2
                )));
    }

    public static class TextManager {
        private final Map<String, String> texts;

        public TextManager(Map<String, String> texts) {
            this.texts = texts;
        }

        public void print() {
            texts.forEach((k, v) -> System.out.printf("%s\n\n", v));
        }

        public void save() {
            try {
                Files.createDirectory(Paths.get(SRC_PATH +
                        PACKAGE.replace(".", "/") + "/records/"));
            } catch (IOException ignored) {

            }
            texts.forEach((name, text) -> {
                Path filePath = Paths.get(SRC_PATH +
                        PACKAGE.replace(".", "/") + "/records/" + name + ".java");

                var header = (new StringBuilder()).append("package ").append(PACKAGE)
                                .append(".records;\n\n");
                if (text.contains("List")) {
                    header.append("import java.util.List;\n\n");
                }

                byte[] classToBytes = (header + text).getBytes();

                try {
                    Files.write(filePath, classToBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private static String getNameForRecordWithoutField(Entity entity, Field field) {
        return entity.getName()
                + "In"
                + StringUtil.capitalize(field.getPureType().getName())
                + "Record";

    }

    private static String getNameForRecord(Entity entity) {
        return entity.getName() + "Record";
    }

    private static Entity getEntityWithoutField(Entity entity, Field field) {
        var newFields = new ArrayList<>(entity.getFields());
        newFields.remove(field);
        return Entity.of(entity.getName(), newFields);
    }

    private static String getFieldTypeNameByEntity(Field field, String entityName) {
        if (field.isPrimitive()) {
            return field.getType().getName();
        } else {
            var fieldType = (Entity)field.getPureType();
            if (fieldType.getFields().stream().allMatch(Field::isPrimitive)) {
                return StringUtil.capitalize(field.getName()) + "Record";
            }
        }
        return field.getPureType().getName() + "In" + entityName + "Record";
    }

    private static String getRecordStringFromEntity(String entityName, Entity entity) {
        var recordString = new StringBuilder();
//        if (!entityName.contains("In")) {
            recordString.append("public ");
//        }
        recordString.append(RECORD).append(entityName).append("(\n");
        var fields = entity.getFields().stream()
                .map(field -> {
                    String fieldTypeName;
                    if (field.getType() instanceof CollectionType collectionType) {
                        fieldTypeName = collectionType.getCollection().toStringWithType(getFieldTypeNameByEntity(field, entity.getName()));
                    } else {
                        fieldTypeName = getFieldTypeNameByEntity(field, entity.getName());
                    }
                    return "\t" + fieldTypeName + " " + field.getName();
                })
                .collect(Collectors.joining(",\n"));
        recordString.append(fields);
        recordString.append("){}\n");
        return recordString.toString();
    }
}
