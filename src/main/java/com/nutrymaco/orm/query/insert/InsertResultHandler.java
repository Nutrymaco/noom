package com.nutrymaco.orm.query.insert;

import com.nutrymaco.orm.util.Voider;

public class InsertResultHandler {
    private final boolean success;

    InsertResultHandler(boolean success) {
        this.success = success;
    }

    public InsertResultHandler onSuccess(Voider voider) {
        if (success) {
            voider.doSome();
        }
        return this;
    }

    public InsertResultHandler onFailure(Voider voider) {
        if (!success) {
            voider.doSome();
        }
        return this;
    }
}