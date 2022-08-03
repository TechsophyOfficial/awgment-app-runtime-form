package com.techsophy.tsf.runtime.form.dto;

import lombok.Value;
import lombok.With;

@With
@Value
public class FormDataResponse
{
    String id;
    Integer version;
}
