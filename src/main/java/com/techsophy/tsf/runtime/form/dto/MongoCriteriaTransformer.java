package com.techsophy.tsf.runtime.form.dto;

import org.springframework.data.mongodb.core.query.Criteria;

public interface MongoCriteriaTransformer
{
    Criteria getCriteria(String field);
}
