package com.techsophy.tsf.runtime.form.dto;

import lombok.*;
import javax.validation.constraints.NotBlank;
import java.util.Map;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;

@Data
public class FormDataSchema
{
    private final String id;
    @NotBlank(message = FORM_ID_NOT_BLANK)
    private final String formId;
    private final int version;
    private final Map<String,Object> formData;
    private final Map<String,Object> formMetaData;
}
