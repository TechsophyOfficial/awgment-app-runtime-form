package com.techsophy.tsf.runtime.form.service;

import com.techsophy.tsf.commons.user.UserDetails;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.ELasticAcl;
import com.techsophy.tsf.runtime.form.entity.FormDataDefinition;
import com.techsophy.tsf.runtime.form.exception.InvalidInputException;
import com.techsophy.tsf.runtime.form.service.impl.FormDataElasticServiceImpl;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import com.techsophy.tsf.runtime.form.utils.WebClientWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class FormDataElasticServiceTest
{
    @Mock
    private TokenUtils mockTokenUtils;
    @Mock
    private WebClientWrapper mockWebClientWrapper;
    @Mock
    private UserDetails userDetails;
    @Mock
    GlobalMessageSource globalMessageSource;
    @InjectMocks
    FormDataElasticServiceImpl formDataElasticService;

    @BeforeEach public void setUp() {
        ReflectionTestUtils.setField(formDataElasticService,"elasticEnable",true); }
    @Test
    void saveOrUpdateToElasticTest()
    {
        FormDataDefinition formDataDefinition=new FormDataDefinition();
        formDataElasticService.saveOrUpdateToElastic(formDataDefinition);
        Mockito.verify(mockWebClientWrapper,Mockito.times(1)).webclientRequest(any(),anyString(),anyString(),any());
    }

    @Test
    void saveOrUpdateToElasticExceptionTest()
    {
        FormDataDefinition formDataDefinition=new FormDataDefinition();
        when(mockWebClientWrapper.webclientRequest(any(),anyString(),anyString(),any())).thenThrow(RuntimeException.class);
        Assertions.assertThrows(RuntimeException.class,()->formDataElasticService.saveOrUpdateToElastic(formDataDefinition));
    }
    @Test
    void saveACL()
    {
        ELasticAcl eLasticAcl = new ELasticAcl();
        eLasticAcl.setId("12");
        eLasticAcl.setIndexName("tp_runtime_form_data_12");
        eLasticAcl.setAclId("123");
        WebClient webClient= WebClient.builder().build();
        when(mockWebClientWrapper.createWebClient(any())).thenReturn(webClient);
        when(mockWebClientWrapper.webclientRequest(any(),anyString(),any(),any())).thenReturn("abc");
        when(userDetails.getToken()).thenReturn(Optional.of("abc"));
        formDataElasticService.saveACL(eLasticAcl);
        Mockito.verify(mockWebClientWrapper,Mockito.times(1)).webclientRequest(any(),anyString(),any(),any());
    }
    @Test
    void saveACLExceptionIfTokenNull()
    {
        ELasticAcl eLasticAcl = new ELasticAcl();
        eLasticAcl.setId("12");
        eLasticAcl.setIndexName("tp_runtime_form_data_12");
        eLasticAcl.setAclId("123");
        Mockito.when(globalMessageSource.get(anyString(),anyString())).thenReturn("abc");
        when(userDetails.getToken()).thenReturn(Optional.empty());
        Assertions.assertThrows(InvalidInputException.class,()->formDataElasticService.saveACL(eLasticAcl));
    }
    @Test
    void deleteACL()
    {
        WebClient webClient= WebClient.builder().build();
        when(mockWebClientWrapper.createWebClient(any())).thenReturn(webClient);
        when(mockWebClientWrapper.webclientRequest(any(),anyString(),any(),any())).thenReturn("abc");
        when(userDetails.getToken()).thenReturn(Optional.of("abc"));
        formDataElasticService.deleteACL("tp_runtime_form_data_12");
        Mockito.verify(mockWebClientWrapper,Mockito.times(1)).webclientRequest(any(),anyString(),any(),any());
    }
    @Test
    void deleteACLExceptionIfTokenNull()
    {
        when(userDetails.getToken()).thenReturn(Optional.empty());
        Assertions.assertThrows(InvalidInputException.class,()->formDataElasticService.deleteACL("tp_runtime_form_data_12"));
    }
}
