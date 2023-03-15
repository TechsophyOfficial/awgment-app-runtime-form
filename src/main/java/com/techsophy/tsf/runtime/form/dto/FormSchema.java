package com.techsophy.tsf.runtime.form.dto;

import com.techsophy.tsf.runtime.form.entity.Status;
import lombok.Value;
import lombok.With;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;

@With
@Value
public class FormSchema
{
    @NotNull(message = ID_NOT_NULL) String id;
    @NotBlank(message = NAME_NOT_BLANK) String name;
    Map<String,Object> components;
    List<Map<String,Object>> acls;
    Map<String,Object> properties;
    String type;
    @NotNull(message = VERSION_NOT_BLANK) Integer version;
    Boolean isDefault;
    Status elasticPush= Status.DISABLED;
}
