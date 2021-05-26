package com.nutrymaco.orm.query.condition;

public sealed interface GreaterCondition extends ComparisonCondition permits GreaterThanCondition, GreaterOrEqualsCondition {
}
