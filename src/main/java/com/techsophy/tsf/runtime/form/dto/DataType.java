package com.techsophy.tsf.runtime.form.dto;

import lombok.Data;

import java.util.List;

@Data
public class DataType
{
    List<Values> values;
    String resource;
    String json;
    String url;
    String custom;
}
