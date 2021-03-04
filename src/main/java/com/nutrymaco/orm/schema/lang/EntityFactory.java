package com.nutrymaco.orm.schema.lang;

import com.nutrymaco.orm.constraints.Constraint;
import com.nutrymaco.orm.constraints.LessThanConstraint;
import com.nutrymaco.orm.constraints.annotations.LessThan;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EntityFactory {
    private final static Map<Class<?>, Entity<?>> entityByClassCache = new HashMap<>();

    public static <T> Entity<T> from(final Class<T> clazz) {
        var cachedEntity = entityByClassCache.get(clazz);
        if (cachedEntity != null) {
            return (Entity<T>) cachedEntity;
        } else {
            Entity<T> entity = new Entity<>(clazz.getSimpleName());
            entityByClassCache.put(clazz, entity);

            List<Field> fields =
                    Arrays.stream(clazz.getDeclaredFields())
                            .map(f -> {
                                var fieldType = f.getType();
                                var baseType = BaseType.from(fieldType.getName());

                                if (baseType.isPresent()) {
                                    var type = baseType.get();
                                    return Field.of(entity, f.getName(), type);
                                } else if (List.class.isAssignableFrom(fieldType)) {
                                    var genericClass = (Class<?>) ((ParameterizedType) f.getGenericType())
                                            .getActualTypeArguments()[0];
                                    var collectionType = CollectionType.of(Collection.LIST,
                                            EntityFactory.from(genericClass));
                                    return Field.of(entity, f.getName(), collectionType);
                                } else {
                                    return Field.of(entity, f.getName(), EntityFactory.from(fieldType));
                                }
                            }).collect(Collectors.toList());
            entity.setFields(fields);

            List<Constraint> constraints = Arrays.stream(clazz.getDeclaredFields())
                    // это может сломаться в случае добавления
                    // новых аннотация для полей
                    .filter(field -> field.getAnnotations().length > 0)
                    .map(Constraint::of)
                    .collect(Collectors.toList());

            return entity;
        }
    }


}
