package com.techsophy.tsf.runtime.form.dto;

import lombok.Data;
import org.springframework.data.mongodb.core.query.Criteria;

@Data
public class EqualsOperation implements FilterOperation
{
    private Object equals;

    @Override
    public Criteria getCriteria(String field)
    {
       return Criteria.where(field).is(this.equals);
    }
}
