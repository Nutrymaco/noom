package com.nutrymaco.orm.schema.db.table;

import com.nutrymaco.orm.schema.db.PrimaryKey;
import com.nutrymaco.orm.schema.lang.Entity;
import com.nutrymaco.orm.util.StringUtil;

import java.util.Arrays;
import java.util.stream.Collectors;

class TableNameGeneratorImpl implements TableNameGenerator {

    private final Entity entity;
    private final PrimaryKey primaryKey;

    public TableNameGeneratorImpl(Entity entity, PrimaryKey primaryKey) {
        this.entity = entity;
        this.primaryKey = primaryKey;
    }

    @Override
    public String generateName() {
        var conditionPart = primaryKey.columns().stream()
                .map(column -> {
                    var pathParts = column.name().split("_");
                    return Arrays.stream(pathParts).map(StringUtil::capitalize).collect(Collectors.joining());
                })
                .sorted()
                .collect(Collectors.joining("And"));
        return entity.getName() + "By" + conditionPart;
    }
}
