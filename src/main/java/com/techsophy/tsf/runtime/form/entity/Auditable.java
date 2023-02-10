package com.techsophy.tsf.runtime.form.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class Auditable
{
    private String createdById;
    private String createdOn;
    private String updatedById;
    private String updatedOn;
}
