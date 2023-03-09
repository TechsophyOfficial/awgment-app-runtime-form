package com.techsophy.tsf.runtime.form.dto;

import lombok.Data;
import org.springframework.data.mongodb.core.query.Criteria;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class ComparatorOperation implements FilterOperation
{
    private Integer lt;
    private Integer lte;
    private Integer gt;
    private Integer gte;

    @Override
    public Criteria getCriteria(String field)
    {
        return new Criteria()
                .andOperator(
                        Stream.of(
                                        this.lt != null ? Criteria.where(field).lt(this.lt) : null,
                                        this.gt != null ? Criteria.where(field).gt(this.gt) : null,
                                        this.lte != null ? Criteria.where(field).lte(this.lte) : null,
                                        this.gte != null ? Criteria.where(field).gte(this.gte) : null
                                )
                                .filter(Objects::nonNull).collect(Collectors.toList())
                );
    }
}
