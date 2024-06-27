package com.techsophy.tsf.runtime.form.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.data.mongodb.core.MongoTemplate;

@Component
public class TenantScopedMongoTemplate {
  private final MongoTemplate mongoTemplate;
  @Autowired
  public TenantScopedMongoTemplate(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }
  public MongoTemplate getMongoTemplate() {
    return mongoTemplate;
  }
}
