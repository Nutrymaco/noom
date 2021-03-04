package com.nutrymaco.orm.schema.lang;

public sealed interface Type permits BaseType, CollectionType, Entity {
    String getName();
}
