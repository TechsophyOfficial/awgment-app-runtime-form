package com.techsophy.tsf.runtime.form.repository.impl;

import com.techsophy.tsf.runtime.form.entity.SequenceGeneratorDefinition;
import com.techsophy.tsf.runtime.form.repository.SequenceGeneratorCustomRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import static com.techsophy.tsf.runtime.form.constants.SequenceGeneratorConstants.LENGTH;
import static com.techsophy.tsf.runtime.form.constants.SequenceGeneratorConstants.SEQUENCE_NAME;

@AllArgsConstructor
public class SequenceGeneratorCustomRepositoryImpl implements SequenceGeneratorCustomRepository
{
    private MongoTemplate mongoTemplate;

    @Override
    public boolean existsBySequenceNameAndLength(String sequenceName, int length)
    {
        Query query=new Query();
        query.addCriteria(new Criteria().andOperator(Criteria.where(SEQUENCE_NAME).is(sequenceName),Criteria.where(LENGTH).is(length)));
        return mongoTemplate.exists(query, SequenceGeneratorDefinition.class);
    }

    @Override
    public SequenceGeneratorDefinition findBySequenceNameAndLength(String sequenceName, int length)
    {
        Query query=new Query();
        query.addCriteria(new Criteria().andOperator(Criteria.where(SEQUENCE_NAME).is(sequenceName),Criteria.where(LENGTH).is(length)));
        return mongoTemplate.findOne(query, SequenceGeneratorDefinition.class);
    }
}
