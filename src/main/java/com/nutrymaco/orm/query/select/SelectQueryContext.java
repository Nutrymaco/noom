package com.nutrymaco.orm.query.select;

import com.nutrymaco.orm.query.condition.Condition;
import com.nutrymaco.orm.query.condition.ConditionValidator;
import com.nutrymaco.orm.schema.lang.Entity;

import java.util.List;

public class SelectQueryContext {
    private Entity entity;
    private List<Condition> conditions;

    public SelectQueryContext() {

    }

    public SelectQueryContext(Entity entity, List<Condition> conditions) {
        this.entity = entity;
        setConditions(conditions);
    }

    public Entity getEntity() {
        return entity;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public void setConditions(List<Condition> conditions) {
        var validator = new ConditionValidator(conditions);
        this.conditions = validator.validateConditions();
    }

}