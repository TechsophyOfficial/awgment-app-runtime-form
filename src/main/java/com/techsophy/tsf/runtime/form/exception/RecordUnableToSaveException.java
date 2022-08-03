package com.techsophy.tsf.runtime.form.exception;

public class RecordUnableToSaveException extends RuntimeException
{
    final String errorCode;
    final String message;
    public RecordUnableToSaveException(String errorCode, String message)
    {
        super(message);
        this.errorCode = errorCode;
        this.message=message;
    }
}
