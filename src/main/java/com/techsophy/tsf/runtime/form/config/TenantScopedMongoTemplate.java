package com.techsophy.tsf.runtime.form.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.techsophy.multitenancy.mongo.config.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TenantScopedMongoTemplate {

  private final String tenantId;
  private final MongoTemplate mongoTemplate;

  @Autowired
  public TenantScopedMongoTemplate(TenantContext tenantContext, MongoClient mongoClient) {
    this.tenantId = tenantContext.getTenantId();
    this.mongoTemplate = createMongoTemplate(mongoClient, this.tenantId);
  }

  public MongoTemplate getMongoTemplate() {
    return mongoTemplate;
  }

  private MongoTemplate createMongoTemplate(MongoClient mongoClient, String tenantId) {
    // Implement connection pooling or reuse existing MongoTemplate instances here
    MongoDatabase db = mongoClient.getDatabase(tenantId);
    return new MongoTemplate(mongoClient, db.getName());
  }
}
