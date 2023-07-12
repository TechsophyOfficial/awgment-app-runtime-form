package com.techsophy.tsf.runtime.form.service;

import com.techsophy.tsf.commons.query.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
@Service
public class MongoQueryBuilder implements QueryBuilder<Criteria> {

    @Override
    public Criteria equalsQuery(String key, EqualsOperation operation) {
        return Criteria.where(key).is(operation.getEquals());
    }

    @Override
    public Criteria comparatorQuery(String key, ComparatorOperation operation) {
        return  new Criteria()
                .andOperator(
                        Stream.of(
                                        operation.getLt() != null ? Criteria.where(key).lt(operation.getLt()) : null,
                                        operation.getGt() != null ? Criteria.where(key).gt(operation.getGt()) : null,
                                        operation.getLte() != null ? Criteria.where(key).lte(operation.getLte()) : null,
                                        operation.getGte() != null ? Criteria.where(key).gte(operation.getGte()) : null
                                )
                                .filter(Objects::nonNull).collect(Collectors.toList())
                );    }

    @Override
    public Criteria inQuery(String key, InOperation operation) {
        return Criteria.where(key).in(operation.getIn());    }



    @Override
    public Criteria likeQuery(String key, LikeOperation operation) {
        return Criteria.where(key).regex(operation.getLike());
    }

    @Override
    public Criteria orQueries(List<Criteria> queries) {
        return new Criteria().orOperator(queries);
    }

    public Criteria andQueries(List<Criteria> queries) {
        return new Criteria().andOperator(queries);
    }

}
