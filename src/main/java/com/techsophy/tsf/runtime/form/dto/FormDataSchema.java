package com.techsophy.tsf.runtime.form.dto;

import lombok.*;
import javax.validation.constraints.NotBlank;
import java.util.Map;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;

@With
@Value
public class FormDataSchema
{
    String id;
    @NotBlank(message = FORM_ID_NOT_BLANK)
    String formId;
    Integer version;
    Map<String,Object> formData;
    Map<String,Object> formMetadata;
}
