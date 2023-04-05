package com.techsophy.tsf.runtime.form.config;

import com.techsophy.idgenerator.IdGeneratorImpl;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import java.util.List;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;

@Configuration
@EnableTransactionManagement
public class ApplicationConfig
{
    @Bean
    public IdGeneratorImpl idGeneratorImpl()
    {
        return new IdGeneratorImpl();
    }

    @Value(GATEWAY_URL)
    String gatewayUrl;
    @Bean
    public OpenAPI customOpenAPI()
    {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info().title(RUNTIME_FORM).version(VERSION_1).description(RUNTIME_FORM_MODELER_API_VERSION_1))
                .servers( List.of(new Server().url(gatewayUrl)));
    }

    @Bean
    public MongoTransactionManager transactionManager(MongoTemplate mongoTemplate)
    {
        return new MongoTransactionManager(mongoTemplate.getMongoDatabaseFactory());
    }
}
