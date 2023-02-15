package com.techsophy.tsf.runtime.form.repository;

import com.techsophy.tsf.runtime.form.entity.FormAclEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Optional;

@Repository
public interface FormAclRepository extends MongoRepository<FormAclEntity, String> {
    Optional<FormAclEntity> findByFormId(String formId);

}
