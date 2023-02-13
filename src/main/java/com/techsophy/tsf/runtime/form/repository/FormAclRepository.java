package com.techsophy.tsf.runtime.form.repository;

import com.techsophy.tsf.runtime.form.entity.FormAclEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface FormAclRepository extends MongoRepository<FormAclEntity, BigInteger> {
}
