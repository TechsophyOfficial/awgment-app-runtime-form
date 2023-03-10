package com.techsophy.tsf.runtime.form.dto;

import lombok.Data;
import org.springframework.data.mongodb.core.query.Criteria;
import java.util.List;

@Data
public class InOperation  implements FilterOperation
{
    List<Object> in;

    @Override
    public Criteria getCriteria(String field)
    {
        return Criteria.where(field).in(this.in);
    }
}
