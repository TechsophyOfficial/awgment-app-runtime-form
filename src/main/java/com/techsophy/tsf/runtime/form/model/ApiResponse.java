package com.techsophy.tsf.runtime.form.model;

import lombok.Value;

@Value
public class ApiResponse <T>
{
    T data;
    Boolean success;
    String message;
}
