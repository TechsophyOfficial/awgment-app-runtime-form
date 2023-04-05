package com.techsophy.tsf.runtime.form.service.impl;

import com.techsophy.tsf.commons.user.UserDetails;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.ELasticAcl;
import com.techsophy.tsf.runtime.form.entity.FormDataDefinition;
import com.techsophy.tsf.runtime.form.exception.ExternalServiceErrorException;
import com.techsophy.tsf.runtime.form.exception.InvalidInputException;
import com.techsophy.tsf.runtime.form.service.FormDataElasticService;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import com.techsophy.tsf.runtime.form.utils.WebClientWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.techsophy.tsf.runtime.form.constants.ErrorConstants.TOKEN_NOT_NULL;
import static com.techsophy.tsf.runtime.form.constants.FormDataConstants.ACL;
import static com.techsophy.tsf.runtime.form.constants.FormDataConstants.ELASTIC;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;

@Service
@Data
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class FormDataElasticServiceImpl implements FormDataElasticService
{
    private  WebClientWrapper webClientWrapper;
    private  TokenUtils tokenUtils;
    @Value(GATEWAY_URI)
    private  String gatewayApi;
    @Value(ELASTIC_SOURCE)
    private  boolean elasticSource;
    @Value(ELASTIC_ENABLE)
    private  boolean elasticEnable;
    private  UserDetails userDetails;
    private GlobalMessageSource globalMessageSource;

    @Override
    public void saveOrUpdateToElastic(FormDataDefinition formDataDefinition)
    {
        if(elasticEnable) {
            try {
                webClientWrapper.webclientRequest(webClientWrapper.createWebClient(tokenUtils.getTokenFromContext()),
                        gatewayApi + ELASTIC_VERSION1 + SLASH + TP_RUNTIME_FORM_DATA + formDataDefinition.getFormId() + PARAM_SOURCE + elasticSource, POST,
                        formDataDefinition);
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new ExternalServiceErrorException("Elastic Search pod is down", "Elastic Search pod is down");
            }
        }
    }

    @Override
    public void saveACL(ELasticAcl eLasticAcl) {
        if(elasticEnable) {
            String token = userDetails.getToken().orElseThrow(()->new InvalidInputException(TOKEN_NOT_NULL,globalMessageSource.get(TOKEN_NOT_NULL, String.valueOf(userDetails.getUserId()))));
            var client = webClientWrapper.createWebClient(token);
            webClientWrapper.webclientRequest(client, gatewayApi + ELASTIC + VERSION_V1 + ACL, POST, eLasticAcl);
        }
    }

    @Override
    public void deleteACL(String indexName)
    {
        if(elasticEnable) {
            String token = userDetails.getToken().orElseThrow(()->new InvalidInputException(TOKEN_NOT_NULL,globalMessageSource.get(TOKEN_NOT_NULL, String.valueOf(userDetails.getUserId()))));
            var client = webClientWrapper.createWebClient(token);
            webClientWrapper.webclientRequest(client, gatewayApi + ELASTIC + VERSION_V1 + SLASH + indexName + ACL, DELETE, null);
        }
    }
     public static String formIdToIndexName(String id)
     {
         return TP_RUNTIME_FORM_DATA+id;
     }
}
