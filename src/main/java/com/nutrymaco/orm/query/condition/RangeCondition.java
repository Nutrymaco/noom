package com.nutrymaco.orm.query.condition;

import com.nutrymaco.orm.schema.lang.FieldRef;

import java.util.List;

// <>
public class RangeCondition extends AbstractCondition {
    private final GreaterCondition from;
    private final LessCondition to;

    public RangeCondition(ComparisonCondition from, ComparisonCondition to) {
        if (from instanceof GreaterCondition) {
            this.from = (GreaterCondition) from;
            this.to = (LessCondition) to;
        } else {
            this.from = (GreaterCondition) to;
            this.to = (LessCondition) from;
        }
    }

    @Override
    public String getCql() {
        return from.getCql() + " and " + to.getCql();
    }

    @Override
    public List<FieldRef> fieldRef() {
        return from.fieldRef();
    }
}
