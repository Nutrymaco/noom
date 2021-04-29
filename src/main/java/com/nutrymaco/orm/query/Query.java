package com.nutrymaco.orm.query;

import com.nutrymaco.orm.query.condition.CompositeInCondition;
import com.nutrymaco.orm.query.insert.InsertBuilder;
import com.nutrymaco.orm.query.insert.InsertResultChooser;
import com.nutrymaco.orm.query.select.SelectQueryContext;
import com.nutrymaco.orm.query.select.WhereBuilder;
import com.nutrymaco.orm.schema.lang.Entity;
import com.nutrymaco.orm.schema.lang.Field;
import com.nutrymaco.orm.schema.lang.FieldRef;

import java.util.Arrays;
import java.util.List;

public final class Query {
    public static WhereBuilder select(Entity entity) {
        SelectQueryContext context = new SelectQueryContext();
        context.setEntity(entity);
        return new WhereBuilder(context);
    }

    public static InsertResultChooser insert(Object object) {
        var insertBuilder = new InsertBuilder();
        return insertBuilder.insert(object);
    }

    public static CompositeInConditionBuilder of(FieldRef... fieldRefs) {
        return values -> new CompositeInCondition(Arrays.asList(fieldRefs), values);
    }

    public interface CompositeInConditionBuilder {
        CompositeInCondition in(List<List<Object>> values);
    }
}
