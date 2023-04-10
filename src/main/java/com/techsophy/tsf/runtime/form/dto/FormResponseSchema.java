package com.techsophy.tsf.runtime.form.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.techsophy.tsf.runtime.form.entity.Status;
import lombok.Data;
import java.util.List;
import java.util.Map;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.DATE_PATTERN;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.TIME_ZONE;

@Data
public class FormResponseSchema
{
    private String id;
    private String name;
    private Map<String,Object> components;
    private List<Map<String,Object>> acls;
    private Map<String,Object> properties;
    private String type;
    private int version;
    private boolean isDefault;
    private String createdById;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_PATTERN, timezone = TIME_ZONE)
    private String createdOn;
    private String updatedById;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_PATTERN, timezone = TIME_ZONE)
    private String updatedOn;
    private Status elasticPush;
}
