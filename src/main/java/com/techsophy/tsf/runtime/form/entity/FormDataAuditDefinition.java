package com.techsophy.tsf.runtime.form.entity;

import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.Map;

import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.FORM_ID_NOT_BLANK;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class FormDataAuditDefinition extends Auditable
{
    String id;
    String formDataId;
    @NotBlank(message = FORM_ID_NOT_BLANK)
    String formId;
    Integer version;
    Map<String,Object> formData;
    Map<String,Object> formMetadata;
}
