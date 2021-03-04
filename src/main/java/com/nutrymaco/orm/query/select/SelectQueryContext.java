package com.nutrymaco.orm.query.select;

import com.nutrymaco.orm.query.condition.EqualsCondition;
import com.nutrymaco.orm.schema.lang.Entity;

public class SelectQueryContext<E> {
    private Entity entity;
    private EqualsCondition condition;
    private Class<E> resultClass;

    public SelectQueryContext() {

    }

    public SelectQueryContext(Entity entity, EqualsCondition condition, Class<E> resultClass) {
        this.entity = entity;
        this.condition = condition;
        this.resultClass = resultClass;
    }

    public Entity getEntity() {
        return entity;
    }

    public EqualsCondition getCondition() {
        return condition;
    }

    public Class<E> getResultClass() {
        return resultClass;
    }


    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public void setCondition(EqualsCondition condition) {
        this.condition = condition;
    }

    public <R> SelectQueryContext<R> setResultClass(Class<R> resultClass) {
        return new SelectQueryContext<R>(entity, condition, resultClass);
    }
}