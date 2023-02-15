package com.techsophy.tsf.runtime.form.exception;

public class EntityPathException extends RuntimeException {
    final String errorCode;
    final String message;
    public EntityPathException(String errorCode, String message)
    {
        super(message);
        this.errorCode = errorCode;
        this.message=message;
    }
}
