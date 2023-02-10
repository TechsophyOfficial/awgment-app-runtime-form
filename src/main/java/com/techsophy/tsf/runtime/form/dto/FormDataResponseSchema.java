package com.techsophy.tsf.runtime.form.dto;

import lombok.Value;
import lombok.With;

import java.util.Map;

@Value
@With
public class FormDataResponseSchema
{
    String id;
    Map<String,Object> formData;
    Map<String,Object> formMetadata;
    String version;
    String createdById;
    String createdOn;
    String updatedById;
    String updatedOn;
}
