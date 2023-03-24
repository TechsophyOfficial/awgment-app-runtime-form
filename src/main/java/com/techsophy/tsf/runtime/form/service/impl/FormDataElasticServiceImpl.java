package com.techsophy.tsf.runtime.form.service.impl;

import com.techsophy.tsf.runtime.form.entity.FormDataDefinition;
import com.techsophy.tsf.runtime.form.exception.ExternalServiceErrorException;
import com.techsophy.tsf.runtime.form.service.FormDataElasticService;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import com.techsophy.tsf.runtime.form.utils.WebClientWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FormDataElasticServiceImpl implements FormDataElasticService
{
    private final WebClientWrapper webClientWrapper;
    private final TokenUtils tokenUtils;
    @Value(GATEWAY_URI)
    private final String gatewayApi;
    @Value(ELASTIC_SOURCE)
    private final boolean elasticSource;

    @Override
    public void saveOrUpdateToElastic(FormDataDefinition formDataDefinition)
    {
        try {
            webClientWrapper.webclientRequest(webClientWrapper.createWebClient(tokenUtils.getTokenFromContext()),
                    gatewayApi+ELASTIC_VERSION1+SLASH+ TP_RUNTIME_FORM_DATA +formDataDefinition.getFormId() +PARAM_SOURCE+elasticSource,POST,
                    formDataDefinition);
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
            throw new ExternalServiceErrorException("Elastic Search pod is down","Elastic Search pod is down");
        }
    }
}
