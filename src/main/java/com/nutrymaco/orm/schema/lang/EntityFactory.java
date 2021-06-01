package com.nutrymaco.orm.schema.lang;

import com.nutrymaco.orm.constraints.Constraint;
import com.nutrymaco.orm.schema.db.annotations.Unique;
import com.nutrymaco.orm.util.ClassUtil;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EntityFactory {
    private final static Map<Class<?>, Entity> entityByClassCache = new HashMap<>();

    // clazz - model class
    public static <T> Entity from(final Class<T> clazz) {
        var cachedEntity = entityByClassCache.get(clazz);
        if (cachedEntity != null) {
            return cachedEntity;
        } else {
            Entity entity = new Entity(clazz.getSimpleName());
            entityByClassCache.put(clazz, entity);

            List<Field> fields =
                    Arrays.stream(clazz.getDeclaredFields())
                            .map(f -> {
                                final var fieldType = f.getType();
                                final var baseType = BaseType.from(fieldType.getName());
                                final var isUnique = f.isAnnotationPresent(Unique.class);

                                if (baseType.isPresent()) {
                                    var type = baseType.get();
                                    return Field.of(entity, f.getName(), type, isUnique, fieldType);
                                } else if (List.class.isAssignableFrom(fieldType)) {
                                    var genericClass = (Class<?>) ((ParameterizedType) f.getGenericType())
                                            .getActualTypeArguments()[0];
                                    var collectionType = CollectionType.of(Collection.LIST,
                                            EntityFactory.from(genericClass));
                                    return Field.of(entity, f.getName(), collectionType, isUnique, fieldType);
                                } else {
                                    return Field.of(entity, f.getName(), EntityFactory.from(fieldType), isUnique, fieldType);
                                }
                            }).collect(Collectors.toList());
            entity.setFields(fields);

            List<Constraint> constraints = Arrays.stream(clazz.getDeclaredFields())
                    .filter(field -> field.getAnnotations().length > 0)
                    .map(Constraint::of)
                    .flatMap(java.util.Collection::stream)
                    .collect(Collectors.toList());
            entity.setConstraints(constraints);
            return entity;
        }
    }

    public static Entity getByTableName(String tableName) {
        return ClassUtil.getRecordAndModelClasses()
                .filter(clazz ->
                        clazz.isAnnotationPresent(com.nutrymaco.orm.generator.annotations.Entity.class))
                // todo - возможно стоит добавить поиск по substring
                //  на случай если кастомная реализация будет добавлять префикс в начало имени таблицы
                .filter(clazz -> tableName.startsWith(clazz.getSimpleName().toLowerCase()))
                .findFirst()
                .map(EntityFactory::from)
                .orElseThrow();
    }

}
