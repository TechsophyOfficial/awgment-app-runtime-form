package com.techsophy.tsf.runtime.form.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Setter;

import java.util.List;

@Data
@Setter
public class Columns
{
    @JsonProperty("components")
    List<Component> component;
    Integer width;
    Integer offset;
    Integer push;
    Integer pull;
    String size;
    Integer currentWidth;
}
