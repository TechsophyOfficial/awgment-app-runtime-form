package com.techsophy.tsf.runtime.form.entity;

import lombok.*;
import java.math.BigInteger;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class FormDataDefinition extends Auditable
{
    private BigInteger id;
    private Integer version;
    private Map<String,Object> formData;
    private Map<String,Object> formMetadata;
}
