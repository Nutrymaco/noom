package com.nutrymaco.orm.query.condition;

import com.nutrymaco.orm.schema.lang.FieldRef;

import java.util.List;

public interface Condition {
    String getCql();
    List<FieldRef> fieldRef();

}
