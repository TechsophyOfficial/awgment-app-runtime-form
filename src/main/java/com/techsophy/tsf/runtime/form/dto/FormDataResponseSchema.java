package com.techsophy.tsf.runtime.form.dto;

import lombok.Value;
import lombok.With;
import java.time.Instant;
import java.util.Map;

@With
@Value
public class FormDataResponseSchema
{
    String id;
    Map<String,Object> formData;
    Map<String,Object> formMetadata;
    String version;
    String  createdById;
    Instant createdOn;
    String updatedById;
    Instant updatedOn;
}
