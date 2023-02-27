package com.techsophy.tsf.runtime.form.exception;

public class ACLException extends RuntimeException
{
    final String errorCode;
    final String message;
    public ACLException(String errorCode, String message)
    {
        super(message);
        this.errorCode = errorCode;
        this.message=message;
    }
}
