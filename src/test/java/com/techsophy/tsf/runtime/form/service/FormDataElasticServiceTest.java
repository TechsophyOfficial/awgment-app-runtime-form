package com.techsophy.tsf.runtime.form.service;

import com.techsophy.tsf.runtime.form.entity.FormDataDefinition;
import com.techsophy.tsf.runtime.form.service.impl.FormDataElasticServiceImpl;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import com.techsophy.tsf.runtime.form.utils.WebClientWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith({MockitoExtension.class})
class FormDataElasticServiceTest
{
    @Mock
    private TokenUtils mockTokenUtils;
    @Mock
    private WebClientWrapper mockWebClientWrapper;
    private final String gatewayURL="http://localhost:8080";
    private final boolean elasticSource=true;

    @Test
    void saveOrUpdateToElasticTest()
    {
        FormDataElasticServiceImpl mockFormDataElasticServiceImpl=
                new FormDataElasticServiceImpl(mockWebClientWrapper,mockTokenUtils,gatewayURL,elasticSource);
        FormDataDefinition formDataDefinition=new FormDataDefinition();
        mockFormDataElasticServiceImpl.saveOrUpdateToElastic(formDataDefinition);
        Mockito.verify(mockWebClientWrapper,Mockito.times(1)).webclientRequest(any(),anyString(),anyString(),any());
    }

    @Test
    void saveOrUpdateToElasticExceptionTest()
    {
        FormDataElasticServiceImpl mockFormDataElasticServiceImpl=
                new FormDataElasticServiceImpl(mockWebClientWrapper,mockTokenUtils,gatewayURL,elasticSource);
        FormDataDefinition formDataDefinition=new FormDataDefinition();
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),anyString(),anyString(),any())).thenThrow(RuntimeException.class);
        Assertions.assertThrows(RuntimeException.class,()->mockFormDataElasticServiceImpl.saveOrUpdateToElastic(formDataDefinition));
    }
}
