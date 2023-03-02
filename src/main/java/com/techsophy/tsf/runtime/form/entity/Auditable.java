package com.techsophy.tsf.runtime.form.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class Auditable
{
    @CreatedBy
    private String createdById;
    @CreatedDate
    private String createdOn;
    @LastModifiedBy
    private String updatedById;
    @LastModifiedDate
    private String updatedOn;
}
