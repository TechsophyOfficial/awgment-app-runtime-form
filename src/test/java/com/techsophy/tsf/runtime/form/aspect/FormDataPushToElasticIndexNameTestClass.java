package com.techsophy.tsf.runtime.form.aspect;

import com.techsophy.tsf.runtime.form.dto.FormAclDto;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import com.techsophy.tsf.runtime.form.utils.WebClientWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class FormDataPushToElasticIndexNameTestClass {

    @Mock
    WebClientWrapper webClientWrapper;
    @Mock
    TokenUtils tokenUtils;
    @InjectMocks
    FormDataPushToElasticIndexName formDataPushToElasticIndexName;

    @Test
     public void afterSaveFormAclController()
    {
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setId("12");
        formAclDto.setFormId("23131");
        formAclDto.setAclId("34");
        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("abc");
        WebClient webClient= WebClient.builder().build();
        when(webClientWrapper.createWebClient(any())).thenReturn(webClient);
        when(webClientWrapper.webclientRequest(any(),anyString(),any(),any())).thenReturn("abc");
        formDataPushToElasticIndexName.afterSaveFormAclController(formAclDto);
        verify(webClientWrapper,times(1)).webclientRequest(any(),anyString(),any(),any());
    }
    @Test
    public void afterDeleteFormAclController()
    {
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setId("12");
        formAclDto.setFormId("1234");
        formAclDto.setAclId("34");
        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("abc");
        WebClient webClient= WebClient.builder().build();
        when(webClientWrapper.createWebClient(any())).thenReturn(webClient);
        when(webClientWrapper.webclientRequest(any(),anyString(),any(),any())).thenReturn("abc");
        formDataPushToElasticIndexName.afterDeleteFormAclController("1234");
        verify(webClientWrapper,times(1)).webclientRequest(any(),anyString(),any(),any());
    }
}
