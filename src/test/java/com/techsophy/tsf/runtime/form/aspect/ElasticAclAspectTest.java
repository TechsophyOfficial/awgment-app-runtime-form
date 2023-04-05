package com.techsophy.tsf.runtime.form.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techsophy.tsf.runtime.form.dto.ELasticAcl;
import com.techsophy.tsf.runtime.form.dto.FormAclDto;
import com.techsophy.tsf.runtime.form.service.impl.FormDataElasticServiceImpl;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import com.techsophy.tsf.runtime.form.utils.WebClientWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
 class ElasticAclAspectTest {

    @Mock
    WebClientWrapper webClientWrapper;
    @Mock
    TokenUtils tokenUtils;
    @Mock
    ObjectMapper objectMapper;
    @Mock
    FormDataElasticServiceImpl formDataElasticService;
    @InjectMocks
    ElasticAclAspect elasticAclAspect;

    @Test
      void afterSaveFormAclController()
    {
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setId("12");
        formAclDto.setFormId("23131");
        formAclDto.setAclId("34");
        ELasticAcl eLasticAcl = new ELasticAcl();
        eLasticAcl.setId("12");
        eLasticAcl.setIndexName("tp_runtime_form_data_23131");
        eLasticAcl.setAclId("34");
        Mockito.when(objectMapper.convertValue(any(),eq(ELasticAcl.class))).thenReturn(eLasticAcl);
        elasticAclAspect.afterSaveFormAclController(formAclDto);
        verify(formDataElasticService,times(1)).saveACL(eLasticAcl);

    }
    @Test
     void afterDeleteFormAclController()
    {
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setId("12");
        formAclDto.setFormId("1234");
        formAclDto.setAclId("34");
        elasticAclAspect.afterDeleteFormAclController("123");
        verify(formDataElasticService,times(1)).deleteACL("tp_runtime_form_data_123");

    }
}
