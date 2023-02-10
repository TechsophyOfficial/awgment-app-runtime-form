package com.techsophy.tsf.runtime.form.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techsophy.idgenerator.IdGeneratorImpl;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.FormDataSchema;
import com.techsophy.tsf.runtime.form.exception.FormIdNotFoundException;
import com.techsophy.tsf.runtime.form.exception.InvalidInputException;
import com.techsophy.tsf.runtime.form.exception.UserDetailsIdNotFoundException;
import com.techsophy.tsf.runtime.form.service.FormService;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import com.techsophy.tsf.runtime.form.utils.UserDetails;
import com.techsophy.tsf.runtime.form.utils.WebClientWrapper;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.STRING;

@ExtendWith(MockitoExtension.class)
class FormDataServiceImplTest
{
    @Mock
    UserDetails userDetails;
    @Mock
    TokenUtils tokenUtils;
    @Mock
    WebClientWrapper webClientWrapper;
    @Mock
    FormService formService;
    @Mock
    IdGeneratorImpl idGenerator;
    @Mock
    GlobalMessageSource globalMessageSource;
    @Mock
    MongoTemplate mongoTemplate;
    @Mock
    Document document;
    @InjectMocks
    FormDataServiceImpl formDataService;

    @Test
    void saveFormDataTestWhileUserIdIsEmpty() throws JsonProcessingException
    {
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        map.put(STRING,STRING);
        FormDataSchema formDataSchema = new FormDataSchema("1","1",1,map,map);
        Mockito.when(userDetails.getUserDetails()).thenReturn(List.of(Map.of("id","")));
        Assertions.assertThrows(UserDetailsIdNotFoundException.class, () -> formDataService.saveFormData(formDataSchema));
    }

    @Test
    void updateFormDataTestWhileIdIsEmpty()
    {
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        map.put(STRING,STRING);
        FormDataSchema formDataSchema = new FormDataSchema("","1",1,map,map);
        Assertions.assertThrows(InvalidInputException.class, () -> formDataService.updateFormData(formDataSchema));
    }

    @Test
    void updateFormDataTestWhileFormIdIsEmpty()
    {
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        map.put(STRING,STRING);
        FormDataSchema formDataSchema = new FormDataSchema("1","",1,map,map);
        Assertions.assertThrows(InvalidInputException.class, () -> formDataService.updateFormData(formDataSchema));
    }

    @Test
    void updateFormDataTestWhileFormIdNotFound()
    {
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        map.put(STRING,STRING);
        map.put("components","value");
        FormDataSchema formDataSchema = new FormDataSchema("1","1",1,map,map);
        Assertions.assertThrows(FormIdNotFoundException.class, () -> formDataService.updateFormData(formDataSchema));
    }
}