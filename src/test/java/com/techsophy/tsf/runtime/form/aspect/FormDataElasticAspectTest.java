package com.techsophy.tsf.runtime.form.aspect;

import com.techsophy.tsf.runtime.form.dto.FormResponseSchema;
import com.techsophy.tsf.runtime.form.entity.FormDataDefinition;
import com.techsophy.tsf.runtime.form.entity.Status;
import com.techsophy.tsf.runtime.form.service.FormDataElasticService;
import com.techsophy.tsf.runtime.form.service.FormService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.TEST_FORM_ID;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.TEST_ID;

@ExtendWith(MockitoExtension.class)
class FormDataElasticAspectTest
{
    @Mock
    FormService mockFormService;
    @Mock
    FormDataElasticService formDataElasticService;
    @InjectMocks
    FormDataElasticAspectHandler formDataElasticAspectHandler;
    @Test
    void callElasticServiceTest()
    {
        FormDataDefinition formDataDefinition=new FormDataDefinition();
        formDataDefinition.setFormId(TEST_FORM_ID);
        formDataDefinition.setId(TEST_ID);
        FormResponseSchema formResponseSchema=new FormResponseSchema();
        formResponseSchema.setElasticPush(Status.ENABLED);
        Mockito.when(mockFormService.getRuntimeFormById(Mockito.anyString())).thenReturn(formResponseSchema);
        formDataElasticAspectHandler.afterControllerAdvice(formDataDefinition);
        Mockito.verify(formDataElasticService,Mockito.times(1)).saveOrUpdateToElastic(Mockito.any());
    }
}
