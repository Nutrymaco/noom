package com.nutrymaco.orm.query.insert;

public class InsertBuilder {
    public InsertResultChooser insert(Object object) {
        return new InsertResultChooser(object);
    }
}
