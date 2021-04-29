package com.nutrymaco.orm.schema.exception;

public class TableCreationException extends RuntimeException {
    private final TableCreationExceptionCause cause;


    public TableCreationException(TableCreationExceptionCause cause) {
        this.cause = cause;
    }

    @Override
    public String getMessage() {
        return "Exception while creating table due " + cause;
    }
}
