package com.techsophy.tsf.runtime.form.dto;

import lombok.Value;
import lombok.With;
import java.util.List;
import java.util.Map;

@With
@Value
public class AggregationResponse
{
    List<Map<String,String>> content;
}
