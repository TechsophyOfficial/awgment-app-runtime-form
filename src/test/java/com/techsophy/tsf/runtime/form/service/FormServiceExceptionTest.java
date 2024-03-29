package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techsophy.idgenerator.IdGeneratorImpl;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.config.LocaleConfig;
import com.techsophy.tsf.runtime.form.dto.FormSchema;
import com.techsophy.tsf.runtime.form.exception.EntityIdNotFoundException;
import com.techsophy.tsf.runtime.form.exception.FormIdNotFoundException;
import com.techsophy.tsf.runtime.form.repository.FormDefinitionRepository;
import com.techsophy.tsf.runtime.form.service.impl.FormServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigInteger;
import java.util.*;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class FormServiceExceptionTest
{
    @Mock
    FormDefinitionRepository mockFormDefinitionRepository;
    @Mock
    ObjectMapper mockObjectMapper;
    @Mock
    IdGeneratorImpl mockIdGenerator;
    @Mock
    GlobalMessageSource mockGlobalMessageSource;
    @Mock
    LocaleConfig mockLocaleConfig;
    @InjectMocks
    FormServiceImpl mockFormServiceImpl;

    @Test
    void saveRuntimeFormExceptionTest()
    {
        List<Map<String,Object>> list=new ArrayList<>();
        Map<String,Object> map=new HashMap<>();
        map.put("create","true");
        list.add(map);
        FormSchema formSchemaTest = new FormSchema(null, TEST_NAME, TEST_COMPONENTS,list, TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE);
        Assertions.assertThrows(NullPointerException.class, () ->
                mockFormServiceImpl.saveRuntimeForm(formSchemaTest));
    }

    @Test
    void getRuntimeFormByIdExceptionTest()
    {
        List<Map<String,Object>> list=new ArrayList<>();
        Map<String,Object> map=new HashMap<>();
        map.put("create","true");
        list.add(map);
        when(mockFormDefinitionRepository.findById(BigInteger.valueOf(Long.parseLong(String.valueOf(1))))).thenReturn(Optional.empty());
        Assertions.assertThrows(FormIdNotFoundException.class,()-> mockFormServiceImpl.getRuntimeFormById(TEST_ID));
    }

    @Test
    void deleteRuntimeFormByIdExceptionTest()
    {
        when(mockFormDefinitionRepository.existsById(BigInteger.valueOf(1))).thenReturn(false);
        Assertions.assertThrows(EntityIdNotFoundException.class,()->
                mockFormServiceImpl.deleteRuntimeFormById(TEST_ID));
    }
}
