package com.techsophy.tsf.runtime.form.dto;

import lombok.Data;
import java.util.Map;

@Data
public class Filters
{
    private Map<String, FilterOperation> operations;
}
