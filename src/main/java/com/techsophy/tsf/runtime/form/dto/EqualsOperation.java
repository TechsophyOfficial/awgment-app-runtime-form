package com.techsophy.tsf.runtime.form.dto;

import lombok.Data;
import org.springframework.data.mongodb.core.query.Criteria;
import static com.techsophy.tsf.runtime.form.constants.FormDataConstants.CONTAINS_ONLY_NUMBER;

@Data
public class EqualsOperation implements FilterOperation
{
    private String equals;

    @Override
    public Criteria getCriteria(String field)
    {
       return Criteria.where(field).is(this.equals.matches(CONTAINS_ONLY_NUMBER)
       ?Long.parseLong(this.equals):this.equals);
    }
}
