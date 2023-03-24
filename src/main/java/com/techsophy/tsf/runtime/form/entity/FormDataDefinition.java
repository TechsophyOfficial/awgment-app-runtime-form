package com.techsophy.tsf.runtime.form.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class FormDataDefinition extends Auditable
{
    @Id
    private String id;
    private String formId;
    private int version;
    private Map<String,Object> formData;
    private Map<String,Object> formMetaData;
}
