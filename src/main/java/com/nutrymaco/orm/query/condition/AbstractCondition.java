package com.nutrymaco.orm.query.condition;

import com.nutrymaco.orm.config.InternalConfiguration;
import com.nutrymaco.orm.query.mapper.ValueMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class AbstractCondition implements Condition {
    final static ValueMapper VALUE_MAPPER = InternalConfiguration.valueMapper();
}
