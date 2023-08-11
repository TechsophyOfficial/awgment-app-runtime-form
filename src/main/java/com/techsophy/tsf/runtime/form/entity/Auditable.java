package com.techsophy.tsf.runtime.form.entity;

import lombok.Data;
import org.springframework.data.annotation.*;

@Data
public class Auditable
{
    @CreatedBy
    private String createdById;
    @CreatedDate
    private String createdOn;
    @LastModifiedBy
    private String updatedById;
    private String updatedOn;
}
