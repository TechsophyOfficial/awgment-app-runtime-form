package com.techsophy.tsf.runtime.form.entity;

import lombok.*;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class FormDataDefinition extends Auditable
{
    private String id;
    private String formId;
    private int version;
    private Map<String,Object> formData;
    private Map<String,Object> formMetaData;
}
