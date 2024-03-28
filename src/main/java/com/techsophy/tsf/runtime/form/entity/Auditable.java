package com.techsophy.tsf.runtime.form.entity;

import lombok.Data;
import org.springframework.data.annotation.*;

@Data
public class Auditable
{
    private String createdById;
    private String createdOn;
    private String updatedById;
    private String updatedOn;
}
