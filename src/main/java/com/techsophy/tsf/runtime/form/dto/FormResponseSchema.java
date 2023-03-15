package com.techsophy.tsf.runtime.form.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.techsophy.tsf.runtime.form.entity.Status;
import lombok.Value;
import lombok.With;
import java.util.List;
import java.util.Map;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.DATE_PATTERN;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.TIME_ZONE;

@With
@Value
public class FormResponseSchema
{
    String id;
    String name;
    Map<String,Object> components;
    List<Map<String,Object>> acls;
    Map<String,Object> properties;
    String type;
    Integer version;
    Boolean isDefault;
    String createdById;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_PATTERN, timezone = TIME_ZONE)
    String createdOn;
    String updatedById;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_PATTERN, timezone = TIME_ZONE)
    String updatedOn;
    Status elasticPush;
}
