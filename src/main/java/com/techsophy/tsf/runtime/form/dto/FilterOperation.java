package com.techsophy.tsf.runtime.form.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({@JsonSubTypes.Type(value = EqualsOperation.class),
        @JsonSubTypes.Type(value = ComparatorOperation.class),
        @JsonSubTypes.Type(value = InOperation.class),
        @JsonSubTypes.Type(value = LikeOperation.class)})
public interface FilterOperation extends MongoCriteriaTransformer
{
}
