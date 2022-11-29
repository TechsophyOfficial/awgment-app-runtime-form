package com.techsophy.tsf.runtime.form.dto;

import lombok.Value;
import lombok.With;
import javax.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Map;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.FORM_ID_NOT_BLANK;

@With
@Value
public class FormDataAuditResponseSchema
{
    String id;
    String formDataId;
    @NotBlank(message = FORM_ID_NOT_BLANK)
    String formId;
    Integer version;
    Map<String,Object> formData;
    Map<String,Object> formMetadata;
    String  createdById;
    Instant createdOn;
}
