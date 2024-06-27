package com.techsophy.tsf.runtime.form.config;
import com.mongodb.client.MongoClient;
import com.techsophy.multitenancy.mongo.config.TenantContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
@Component
public class MongoConfig {
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        return new MongoTemplate(mongoClient, TenantContext.getTenantId()); // Customize with your MongoClient and database name
    }
}
