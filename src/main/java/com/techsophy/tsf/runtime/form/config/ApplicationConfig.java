package com.techsophy.tsf.runtime.form.config;

import com.techsophy.idgenerator.IdGeneratorImpl;
import com.techsophy.tsf.commons.acl.ACLEvaluation;
import com.techsophy.tsf.commons.acl.ACLEvaluatorImpl;
import com.techsophy.tsf.commons.user.UserDetails;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;

@Configuration
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

    @Bean("CommonUserDetails")
    public UserDetails userDetails(String gatewayUrl)
    {
        return new UserDetails(gatewayUrl);
    }

    @Bean
    public ACLEvaluation aclEvaluator()
    {
        return new ACLEvaluatorImpl(gatewayUrl);
    }
}
