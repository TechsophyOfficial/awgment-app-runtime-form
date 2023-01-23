package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.techsophy.idgenerator.IdGeneratorImpl;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.FormDataSchema;
import com.techsophy.tsf.runtime.form.entity.FormDataDefinition;
import com.techsophy.tsf.runtime.form.exception.FormIdNotFoundException;
import com.techsophy.tsf.runtime.form.exception.InvalidInputException;
import com.techsophy.tsf.runtime.form.service.impl.FormDataAuditServiceImpl;
import com.techsophy.tsf.runtime.form.service.impl.FormDataServiceImpl;
//import com.techsophy.tsf.runtime.form.service.impl.ValidationCheckServiceImpl;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import com.techsophy.tsf.runtime.form.utils.UserDetails;
import com.techsophy.tsf.runtime.form.utils.WebClientWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import javax.swing.text.Document;
import java.util.*;
import java.util.logging.Logger;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.FORM_DATA;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class FormDataServiceElasticDisabledExceptionTest
{
    @Mock
    MongoTemplate mockMongoTemplate;
    @Mock
    UserDetails mockUserDetails;
    @Mock
    TokenUtils mockTokenUtils;
    @Mock
    MessageSource mockMessageSource;
    @Mock
    ObjectMapper mockObjectMapper;
    @Mock
    IdGeneratorImpl mockIdGeneratorImpl;
    @Mock
    GlobalMessageSource mockGlobalMessageSource;
    @Mock
    FormDataAuditServiceImpl mockFormDataAuditServiceImpl;
    @Mock
    WebClientWrapper mockWebClientWrapper;
    @Mock
    Logger mockLogger;
    @Mock
    FormDataDefinition mockFormDataDefinition;
    @Mock
    MongoCollection mockMongoCollection;
    @Mock
    WebClient mockWebClient;
    @Mock
    FormService mockFormService;
    @Mock
    MongoCollection<Document> mockDocument;
    @Mock
    DeleteResult mockDeleteResult;
    @Mock
    AggregationResults aggregationResults;
    @InjectMocks
    FormDataServiceImpl mockFormDataServiceImpl;

    List<Map<String, Object>> userList = new ArrayList<>();
    List<Map<String,Object>> list=new ArrayList<>();

    @BeforeEach
    public void init()
    {
        Map<String, Object> map = new HashMap<>();
        map.put(CREATED_BY_ID, NULL);
        map.put(CREATED_BY_NAME, NULL);
        map.put(CREATED_ON, NULL);
        map.put(UPDATED_BY_ID, NULL);
        map.put(UPDATED_BY_NAME, NULL);
        map.put(UPDATED_ON, NULL);
        map.put(ID, BIGINTEGER_ID);
        map.put(USER_NAME, USER_FIRST_NAME);
        map.put(FIRST_NAME, USER_LAST_NAME);
        map.put(LAST_NAME, USER_FIRST_NAME);
        map.put(MOBILE_NUMBER, NUMBER);
        map.put(EMAIL_ID, MAIL_ID);
        map.put(DEPARTMENT, NULL);
        userList.add(map);
        Map<String,Object> mapData =new HashMap<>();
        mapData.put("create","true");
        list.add(mapData);
    }


    @Test
    void saveFormDataFormIdNullExceptionTest()
    {
        Map<String,Object> testFormData=new HashMap<>();
        testFormData.put(NAME,NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        Map<String,Object> testFormMetaData=new HashMap<>();
        testFormMetaData.put(FORM_VERSION,1);
        FormDataSchema formDataSchemaTest=new FormDataSchema(TEST_ID,null,TEST_VERSION,testFormData,testFormMetaData);
        Assertions.assertThrows(InvalidInputException.class, () ->
                mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
    }

    @Test
    void saveFormDataEmptyTokenExceptionTest() throws JsonProcessingException
    {
        Map<String,Object> testFormData=new HashMap<>();
        testFormData.put(NAME,NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        Map<String,Object> testFormMetaData=new HashMap<>();
        testFormMetaData.put(FORM_VERSION,1);
        FormDataSchema formDataSchemaTest=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,testFormData,testFormMetaData);
        doReturn(userList).when(mockUserDetails).getUserDetails();
        when(mockTokenUtils.getTokenFromContext()).thenReturn(EMPTY_STRING);
        Assertions.assertThrows(InvalidInputException.class, () ->
                mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
    }


//    @Test
//    void saveFormDataMissingMandatoryFieldsExceptionTest() throws JsonProcessingException
//    {
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        doReturn(userList).when(mockUserDetails).getUserDetails();
//        when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        Map<String, Object> givenData = new HashMap<>();
//        FormDataSchema formDataSchemaTest=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,givenData,testFormMetaData);
//        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_ID, TEST_NAME,
//                TEST_COMPONENTS,list, TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE,
//                TEST_CREATED_BY_ID, TEST_CREATED_ON,
//                TEST_UPDATED_BY_ID, TEST_UPDATED_ON);
//        when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
//        LinkedHashMap<String, LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
//        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
//        fieldsMap.put(REQUIRED,true);
//        schemaMap.put(NAME,fieldsMap);
//        schemaMap.put(AGE,fieldsMap);
//        try (MockedStatic<ValidateFormUtils> mockValidateFormUtils = Mockito.mockStatic(ValidateFormUtils.class))
//        {
//            mockValidateFormUtils.when(() -> ValidateFormUtils.getSchema(formResponseSchemaTest.getComponents()))
//                    .thenReturn(schemaMap);
//            when(mockValidationCheckServiceImpl.allFieldsValidations(schemaMap,givenData,formDataSchemaTest.getFormId(),formDataSchemaTest.getId())).thenReturn(List.of(String.valueOf(0),NAME));
//            Assertions.assertThrows(InvalidInputException.class, () ->
//                    mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
//        }
//    }

//    @Test
//    void saveFormDataDuplicatesExceptionTest() throws JsonProcessingException
//    {
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        doReturn(userList).when(mockUserDetails).getUserDetails();
//        when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        Map<String, Object> givenData = new HashMap<>();
//        givenData.put(NAME, NAME_VALUE);
//        FormDataSchema formDataSchemaTest=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,givenData,testFormMetaData);
//        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_ID, TEST_NAME,
//                TEST_COMPONENTS,list, TEST_PROPERTIES,TEST_TYPE_FORM,
//                TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID, TEST_CREATED_ON, TEST_UPDATED_BY_ID, TEST_UPDATED_ON);
//        when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
//        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
//        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
//        fieldsMap.put(UNIQUE,true);
//        schemaMap.put(NAME,fieldsMap);
//        try (MockedStatic<ValidateFormUtils> mockValidateFormUtils = Mockito.mockStatic(ValidateFormUtils.class))
//        {
//            mockValidateFormUtils.when(() -> ValidateFormUtils.getSchema(formResponseSchemaTest.getComponents()))
//                    .thenReturn(schemaMap);
//            when(mockValidationCheckServiceImpl.allFieldsValidations(schemaMap,givenData,formDataSchemaTest.getFormId(), formDataSchemaTest.getId())).thenReturn(List.of(String.valueOf(1),NAME));
//            Assertions.assertThrows(InvalidInputException.class, () ->
//                    mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
//        }
//    }

//    @Test
//    void saveFormDataMinLengthExceptionTest() throws JsonProcessingException
//    {
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        doReturn(userList).when(mockUserDetails).getUserDetails();
//        when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        Map<String, Object> givenData = new HashMap<>();
//        givenData.put(NAME, NAME_VALUE);
//        FormDataSchema formDataSchemaTest=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,givenData,testFormMetaData);
//        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_ID, TEST_NAME,
//                TEST_COMPONENTS,list, TEST_PROPERTIES,TEST_TYPE_FORM,
//                TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID, TEST_CREATED_ON,
//                TEST_UPDATED_BY_ID, TEST_UPDATED_ON);
//        when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
//        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
//        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
//        fieldsMap.put(MIN_LENGTH,10);
//        schemaMap.put(NAME,fieldsMap);
//        try (MockedStatic<ValidateFormUtils> mockValidateFormUtils = Mockito.mockStatic(ValidateFormUtils.class))
//        {
//            mockValidateFormUtils.when(() -> ValidateFormUtils.getSchema(formResponseSchemaTest.getComponents()))
//                    .thenReturn(schemaMap);
//            when(mockValidationCheckServiceImpl.allFieldsValidations(schemaMap,givenData,formDataSchemaTest.getFormId(), formDataSchemaTest.getId())).thenReturn(List.of(String.valueOf(2),NAME));
//            Assertions.assertThrows(InvalidInputException.class, () ->
//                    mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
//        }
//    }

//    @Test
//    void saveFormDataMaxLengthExceptionTest() throws JsonProcessingException
//    {
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        doReturn(userList).when(mockUserDetails).getUserDetails();
//        when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        Map<String, Object> givenData = new HashMap<>();
//        givenData.put(NAME, NAME_VALUE);
//        FormDataSchema formDataSchemaTest=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,givenData,testFormMetaData);
//        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_ID, TEST_NAME,
//                TEST_COMPONENTS,list, TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID,
//                TEST_CREATED_ON, TEST_UPDATED_BY_ID, TEST_UPDATED_ON);
//        when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
//        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
//        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
//        fieldsMap.put(MAX_LENGTH,1);
//        schemaMap.put(NAME,fieldsMap);
//        try (MockedStatic<ValidateFormUtils> mockValidateFormUtils = Mockito.mockStatic(ValidateFormUtils.class))
//        {
//            mockValidateFormUtils.when(() -> ValidateFormUtils.getSchema(formResponseSchemaTest.getComponents()))
//                    .thenReturn(schemaMap);
//            when(mockValidationCheckServiceImpl.allFieldsValidations(schemaMap,givenData,formDataSchemaTest.getFormId(), formDataSchemaTest.getId())).thenReturn(List.of(String.valueOf(3),NAME));
//            Assertions.assertThrows(InvalidInputException.class, () ->
//                    mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
//        }
//    }

//    @Test
//    void saveFormDataIntegerFieldsExceptionTest() throws JsonProcessingException
//    {
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        doReturn(userList).when(mockUserDetails).getUserDetails();
//        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        Map<String, Object> givenData = new HashMap<>();
//        givenData.put(NAME, NAME_VALUE);
//        givenData.put(AGE, "abc");
//        FormDataSchema formDataSchemaTest=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,givenData,testFormMetaData);
//        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_ID, TEST_NAME,
//                TEST_COMPONENTS,list, TEST_PROPERTIES,TEST_TYPE_FORM,
//                TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID, TEST_CREATED_ON, TEST_UPDATED_BY_ID,
//                TEST_UPDATED_ON);
//        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
//        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
//        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
//        schemaMap.put(NAME,fieldsMap);
//        schemaMap.put(AGE,fieldsMap);
//        try (MockedStatic<ValidateFormUtils> mockValidateFormUtils = Mockito.mockStatic(ValidateFormUtils.class))
//        {
//            mockValidateFormUtils.when(() -> ValidateFormUtils.getSchema(formResponseSchemaTest.getComponents()))
//                    .thenReturn(schemaMap);
//            Mockito.when(mockValidationCheckServiceImpl.allFieldsValidations(schemaMap,givenData,formDataSchemaTest.getFormId(), formDataSchemaTest.getId())).thenReturn(List.of(String.valueOf(4),NAME));
//            Assertions.assertThrows(InvalidInputException.class, () ->
//                    mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
//        }
//    }

//    @Test
//    void saveFormDataMinIntegerExceptionTest() throws JsonProcessingException
//    {
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        doReturn(userList).when(mockUserDetails).getUserDetails();
//        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        Map<String, Object> givenData = new HashMap<>();
//        givenData.put(NAME, NAME_VALUE);
//        givenData.put(AGE, 17);
//        FormDataSchema formDataSchemaTest=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,givenData,testFormMetaData);
//        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_ID, TEST_NAME, TEST_COMPONENTS,list, TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID,
//                TEST_CREATED_ON, TEST_UPDATED_BY_ID, TEST_UPDATED_ON);
//        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
//        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
//        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
//        schemaMap.put(NAME,fieldsMap);
//        fieldsMap.put(MIN,18);
//        schemaMap.put(AGE,fieldsMap);
//        try (MockedStatic<ValidateFormUtils> mockValidateFormUtils = Mockito.mockStatic(ValidateFormUtils.class))
//        {
//            mockValidateFormUtils.when(() -> ValidateFormUtils.getSchema(formResponseSchemaTest.getComponents()))
//                    .thenReturn(schemaMap);
//            Mockito.when(mockValidationCheckServiceImpl.allFieldsValidations(schemaMap,givenData,formDataSchemaTest.getFormId(), formDataSchemaTest.getId())).thenReturn(List.of(String.valueOf(5),AGE));
//            Assertions.assertThrows(InvalidInputException.class, () ->
//                    mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
//        }
//    }

//    @Test
//    void saveFormDataMaxIntegerExceptionTest() throws JsonProcessingException
//    {
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        doReturn(userList).when(mockUserDetails).getUserDetails();
//        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        Map<String, Object> givenData = new HashMap<>();
//        givenData.put(NAME, NAME_VALUE);
//        givenData.put(AGE, 100);
//        FormDataSchema formDataSchemaTest=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,givenData,testFormMetaData);
//        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_ID, TEST_NAME, TEST_COMPONENTS,list, TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID, TEST_CREATED_ON,
//                TEST_UPDATED_BY_ID, TEST_UPDATED_ON);
//        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
//        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
//        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
//        schemaMap.put(NAME,fieldsMap);
//        fieldsMap.put(MAX,99);
//        schemaMap.put(AGE,fieldsMap);
//        try (MockedStatic<ValidateFormUtils> mockValidateFormUtils = Mockito.mockStatic(ValidateFormUtils.class))
//        {
//            mockValidateFormUtils.when(() -> ValidateFormUtils.getSchema(formResponseSchemaTest.getComponents()))
//                    .thenReturn(schemaMap);
//            Mockito.when(mockValidationCheckServiceImpl.allFieldsValidations(schemaMap,givenData,formDataSchemaTest.getFormId(), formDataSchemaTest.getId())).thenReturn(List.of(String.valueOf(7),AGE));
//            Assertions.assertThrows(InvalidInputException.class, () ->
//                    mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
//        }
//    }

//    @Test
//    void saveFormDataMinWordExceptionTest() throws JsonProcessingException
//    {
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        doReturn(userList).when(mockUserDetails).getUserDetails();
//        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        Map<String, Object> givenData = new HashMap<>();
//        givenData.put(NAME, NAME_VALUE);
//        FormDataSchema formDataSchemaTest=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,givenData,testFormMetaData);
//        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_ID, TEST_NAME, TEST_COMPONENTS,list, TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID, TEST_CREATED_ON,
//                TEST_UPDATED_BY_ID, TEST_UPDATED_ON);
//        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
//        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
//        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
//        schemaMap.put(NAME,fieldsMap);
//        fieldsMap.put(MIN_WORDS,3);
//        try (MockedStatic<ValidateFormUtils> mockValidateFormUtils = Mockito.mockStatic(ValidateFormUtils.class))
//        {
//            mockValidateFormUtils.when(() -> ValidateFormUtils.getSchema(formResponseSchemaTest.getComponents()))
//                    .thenReturn(schemaMap);
//            Mockito.when(mockValidationCheckServiceImpl.allFieldsValidations(schemaMap,givenData,formDataSchemaTest.getFormId(), formDataSchemaTest.getId())).thenReturn(List.of(String.valueOf(8),AGE));
//            Assertions.assertThrows(InvalidInputException.class, () ->
//                    mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
//        }
//    }

//    @Test
//    void saveFormDataMaxWordExceptionTest() throws JsonProcessingException
//    {
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        doReturn(userList).when(mockUserDetails).getUserDetails();
//        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        Map<String, Object> givenData = new HashMap<>();
//        givenData.put(NAME, NAME_VALUE);
//        FormDataSchema formDataSchemaTest=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,givenData,testFormMetaData);
//        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_ID, TEST_NAME, TEST_COMPONENTS,list, TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID, TEST_CREATED_ON,
//                TEST_UPDATED_BY_ID, TEST_UPDATED_ON);
//        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
//        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
//        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
//        schemaMap.put(NAME,fieldsMap);
//        fieldsMap.put(MAX_WORDS,1);
//        try (MockedStatic<ValidateFormUtils> mockValidateFormUtils = Mockito.mockStatic(ValidateFormUtils.class))
//        {
//            mockValidateFormUtils.when(() -> ValidateFormUtils.getSchema(formResponseSchemaTest.getComponents()))
//                    .thenReturn(schemaMap);
//            Mockito.when(mockValidationCheckServiceImpl.allFieldsValidations(schemaMap,givenData,formDataSchemaTest.getFormId(), formDataSchemaTest.getId())).thenReturn(List.of(String.valueOf(9),NAME));
//            Assertions.assertThrows(InvalidInputException.class, () ->
//                    mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
//        }
//    }

    @Test
    void getAllFormDataByFormIdInvalidInputException()
    {
        String relations = "tp_runtime_form_data_994102731543871488:formData.orderId,tp_runtime_form_data_994122561634369536:formData.parcelId";
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData2 = new HashMap<>();
        testFormData2.put(NAME, NAME_VALUE);
        testFormData2.put(AGE, AGE_VALUE);
        FormDataSchema formDataSchemaTest = new FormDataSchema(TEST_ID, TEST_FORM_ID, TEST_VERSION, testFormData2, testFormMetaData);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA + formDataSchemaTest.getFormId())).thenReturn(false);
        Assertions.assertThrows(FormIdNotFoundException.class, () ->
                mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID, relations, null, null, null));
    }

    @Test
    void getAllFormDataByFormIdNotFoundExceptionTest() {
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData2 = new HashMap<>();
        testFormData2.put(NAME, NAME_VALUE);
        testFormData2.put(AGE, AGE_VALUE);
        FormDataSchema formDataSchemaTest = new FormDataSchema(TEST_ID, TEST_FORM_ID, TEST_VERSION, testFormData2, testFormMetaData);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA + formDataSchemaTest.getFormId())).thenReturn(false);
        Assertions.assertThrows(FormIdNotFoundException.class, () ->
                mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID, null, CREATED_ON, null, null));
    }

    @Test
    void getAllFormDataByFormIdInvalidInputExceptionTest() throws JsonProcessingException {
        ReflectionTestUtils.setField(mockFormDataServiceImpl,"elasticEnable",true);
        List<Map<String,Object>> list = new ArrayList<>();
        LinkedHashMap<String,Object> data = new LinkedHashMap<>();
        LinkedHashMap data1 = new LinkedHashMap<>();
        data1.put("abc","abc");
        data.put(FORM_DATA,data1);
        ArrayList list1 = new ArrayList<>();
        list1.add(data);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        list.add(testFormMetaData);
        Map<String, Object> testFormData2 = new HashMap<>();
        testFormData2.put(NAME, NAME_VALUE);
        testFormData2.put(AGE,AGE_VALUE);
        testFormData2.put("data",AGE_VALUE);
        testFormData2.put("content",AGE_VALUE);
        testFormData2.put(PAGE,AGE_VALUE);
        testFormData2.put(SIZE,AGE_VALUE);
        testFormData2.put(TOTAL_PAGES,AGE_VALUE);
        testFormData2.put(TOTAL_ELEMENTS,1L);
        testFormData2.put(NUMBER_OF_ELEMENTS,AGE_VALUE);
        when(mockWebClientWrapper.createWebClient(any())).thenReturn(mockWebClient);
        when(mockWebClientWrapper.webclientRequest(any(WebClient.class), anyString(), anyString(), eq(null))).thenReturn(STRING);
        when(mockObjectMapper.readValue(anyString(), eq(Map.class))).thenReturn(testFormData2);
        when(mockObjectMapper.convertValue(anyString(), eq(Map.class))).thenReturn(testFormData2);
        when(mockObjectMapper.convertValue(anyString(), eq(ArrayList.class))).thenReturn(list1);
        when(mockObjectMapper.convertValue(any(), eq(LinkedHashMap.class))).thenReturn(data1);
        when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        FormDataSchema formDataSchemaTest = new FormDataSchema(TEST_ID, TEST_FORM_ID, TEST_VERSION, testFormData2, testFormMetaData);
        when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA +formDataSchemaTest.getFormId())).thenReturn(true);
        Mockito.doReturn(aggregationResults).when(mockMongoTemplate).aggregate(Mockito.any(Aggregation.class), Mockito.eq(COLLECTION), Mockito.eq(Map.class));
        PageRequest pageRequest = PageRequest.of(0,2);
        Assertions.assertThrows(InvalidInputException.class,
                ()-> mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,null, ABC, CREATED_ON, DESCENDING,pageRequest));
    }

    @Test
    void getAllFormDataByFormIdNotFoundExceptionTest2()
    {
            Map<String, Object> testFormMetaData = new HashMap<>();
            testFormMetaData.put(FORM_VERSION, 1);
            Map<String, Object> testFormData2 = new HashMap<>();
            testFormData2.put(NAME, NAME_VALUE);
            testFormData2.put(AGE, AGE_VALUE);
            FormDataSchema formDataSchemaTest = new FormDataSchema(TEST_ID, TEST_FORM_ID, TEST_VERSION, testFormData2, testFormMetaData);
            when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA + formDataSchemaTest.getFormId())).thenReturn(false);
            PageRequest pageRequest = PageRequest.of(0, 2);
            Assertions.assertThrows(FormIdNotFoundException.class,()->mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID, TEST_RELATIONS, null, CREATED_ON, null));
    }

    @Test
    void getAllFormDataByFormIdAndQInvalidInputExceptionTest()
    {
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData2 = new HashMap<>();
        testFormData2.put(NAME_VALUE, NAME_VALUE);
        testFormData2.put(AGE,AGE_VALUE);
        FormDataSchema formDataSchemaTest = new FormDataSchema(TEST_ID, TEST_FORM_ID, TEST_VERSION, testFormData2, testFormMetaData);
        when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_+formDataSchemaTest.getFormId())).thenReturn(true);
        Assertions.assertThrows(InvalidInputException.class, () ->
                mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,EMPTY_STRING , null, null, null));
    }

        @Test
    void getAllFormDataByFormIdAndQIllegalArgumentSortExceptionTest()
    {
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData2 = new HashMap<>();
        testFormData2.put(NAME, NAME_VALUE);
        testFormData2.put(AGE,AGE_VALUE);
        FormDataSchema formDataSchemaTest = new FormDataSchema(TEST_ID, TEST_FORM_ID, TEST_VERSION, testFormData2, testFormMetaData);
        when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_+formDataSchemaTest.getFormId())).thenReturn(true);
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,TEST_RELATIONS,SEARCH_STRING, CREATED_ON, null));
    }

    @Test
    void getAllFormDataByFormIdAndQFormIdNotFoundExceptionTest()
    {
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData2 = new HashMap<>();
        testFormData2.put(NAME, NAME_VALUE);
        testFormData2.put(AGE, AGE_VALUE);
        FormDataSchema formDataSchemaTest = new FormDataSchema(TEST_ID, TEST_FORM_ID, TEST_VERSION, testFormData2, testFormMetaData);
        when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA + formDataSchemaTest.getFormId())).thenReturn(false);
        Assertions.assertThrows(FormIdNotFoundException.class, () ->
                mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID, TEST_RELATIONS, EMPTY_STRING, null, null));
    }

    @Test
    void getAllFormDataByFormIdAndQFormIdNotFoundExceptionTest2() {
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData2 = new HashMap<>();
        testFormData2.put(NAME, NAME_VALUE);
        testFormData2.put(AGE, AGE_VALUE);
        FormDataSchema formDataSchemaTest = new FormDataSchema(TEST_ID, TEST_FORM_ID, TEST_VERSION, testFormData2, testFormMetaData);
        when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA + formDataSchemaTest.getFormId())).thenReturn(false);
        PageRequest pageRequest = PageRequest.of(1, 5);
        Assertions.assertThrows(FormIdNotFoundException.class, () ->
                mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID, TEST_RELATIONS, EMPTY_STRING, null, null));
    }

    @Test
    void getAllFormDataByFormIdAndQInvalidInputExceptionTest4()
    {
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData2 = new HashMap<>();
        testFormData2.put(NAME, NAME_VALUE);
        testFormData2.put(AGE,AGE_VALUE);
        FormDataSchema formDataSchemaTest = new FormDataSchema(TEST_ID, TEST_FORM_ID, TEST_VERSION, testFormData2, testFormMetaData);
        when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA + formDataSchemaTest.getFormId())).thenReturn(false);
        PageRequest pageRequest = PageRequest.of(1, 5);
        Assertions.assertThrows(RuntimeException.class, () ->
                mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID, TEST_RELATIONS, EMPTY_STRING, CREATED_ON, null, pageRequest));
    }

    @Test
    void getAllFormDataByFormIdFormIdNotFoundExceptionTest()
    {
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData2 = new HashMap<>();
        testFormData2.put(NAME, NAME_VALUE);
        testFormData2.put(AGE, AGE_VALUE);
        FormDataSchema formDataSchemaTest = new FormDataSchema(TEST_ID, TEST_FORM_ID, TEST_VERSION, testFormData2, testFormMetaData);
        when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA + formDataSchemaTest.getFormId())).thenReturn(false);
        Assertions.assertThrows(FormIdNotFoundException.class, () ->
                mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID, TEST_RELATIONS));
    }

    @Test
    void deleteAllFormDataByFormIdNotFoundExceptionTest() {
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData2 = new HashMap<>();
        testFormData2.put(NAME, NAME_VALUE);
        testFormData2.put(AGE, AGE_VALUE);
        FormDataSchema formDataSchemaTest = new FormDataSchema(TEST_ID, TEST_FORM_ID, TEST_VERSION, testFormData2, testFormMetaData);
        when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA +formDataSchemaTest.getFormId())).thenReturn(false);
        Assertions.assertThrows(InvalidInputException.class, () ->
                mockFormDataServiceImpl.deleteAllFormDataByFormId(TEST_FORM_ID));
    }

    @Test
    void deleteFormDataByFormIdAndIdFormIdNotFoundExceptionTest()
    {
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData2 = new HashMap<>();
        testFormData2.put(NAME, NAME_VALUE);
        testFormData2.put(AGE,AGE_VALUE);
        FormDataSchema formDataSchemaTest = new FormDataSchema(TEST_ID, TEST_FORM_ID, TEST_VERSION, testFormData2, testFormMetaData);
        when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA +formDataSchemaTest.getFormId())).thenReturn(false);
        Assertions.assertThrows(FormIdNotFoundException.class, () ->
                mockFormDataServiceImpl.deleteFormDataByFormIdAndId(TEST_FORM_ID,TEST_FORM_DATA_ID));
    }

    @Test
    void deleteFormDataByFormIdAndIdInvalidInputExceptionTest2()
    {
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData2 = new HashMap<>();
        testFormData2.put(NAME, NAME_VALUE);
        testFormData2.put(AGE,AGE_VALUE);
        FormDataSchema formDataSchemaTest = new FormDataSchema(TEST_ID, TEST_FORM_ID, TEST_VERSION, testFormData2, testFormMetaData);
        when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA +formDataSchemaTest.getFormId())).thenReturn(true);
        when(mockMongoTemplate.getCollection(any())).thenReturn(mockMongoCollection);
        Assertions.assertThrows(InvalidInputException.class, () ->
                mockFormDataServiceImpl.deleteFormDataByFormIdAndId(TEST_FORM_ID,TEST_FORM_DATA_ID));
    }
}
