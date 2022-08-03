package com.techsophy.tsf.runtime.form.dto;

import lombok.Value;
import lombok.With;

@With
@Value
public class FormDataAuditResponse
{
    String id;
    Integer version;
}
