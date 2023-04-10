package com.techsophy.tsf.runtime.form.entity;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.TP_FORM_DEFINITION_COLLECTION;

@EqualsAndHashCode(callSuper = true)
@Document(collection = TP_FORM_DEFINITION_COLLECTION)
@Data
public class FormDefinition extends Auditable
{
    private static final long serialVersionUID = 1L;
    @Id
    private BigInteger id;
    private String name;
    private BigInteger version;
    private Map<String,Object> components;
    private List<Map<String,Object>> acls;
    Map<String,Object> properties;
    private String type;
    private Boolean isDefault;
    private Status elasticPush = Status.DISABLED;
}
