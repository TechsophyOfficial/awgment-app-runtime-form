package com.techsophy.tsf.runtime.form.exception;

public class UserDetailsIdNotFoundException extends RuntimeException
{
    final String errorCode;
    final String message;
    public UserDetailsIdNotFoundException(String errorCode,String message)
    {
        super(message);
        this.errorCode=errorCode;
        this.message=message;
    }
}
