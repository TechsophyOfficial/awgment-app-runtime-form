package com.techsophy.tsf.runtime.form.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

@Data
public class ValidationResult
{
    @NotNull
    String keyPath;
    List<String> errors;
    String errorCode;

    public ValidationResult(String keyPath)
    {
        this.keyPath = keyPath;
    }

    public boolean isValid()
    {
        return errorCode==null || errorCode.isEmpty();
    }

    public ValidationResult(String keyPath,String errorCodes,String... errors)
    {
        this.keyPath = keyPath;
        this.errorCode=errorCodes;
        this.errors=Arrays.asList(errors);
    }

    public ValidationResult addPrefix(String prefix)
    {
        keyPath = prefix+"."+keyPath;
        return this;
    }
}
