package com.nutrymaco.orm.query.select;

import com.nutrymaco.orm.query.condition.Condition;
import com.nutrymaco.orm.schema.db.Column;
import com.nutrymaco.orm.schema.db.Table;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ConditionPartitioner {

    private final List<Condition> dbSideConditions;
    private final List<Condition> inMemoryConditions;

    public ConditionPartitioner(Table table, Collection<Condition> conditions) {
        this.dbSideConditions = conditions.stream()
                .filter(condition -> table.columns().stream()
                        .map(Column::name)
                        .map(columnName -> columnName.replaceAll("_", "."))
                        .anyMatch(columnName -> condition.fieldRef().stream()
                                .map(fieldRef -> {
                                    var parts = fieldRef.path().split("\\.");
                                    var prefix = parts.length == 1
                                            ? ""
                                            : ("." + parts[parts.length - 1]);
                                    return prefix + fieldRef.field().getName();
                                })
                                .allMatch(entityAndField -> entityAndField.equalsIgnoreCase(columnName))))
                .toList();

        this.inMemoryConditions = conditions.stream()
                .filter(condition -> !dbSideConditions.contains(condition))
                .toList();
    }

    public List<Condition> getDbSideConditions() {
        return dbSideConditions;
    }

    public List<Condition> getInMemoryConditions() {
        return inMemoryConditions;
    }
}
