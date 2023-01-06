package com.techsophy.tsf.runtime.form.dto;

import lombok.Value;
import lombok.With;
import java.util.LinkedHashMap;
import java.util.List;

@Value
@With
public class Components
{
    String display;
    List<LinkedHashMap> components;
}
