package com.techsophy.tsf.runtime.form.repository;

import com.techsophy.tsf.runtime.form.entity.FormDefinition;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Repository
public interface FormDefinitionRepository extends MongoRepository<FormDefinition, Long>, FormDefinitionCustomRepository
{
    Optional<FormDefinition> findById(BigInteger formKey);
    List<FormDefinition> findByType(String type);
    boolean existsById(BigInteger formKey);
    int deleteById(BigInteger formKey);
}
