package com.techsophy.tsf.runtime.form.repository;

import com.techsophy.tsf.runtime.form.entity.SequenceGeneratorDefinition;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SequenceGeneratorRepository extends MongoRepository<SequenceGeneratorDefinition,Long>, SequenceGeneratorCustomRepository
{

}
