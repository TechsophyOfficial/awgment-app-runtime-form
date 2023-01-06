package com.techsophy.tsf.runtime.form.dto;

import lombok.Data;

@Data
public class ConstraintsList
{
    String conditional;
    Boolean required;
    Integer minLength;
    Integer maxLength;
    Integer minWords;
    Integer maxWords;
    Double min;
    Double max;
    Boolean unique;
    String type;
    String insideEditGrid;
    Conditional conditionalMap;
}
