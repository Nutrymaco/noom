package com.nutrymaco.orm.query.condition;

import com.nutrymaco.orm.config.InternalConfiguration;
import com.nutrymaco.orm.query.mapper.ValueMapper;
import com.nutrymaco.orm.schema.lang.FieldRef;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class AbstractCondition implements Condition {
    final static ValueMapper VALUE_MAPPER = InternalConfiguration.valueMapper();

    protected String getColumnNameByFieldRef(FieldRef<?> fieldRef) {
        if (fieldRef.path().contains(".")) {
            return (fieldRef.path().substring(fieldRef.path().indexOf(".") + 1)
                    + "."
                    + fieldRef.field().getName()).toLowerCase().replaceAll("\\.", "_");
        } else {
            return fieldRef.field().getName().toLowerCase();
        }
    }
}
