package com.techsophy.tsf.runtime.form.exception;

public class ExternalServiceErrorException extends RuntimeException
{
    final String errorCode;
    final String message;
    public ExternalServiceErrorException(String errorCode, String message)
    {
        super(message);
        this.errorCode=errorCode;
        this.message=message;
    }

}
