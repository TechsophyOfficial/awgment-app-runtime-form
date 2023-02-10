package com.techsophy.tsf.runtime.form.dto;

import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class ValidationResult
{
    @NotNull
    String keyPath;
    String errorCode;

    public ValidationResult(String keyPath)
    {
        this.keyPath = keyPath;
    }

    public boolean isValid()
    {
        return errorCode==null || errorCode.isEmpty();
    }

    public ValidationResult(String keyPath,String errorCodes)
    {
        this.keyPath = keyPath;
        this.errorCode=errorCodes;
    }

    public String getErrorMessage(GlobalMessageSource globalMessageSource)
    {
        return globalMessageSource.get(this.errorCode,this.keyPath);
    }
}
