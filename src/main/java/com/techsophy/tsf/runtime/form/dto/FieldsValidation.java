package com.techsophy.tsf.runtime.form.dto;

import lombok.Data;

@Data
public class FieldsValidation
{
    String key;
    ConstraintsList constraintsList;
}
