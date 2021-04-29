package com.nutrymaco.orm.query.condition;

import com.nutrymaco.orm.query.select.SelectQueryBuilder;
import com.nutrymaco.orm.schema.Schema;
import com.nutrymaco.orm.schema.lang.FieldRef;

import java.util.List;
import java.util.stream.Collectors;

public class CompositeInCondition extends AbstractCondition {
    private final List<FieldRef> fieldRefs;
    private final List<List<Object>> values;

    public CompositeInCondition(List<FieldRef> fieldRefs, List<List<Object>> values) {
        this.fieldRefs = fieldRefs;
        this.values = values;
    }

    @Override
    public String getCql() {
        return fieldRefs.stream()
                    .map(Schema::getColumnNameByFieldRef)
                    .collect(Collectors.joining("(", ",", ")")) +
                "in" +
                values.stream()
                        .map(tuple ->
                                tuple.stream()
                                        .map(VALUE_MAPPER::getValueAsString)
                                        .collect(Collectors.joining(","))
                        )
                        .collect(Collectors.joining("(", ",", ")"));
    }

    @Override
    public List<FieldRef> fieldRef() {
        return fieldRefs;
    }
}
