package com.techsophy.tsf.runtime.form.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormAclDto {
    String id;
    String formId;
    String aclId;
}
