package com.nutrymaco.orm.query.insert;

public class InsertResultHandler {
    private final boolean success;

    InsertResultHandler(boolean success) {
        this.success = success;
    }

    public InsertResultHandler onSuccess(Runnable runnable) {
        if (success) {
            runnable.run();
        }
        return this;
    }

    public InsertResultHandler onFailure(Runnable runnable) {
        if (!success) {
            runnable.run();
        }
        return this;
    }
}