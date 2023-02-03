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
class FormDataServiceImplTest {

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
//    @Mock
//    ValidationCheckServiceImpl validationCheckService;
    @Mock
    GlobalMessageSource globalMessageSource;
    @Mock
    MongoTemplate mongoTemplate;
    @Mock
    Document document;
    @InjectMocks
    FormDataServiceImpl formDataService;

//    @Test
//    void saveFormDataTestWhileFormIdIsEmpty()
//    {
//        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
//        map.put(STRING,STRING);
//        FormDataSchema formDataSchema = new FormDataSchema("1","",1,map,map);
//        Assertions.assertThrows(InvalidInputException.class, () -> formDataService.saveFormData(formDataSchema));
//    }

    @Test
    void saveFormDataTestWhileUserIdIsEmpty() throws JsonProcessingException {
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        map.put(STRING,STRING);
        FormDataSchema formDataSchema = new FormDataSchema("1","1",1,map,map);
        Mockito.when(userDetails.getUserDetails()).thenReturn(List.of(Map.of("id","")));
        Assertions.assertThrows(UserDetailsIdNotFoundException.class, () -> formDataService.saveFormData(formDataSchema));
    }

//    @Test
//    void saveFormDataTestWhileThrowsInvalidInputException() throws JsonProcessingException {
//        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
//        map.put(STRING,STRING);
//        FormDataSchema formDataSchema = new FormDataSchema("1","1",1,map,map);
//        Mockito.when(userDetails.getUserDetails()).thenReturn(List.of(Map.of("id","1")));
//        Assertions.assertThrows(InvalidInputException.class, () -> formDataService.saveFormData(formDataSchema));
//    }

//    @Test
//    void saveFormDataTestCase0() throws JsonProcessingException {
//        FormResponseSchema formResponseSchema = Mockito.mock(FormResponseSchema.class);
//        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
//        map.put(STRING,STRING);
//        map.put("components","value");
//        FormDataSchema formDataSchema = new FormDataSchema("1","1",1,map,map);
//        Mockito.when(userDetails.getUserDetails()).thenReturn(List.of(Map.of("id","0")));
//        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("token");
//        Mockito.when(formService.getRuntimeFormById(any())).thenReturn(formResponseSchema);
//        Mockito.when(formResponseSchema.getComponents()).thenReturn(map);
//        Mockito.when(validationCheckService.allFieldsValidations(any(), any(), anyString(), anyString())).thenReturn(List.of("0","1"));
//        Assertions.assertThrows(InvalidInputException.class, () -> formDataService.saveFormData(formDataSchema));
//    }

//    @Test
//    void saveFormDataTestCase1() throws JsonProcessingException {
//        FormResponseSchema formResponseSchema = Mockito.mock(FormResponseSchema.class);
//        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
//        map.put(STRING,STRING);
//        map.put("components","value");
//        FormDataSchema formDataSchema = new FormDataSchema("1","1",1,map,map);
//        Mockito.when(userDetails.getUserDetails()).thenReturn(List.of(Map.of("id","0")));
//        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("token");
//        Mockito.when(formService.getRuntimeFormById(any())).thenReturn(formResponseSchema);
//        Mockito.when(formResponseSchema.getComponents()).thenReturn(map);
//        Mockito.when(validationCheckService.allFieldsValidations(any(), any(), anyString(), anyString())).thenReturn(List.of("1","2"));
//        Assertions.assertThrows(InvalidInputException.class, () -> formDataService.saveFormData(formDataSchema));
//    }

//    @Test
//    void saveFormDataTestCase2() throws JsonProcessingException {
//        FormResponseSchema formResponseSchema = Mockito.mock(FormResponseSchema.class);
//        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
//        map.put(STRING,STRING);
//        map.put("components","value");
//        FormDataSchema formDataSchema = new FormDataSchema("1","1",1,map,map);
//        Mockito.when(userDetails.getUserDetails()).thenReturn(List.of(Map.of("id","0")));
//        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("token");
//        Mockito.when(formService.getRuntimeFormById(any())).thenReturn(formResponseSchema);
//        Mockito.when(formResponseSchema.getComponents()).thenReturn(map);
//        Mockito.when(validationCheckService.allFieldsValidations(any(), any(), anyString(), anyString())).thenReturn(List.of("2","3"));
//        Assertions.assertThrows(InvalidInputException.class, () -> formDataService.saveFormData(formDataSchema));
//    }

//    @Test
//    void saveFormDataTestCase3() throws JsonProcessingException {
//        FormResponseSchema formResponseSchema = Mockito.mock(FormResponseSchema.class);
//        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
//        map.put(STRING,STRING);
//        map.put("components","value");
//        FormDataSchema formDataSchema = new FormDataSchema("1","1",1,map,map);
//        Mockito.when(userDetails.getUserDetails()).thenReturn(List.of(Map.of("id","0")));
//        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("token");
//        Mockito.when(formService.getRuntimeFormById(any())).thenReturn(formResponseSchema);
//        Mockito.when(formResponseSchema.getComponents()).thenReturn(map);
//        Mockito.when(validationCheckService.allFieldsValidations(any(), any(), anyString(), anyString())).thenReturn(List.of("3","5","6"));
//        Assertions.assertThrows(InvalidInputException.class, () -> formDataService.saveFormData(formDataSchema));
//    }

//    @Test
//    void saveFormDataTestCase5() throws JsonProcessingException {
//        FormResponseSchema formResponseSchema = Mockito.mock(FormResponseSchema.class);
//        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
//        map.put(STRING,STRING);
//        map.put("components","value");
//        FormDataSchema formDataSchema = new FormDataSchema("1","1",1,map,map);
//        Mockito.when(userDetails.getUserDetails()).thenReturn(List.of(Map.of("id","0")));
//        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("token");
//        Mockito.when(formService.getRuntimeFormById(any())).thenReturn(formResponseSchema);
//        Mockito.when(formResponseSchema.getComponents()).thenReturn(map);
//        Mockito.when(validationCheckService.allFieldsValidations(any(), any(), anyString(), anyString())).thenReturn(List.of("5","6"));
//        Assertions.assertThrows(InvalidInputException.class, () -> formDataService.saveFormData(formDataSchema));
//    }

//    @Test
//    void saveFormDataTestCase6() throws JsonProcessingException {
//        FormResponseSchema formResponseSchema = Mockito.mock(FormResponseSchema.class);
//        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
//        map.put(STRING,STRING);
//        map.put("components","value");
//        FormDataSchema formDataSchema = new FormDataSchema("1","1",1,map,map);
//        Mockito.when(userDetails.getUserDetails()).thenReturn(List.of(Map.of("id","0")));
//        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("token");
//        Mockito.when(formService.getRuntimeFormById(any())).thenReturn(formResponseSchema);
//        Mockito.when(formResponseSchema.getComponents()).thenReturn(map);
//        Mockito.when(validationCheckService.allFieldsValidations(any(), any(), anyString(), anyString())).thenReturn(List.of("6","7"));
//        Assertions.assertThrows(InvalidInputException.class, () -> formDataService.saveFormData(formDataSchema));
//    }

//    @Test
//    void saveFormDataTestCase7() throws JsonProcessingException {
//        FormResponseSchema formResponseSchema = Mockito.mock(FormResponseSchema.class);
//        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
//        map.put(STRING,STRING);
//        map.put("components","value");
//        FormDataSchema formDataSchema = new FormDataSchema("1","1",1,map,map);
//        Mockito.when(userDetails.getUserDetails()).thenReturn(List.of(Map.of("id","0")));
//        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("token");
//        Mockito.when(formService.getRuntimeFormById(any())).thenReturn(formResponseSchema);
//        Mockito.when(formResponseSchema.getComponents()).thenReturn(map);
//        Mockito.when(validationCheckService.allFieldsValidations(any(), any(), anyString(), anyString())).thenReturn(List.of("7","8"));
//        Assertions.assertThrows(InvalidInputException.class, () -> formDataService.saveFormData(formDataSchema));
//    }

//    @Test
//    void saveFormDataTestCase8() throws JsonProcessingException {
//        FormResponseSchema formResponseSchema = Mockito.mock(FormResponseSchema.class);
//        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
//        map.put(STRING,STRING);
//        map.put("components","value");
//        FormDataSchema formDataSchema = new FormDataSchema("1","1",1,map,map);
//        Mockito.when(userDetails.getUserDetails()).thenReturn(List.of(Map.of("id","0")));
//        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("token");
//        Mockito.when(formService.getRuntimeFormById(any())).thenReturn(formResponseSchema);
//        Mockito.when(formResponseSchema.getComponents()).thenReturn(map);
//        Mockito.when(validationCheckService.allFieldsValidations(any(), any(), anyString(), anyString())).thenReturn(List.of("8","9"));
//        Assertions.assertThrows(InvalidInputException.class, () -> formDataService.saveFormData(formDataSchema));
//    }

//    @Test
//    void saveFormDataTestCase9() throws JsonProcessingException
//    {
//        FormResponseSchema formResponseSchema = Mockito.mock(FormResponseSchema.class);
//        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
//        map.put(STRING,STRING);
//        map.put("components","value");
//        FormDataSchema formDataSchema = new FormDataSchema("1","1",1,map,map);
//        Mockito.when(userDetails.getUserDetails()).thenReturn(List.of(Map.of("id","0")));
//        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("token");
//        Mockito.when(formService.getRuntimeFormById(any())).thenReturn(formResponseSchema);
//        Mockito.when(formResponseSchema.getComponents()).thenReturn(map);
//        Mockito.when(validationCheckService.allFieldsValidations(any(), any(), anyString(), anyString())).thenReturn(List.of("9","1"));
//        Assertions.assertThrows(InvalidInputException.class, () -> formDataService.saveFormData(formDataSchema));
//    }

//    @Test
//    void saveFormDataTestDefaultCase() throws JsonProcessingException
//    {
//        FormResponseSchema formResponseSchema = Mockito.mock(FormResponseSchema.class);
//        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
//        map.put(STRING,STRING);
//        map.put("components","value");
//        FormDataSchema formDataSchema = new FormDataSchema("1","1",1,map,map);
//        Mockito.when(userDetails.getUserDetails()).thenReturn(List.of(Map.of("id","0")));
//        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("token");
//        Mockito.when(formService.getRuntimeFormById(any())).thenReturn(formResponseSchema);
//        Mockito.when(formResponseSchema.getComponents()).thenReturn(map);
//        Mockito.when(validationCheckService.allFieldsValidations(any(), any(), anyString(), anyString())).thenReturn(List.of("10","1"));
//        Mockito.when(idGenerator.nextId()).thenReturn(BigInteger.valueOf(1l));
//        Assertions.assertThrows(FormIdNotFoundException.class, () -> formDataService.saveFormData(formDataSchema));
//    }

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