package com.techsophy.tsf.runtime.form.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigInteger;
@Data
@Document(collection = "tp_formAcl")
public class FormAclEntity extends Auditable{

    @Id
    private BigInteger id;

    @NonNull
    private String formId;

    @NonNull
    private String aclId;

}
