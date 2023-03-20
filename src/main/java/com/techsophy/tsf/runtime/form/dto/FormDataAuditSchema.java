package com.techsophy.tsf.runtime.form.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.util.Map;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.FORM_ID_NOT_BLANK;

@Data
public class FormDataAuditSchema
{
    private final String id;
    private final String formDataId;
    @NotBlank(message = FORM_ID_NOT_BLANK)
    private final String formId;
    private final int version;
    private final Map<String,Object> formData;
    private final Map<String,Object> formMetadata;
}
