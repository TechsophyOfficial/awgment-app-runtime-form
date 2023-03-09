package com.techsophy.tsf.runtime.form.dto;

import lombok.Data;
import java.util.Map;

@Data
public class FilterDTO
{
    Map<String, FilterOperation> comparatorFields;
    Map<String, FilterOperation> inOperationFields;
    Map<String, FilterOperation> likeOperationFields;
}
