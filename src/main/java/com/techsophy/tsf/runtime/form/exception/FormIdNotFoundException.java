package com.techsophy.tsf.runtime.form.exception;

public class FormIdNotFoundException extends RuntimeException
{
    final String errorCode;
    final String message;
    public FormIdNotFoundException(String errorCode,String message)
    {
        super(message);
        this.errorCode = errorCode;
        this.message=message;
    }
}
