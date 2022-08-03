package com.techsophy.tsf.runtime.form.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SequenceGeneratorResponse
{
    int length;
    String sequenceName;
    String lastValue;
    String createdOn;
    String updatedOn;
}
