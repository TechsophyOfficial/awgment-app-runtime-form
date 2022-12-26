package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.techsophy.idgenerator.IdGeneratorImpl;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.FormDataAuditResponse;
import com.techsophy.tsf.runtime.form.dto.FormDataAuditSchema;
import com.techsophy.tsf.runtime.form.dto.FormDataSchema;
import com.techsophy.tsf.runtime.form.dto.FormResponseSchema;
import com.techsophy.tsf.runtime.form.exception.InvalidInputException;
import com.techsophy.tsf.runtime.form.exception.RecordUnableToSaveException;
import com.techsophy.tsf.runtime.form.service.impl.FormDataAuditServiceImpl;
import com.techsophy.tsf.runtime.form.service.impl.FormDataServiceImpl;
import com.techsophy.tsf.runtime.form.service.impl.ValidationCheckServiceImpl;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import com.techsophy.tsf.runtime.form.utils.UserDetails;
import com.techsophy.tsf.runtime.form.utils.ValidateFormUtils;
import com.techsophy.tsf.runtime.form.utils.WebClientWrapper;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigInteger;
import java.util.*;
import java.util.logging.Logger;

import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;

import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.CONTENT;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.DATA;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.ELASTIC_ENABLE;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.ELASTIC_SOURCE;

import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.FORM_DATA;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.ONE;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.SUCCESS;

import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles(TEST_ACTIVE_PROFILE)
@SpringBootTest
@ExtendWith({SpringExtension.class})
class FormDataServiceElasticEnabledExceptionTest
{
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
    WebClient mockWebClient;
    @Mock
    ValidationCheckServiceImpl mockValidationCheckServiceImpl;
    @Mock
    FormService mockFormService;
    @Mock
    ValidateFormUtils mockValidateFormUtils;
    @Mock
    MongoTemplate mockMongoTemplate;
    @Mock
    MongoCollection mockMongoCollection;
    @Mock
    InsertOneResult insertOneResult;
    @InjectMocks
    FormDataServiceImpl mockFormDataServiceImpl;

    List<Map<String, Object>> userList = new ArrayList<>();
    Map<String, Object> map = new HashMap<>();

    @BeforeEach
    public void init()
    {
        ReflectionTestUtils.setField(mockFormDataServiceImpl,ELASTIC_ENABLE, true);
        ReflectionTestUtils.setField(mockFormDataServiceImpl,ELASTIC_SOURCE,true);
        ReflectionTestUtils.setField(mockFormDataServiceImpl,GATEWAY_API,GATEWAY_API_VALUE);
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
    }

    @Test
    void saveFormDataRecordNotFoundExceptionTest() throws JsonProcessingException
    {
        doReturn(userList).when(mockUserDetails).getUserDetails();
        when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        FormDataSchema formDataSchemaTest = new FormDataSchema(EMPTY_STRING, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_FORM_ID,
                TEST_NAME, TEST_COMPONENTS,
                null, null,"form", TEST_VERSION,IS_DEFAULT_VALUE,TEST_CREATED_BY_ID,
                TEST_CREATED_ON, TEST_UPDATED_BY_ID, TEST_UPDATED_ON);
        when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
        LinkedHashMap<String,Map<String,Object>> schemaMap=new LinkedHashMap<>();
        Map<String,Object> fieldsMap=new HashMap<>();
        fieldsMap.put(REQUIRED,false);
        fieldsMap.put(UNIQUE,false);
        schemaMap.put(NAME,fieldsMap);
        try (MockedStatic<ValidateFormUtils> mockValidateFormUtils = mockStatic(ValidateFormUtils.class))
        {
            mockValidateFormUtils.when(() -> ValidateFormUtils.getSchema(formResponseSchemaTest.getComponents()))
                    .thenReturn(schemaMap);
            when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_+formDataSchemaTest.getFormId())).thenReturn(true);
            when(mockMongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_+formDataSchemaTest.getFormId())).thenReturn(mockMongoCollection);
            when(mockIdGeneratorImpl.nextId()).thenReturn(BigInteger.valueOf(Long.parseLong(TEST_ID_VALUE)));
            FormDataAuditSchema formDataAuditSchemaTest = new
                    FormDataAuditSchema(TEST_ID_VALUE, TEST_FORM_DATA_ID,TEST_FORM_ID,TEST_VERSION,
                    testFormData,testFormMetaData);
            when(mockFormDataAuditServiceImpl.saveFormDataAudit(formDataAuditSchemaTest)).thenReturn(new FormDataAuditResponse(TEST_ID_VALUE,TEST_VERSION));
            String responseTest =RESPONSE_VALUE_15;
            when(mockWebClientWrapper.webclientRequest(any(),any(),eq(GET),any())).thenThrow(HttpServerErrorException.InternalServerError.class);
            Map<String,Object> responseMapTest=new HashMap<>();
            LinkedHashMap<String,Object> dataMapTest =new LinkedHashMap<>();
            dataMapTest.put(FORM_ID,TEST_FORM_ID);
            dataMapTest.put(UNDERSCORE_ID,Long.parseLong(TEST_ID));
            dataMapTest.put(ID,TEST_ID);
            dataMapTest.put(VERSION,TEST_VERSION);
            dataMapTest.put(FORM_DATA,testFormData);
            dataMapTest.put(FORM_META_DATA,testFormMetaData);
            dataMapTest.put(CREATED_BY_NAME,CREATED_BY_USER_NAME);
            dataMapTest.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
            dataMapTest.put(CREATED_BY_ID,BIGINTEGER_ID);
            dataMapTest.put(UPDATED_BY_ID,BIGINTEGER_ID);
            Date date=new Date();
            dataMapTest.put(CREATED_ON,date);
            dataMapTest.put(UPDATED_ON,date);
            Document document = new Document(dataMapTest);
            responseMapTest.put(DATA, dataMapTest);
            responseMapTest.put(SUCCESS,true);
            responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
            FindIterable iterable = mock(FindIterable.class);
            MongoCursor cursor = mock(MongoCursor.class);
            when(mockMongoCollection.find()).thenReturn(iterable);
            when(iterable.iterator()).thenReturn(cursor);
            when(cursor.hasNext()).thenReturn(true).thenReturn(false);
            when(cursor.next()).thenReturn(document);
            when(mockMongoCollection.findOneAndReplace((Bson) any(),any(), (FindOneAndReplaceOptions) any())).thenReturn(document);
            when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
            when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
            when(mockWebClientWrapper.webclientRequest(any(),any(),eq(POST),any())).thenThrow(HttpServerErrorException.InternalServerError.class);
            List<String> stringList=new ArrayList<>();
            stringList.add("10");
            stringList.add("101");
            when(mockValidationCheckServiceImpl.allFieldsValidations(any(),any(),any(),any())).thenReturn(stringList);
            final Map<String,Object> updatedElasticFormData=dataMapTest;
            when(mockObjectMapper.convertValue(any(), (Class<Object>) any())).thenReturn(updatedElasticFormData);
            when(mockMongoTemplate.save(any(),anyString())).thenReturn(testFormData);
            Assertions.assertThrows(RecordUnableToSaveException.class,()->mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
        }}

    @Test
    void getAllFormDataByFormIdEmptySortInvalidInputExceptionTest()
    {
        Assertions.assertThrows(InvalidInputException.class, () -> mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,null, TEST_ID, CREATED_ON, EMPTY_STRING));
    }

    @Test
    void getAllFormDataByFormIdEmptyTokenTest()
    {
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(EMPTY_STRING);
        Assertions.assertThrows(InvalidInputException.class, () -> mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,null, TEST_ID, CREATED_ON, DESCENDING));
    }

    @Test
    void getAllFormDataByFormIdHashMapExceptionTest() throws JsonProcessingException
    {
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        String responseTest =RESPONSE_VALUE_2;
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(GET),any())).thenReturn(responseTest);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        Map<String,Object> responseMapTest =new HashMap<>();
        ArrayList contentListTest =new ArrayList();
        LinkedHashMap<String,Object> singleContentTest =new LinkedHashMap<>();
        singleContentTest.put(FORM_ID,null);
        singleContentTest.put(ID,TEST_ID);
        singleContentTest.put(VERSION,TEST_VERSION);
        singleContentTest.put(FORM_DATA,testFormData);
        singleContentTest.put(FORM_META_DATA,testFormMetaData);
        singleContentTest.put(CREATED_BY_NAME,CREATED_BY_USER_NAME);
        singleContentTest.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        singleContentTest.put(CREATED_BY_ID,BIGINTEGER_ID);
        singleContentTest.put(UPDATED_BY_ID,BIGINTEGER_ID);
        singleContentTest.put(CREATED_ON,TEST_CREATED_ON);
        singleContentTest.put(UPDATED_ON,TEST_UPDATED_ON);
        contentListTest.add(singleContentTest);
        Map<String,Object> dataMapTest =new HashMap<>();
        dataMapTest.put(CONTENT, contentListTest);
        responseMapTest.put(DATA, dataMapTest);
        responseMapTest.put(SUCCESS,true);
        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
        Mockito.when(mockObjectMapper.readValue(anyString(),eq(Map.class))).thenReturn(responseMapTest);
        Mockito.when(mockObjectMapper.convertValue(any(),eq(Map.class))).thenReturn(responseMapTest);
        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
        Mockito.when(mockObjectMapper.convertValue(dataMapTest.get(CONTENT),ArrayList.class)).thenReturn(contentListTest);
        Mockito.when(mockObjectMapper.convertValue(contentListTest.get(0), LinkedHashMap.class)).thenReturn(singleContentTest);
        Assertions.assertThrows(InvalidInputException.class, () -> mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,null, TEST_ID, CREATED_ON, DESCENDING));
    }

    @Test
    void getAllFormDataByFormIdNotFoundExceptionTest() throws JsonProcessingException
    {
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        String responseTest =RESPONSE_VALUE_3;
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(GET),any())).thenReturn(responseTest);
        Map<String,Object> responseMapTest =new HashMap<>();
        responseMapTest.put(SUCCESS,true);
        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
        Mockito.when(mockObjectMapper.readValue(responseTest, Map.class)).thenReturn(responseMapTest);
        Assertions.assertThrows(InvalidInputException.class, () -> mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,null, TEST_ID, CREATED_ON, DESCENDING));
    }

    @Test
    void getAllFormDataByFormIdPaginationEmptySortExceptionTest()
    {
        Assertions.assertThrows(InvalidInputException.class, () ->mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,null, TEST_ID, CREATED_ON, EMPTY_STRING,
                PageRequest.of(0, 5)));
    }

    @Test
    void getAllFormDataByFormIdPaginationEmptyTokenTest()
    {
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(EMPTY_STRING);
        Assertions.assertThrows(InvalidInputException.class, () -> mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID, null, TEST_ID, CREATED_ON, DESCENDING, PageRequest.of(0, 5)));
    }

    @Test
    void getAllFormDataByFormIdPaginationInvalidInputExceptionTest() throws JsonProcessingException
    {
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        String responseTest =RESPONSE_VALUE_4;
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(GET),any())).thenReturn(responseTest);
        Map<String,Object> responseMapTest =new HashMap<>();
        responseMapTest.put(SUCCESS,true);
        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
        Mockito.when(mockObjectMapper.readValue(responseTest, Map.class)).thenReturn(responseMapTest);
        Assertions.assertThrows(InvalidInputException.class, () -> mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,null, TEST_ID, CREATED_ON, DESCENDING, PageRequest.of(0, 5)));
    }

    @Test
    void getAllFormDataByFormIdAndQEmptySortTest()
    {
        Assertions.assertThrows(InvalidInputException.class, () -> mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,null, TEST_ID, CREATED_ON, EMPTY_STRING));
    }

    @Test
    void getAllFormDataByFormIdAndQEmptyTokenTest()
    {
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(EMPTY_STRING);
        Assertions.assertThrows(InvalidInputException.class, () -> mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,null, TEST_ID, CREATED_ON, DESCENDING));
    }

    @Test
    void getAllFormDataByFormIdAndQHashMapExceptionTest() throws JsonProcessingException
    {
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        String responseTest =RESPONSE_VALUE_5;
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(GET),any())).thenReturn(responseTest);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        Map<String,Object> responseMapTest =new HashMap<>();
        ArrayList contentListTest =new ArrayList();
        LinkedHashMap<String,Object> singleContentTest =new LinkedHashMap<>();
        singleContentTest.put(FORM_ID,null);
        singleContentTest.put(ID,TEST_ID);
        singleContentTest.put(VERSION,TEST_VERSION);
        singleContentTest.put(FORM_DATA,testFormData);
        singleContentTest.put(FORM_META_DATA,testFormMetaData);
        singleContentTest.put(CREATED_BY_NAME,CREATED_BY_USER_NAME);
        singleContentTest.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        singleContentTest.put(CREATED_BY_ID,BIGINTEGER_ID);
        singleContentTest.put(UPDATED_BY_ID,BIGINTEGER_ID);
        singleContentTest.put(CREATED_ON,TEST_CREATED_ON);
        singleContentTest.put(UPDATED_ON,TEST_UPDATED_ON);
        contentListTest.add(singleContentTest);
        Map<String,Object> dataMapTest =new HashMap<>();
        dataMapTest.put(CONTENT, contentListTest);
        responseMapTest.put(DATA, dataMapTest);
        responseMapTest.put(SUCCESS,true);
        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
        Mockito.when(mockObjectMapper.convertValue(dataMapTest.get(CONTENT),ArrayList.class)).thenReturn(contentListTest);
        Mockito.when(mockObjectMapper.convertValue(contentListTest.get(0), LinkedHashMap.class)).thenReturn(singleContentTest);
        Assertions.assertThrows(InvalidInputException.class, () -> mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,null, TEST_ID, CREATED_ON, DESCENDING));
    }

    @Test
    void getAllFormDataByFormIdAndQInvalidInputExceptionTest() throws JsonProcessingException
    {
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        String responseTest =RESPONSE_VALUE_6;
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(GET),any())).thenReturn(responseTest);
        Map<String,Object> responseMapTest =new HashMap<>();
        responseMapTest.put(SUCCESS,true);
        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
        Mockito.when(mockObjectMapper.readValue(responseTest, Map.class)).thenReturn(responseMapTest);
        Assertions.assertThrows(InvalidInputException.class, () -> mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,null, TEST_ID, CREATED_ON, DESCENDING));
    }

    @Test
    void getAllFormDataByFormIdAndQPaginationEmptySortTest()
    {
        Assertions.assertThrows(InvalidInputException.class, () -> mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,null, TEST_ID, CREATED_ON, EMPTY_STRING, PageRequest.of(0, 5)));
    }

    @Test
    void getAllFormDataByFormIdAndQPaginationEmptyTokenTest()
    {
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(EMPTY_STRING);
        Assertions.assertThrows(InvalidInputException.class, () -> mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,null, TEST_ID, CREATED_ON, DESCENDING, PageRequest.of(0, 5)));
    }

    @Test
    void getAllFormDataByFormIdAndQPaginationInvalidInputExceptionTest() throws JsonProcessingException
    {
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        String responseTest =RESPONSE_VALUE_7;
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(GET),any())).thenReturn(responseTest);
        Map<String,Object> responseMapTest =new HashMap<>();
        responseMapTest.put(SUCCESS,true);
        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
        Mockito.when(mockObjectMapper.readValue(responseTest, Map.class)).thenReturn(responseMapTest);
        Assertions.assertThrows(InvalidInputException.class, () -> mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,null, TEST_ID, CREATED_ON, DESCENDING, PageRequest.of(0, 5)));
    }

    @Test
    void getAllFormDataByFormIdTokenExceptionTest()
    {
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(EMPTY_STRING);
        Assertions.assertThrows(InvalidInputException.class, () -> mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,null));
    }

    @Test
    void getAllFormDataByFormIdInvalidInputExceptionTest() throws JsonProcessingException
    {
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        String responseTest =RESPONSE_VALUE_8;
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(GET),any())).thenReturn(responseTest);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        Map<String,Object> responseMapTest =new HashMap<>();
        ArrayList contentListTest =new ArrayList();
        LinkedHashMap<String,Object> singleContentTest =new LinkedHashMap<>();
        singleContentTest.put(FORM_ID,TEST_FORM_ID);
        singleContentTest.put(ID,TEST_ID);
        singleContentTest.put(VERSION,TEST_VERSION);
        singleContentTest.put(FORM_DATA,testFormData);
        singleContentTest.put(FORM_META_DATA,testFormMetaData);
        singleContentTest.put(CREATED_BY_NAME,CREATED_BY_USER_NAME);
        singleContentTest.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        singleContentTest.put(CREATED_BY_ID,BIGINTEGER_ID);
        singleContentTest.put(UPDATED_BY_ID,BIGINTEGER_ID);
        singleContentTest.put(CREATED_ON,TEST_CREATED_ON);
        singleContentTest.put(UPDATED_ON,TEST_UPDATED_ON);
        contentListTest.add(singleContentTest);
        Map<String,Object> dataMapTest =new HashMap<>();
        dataMapTest.put(CONTENT, contentListTest);
        dataMapTest.put(TOTAL_PAGES,ONE);
        dataMapTest.put(TOTAL_ELEMENTS,ONE);
        dataMapTest.put(SIZE,PAGE_SIZE);
        dataMapTest.put(NUMBER_OF_ELEMENTS,ONE);
        responseMapTest.put(DATA, dataMapTest);
        responseMapTest.put(SUCCESS,true);
        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
        Mockito.when(mockObjectMapper.convertValue(dataMapTest.get(CONTENT),ArrayList.class)).thenReturn(contentListTest);
        Mockito.when(mockObjectMapper.convertValue(contentListTest.get(0), LinkedHashMap.class)).thenReturn(singleContentTest);
        Assertions.assertThrows(InvalidInputException.class, () -> mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,null));
    }

    @Test
    void getFormDataByFormIdAndIdTokenExceptionTest()
    {
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(EMPTY_STRING);
        Assertions.assertThrows(InvalidInputException.class, () -> mockFormDataServiceImpl.getFormDataByFormIdAndId(TEST_FORM_ID, TEST_ID,null));
    }

    @Test
    void getFormDataByFormIdAndIdDataMapExceptionTest() throws JsonProcessingException
    {
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        String responseTest =RESPONSE_VALUE_9;
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(GET),any())).thenReturn(responseTest);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        Map<String,Object> responseMapTest =new HashMap<>();
        Map<String,Object> dataMapTest =new HashMap<>();
        dataMapTest.put(FORM_ID,TEST_FORM_ID);
        dataMapTest.put(ID,TEST_ID);
        dataMapTest.put(FORM_DATA,testFormData);
        dataMapTest.put(FORM_META_DATA,testFormMetaData);
        dataMapTest.put(CREATED_BY_NAME,CREATED_BY_USER_NAME);
        dataMapTest.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        dataMapTest.put(CREATED_BY_ID,BIGINTEGER_ID);
        dataMapTest.put(UPDATED_BY_ID,BIGINTEGER_ID);
        dataMapTest.put(CREATED_ON,TEST_CREATED_ON);
        dataMapTest.put(UPDATED_ON,TEST_UPDATED_ON);
        ArrayList contentListTest =new ArrayList();
        HashMap<String,Object> singleContentTest =new HashMap<>();
        singleContentTest.put(FORM_ID,TEST_FORM_ID);
        singleContentTest.put(ID,TEST_ID);
        singleContentTest.put(VERSION,TEST_VERSION);
        singleContentTest.put(FORM_DATA,testFormData);
        singleContentTest.put(FORM_META_DATA,testFormMetaData);
        singleContentTest.put(CREATED_BY_NAME,CREATED_BY_USER_NAME);
        singleContentTest.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        singleContentTest.put(CREATED_BY_ID,BIGINTEGER_ID);
        singleContentTest.put(UPDATED_BY_ID,BIGINTEGER_ID);
        singleContentTest.put(CREATED_ON,TEST_CREATED_ON);
        singleContentTest.put(UPDATED_ON,TEST_UPDATED_ON);
        contentListTest.add(singleContentTest);
        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
//        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
//        Mockito.when(mockObjectMapper.convertValue(dataMapTest.get(CONTENT), ArrayList.class)).thenReturn(contentListTest);
        Assertions.assertThrows(InvalidInputException.class, () -> mockFormDataServiceImpl.getFormDataByFormIdAndId(TEST_FORM_ID, TEST_ID,null));
    }

    @Test
    void getAllFormDataByFormIdAndIdInvalidInputExceptionTest() throws JsonProcessingException
    {
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        String responseTest =RESPONSE_VALUE_10;
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(GET),any())).thenReturn(responseTest);
        Map<String,Object> responseMapTest =new HashMap<>();
        Mockito.when(mockObjectMapper.readValue(responseTest, Map.class)).thenReturn(responseMapTest);
        Assertions.assertThrows(InvalidInputException.class, () -> mockFormDataServiceImpl.getFormDataByFormIdAndId(TEST_FORM_ID, TEST_ID,null));
    }

    @Test
    void deleteAllFormDataByFormIdTokenEmptyExceptionTest()
    {
        when(mockTokenUtils.getTokenFromContext()).thenReturn(EMPTY_STRING);
        when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
        Assertions.assertThrows(InvalidInputException.class,()->mockFormDataServiceImpl.deleteAllFormDataByFormId(TEST_FORM_ID));
    }

    @Test
    void deleteAllFormDataByFormIdInvalidInputExceptionTest()
    {
        when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        when(mockWebClientWrapper.webclientRequest(any(),any(),any(),any())).thenThrow(HttpServerErrorException.InternalServerError.class);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        Map<String,Object> responseMapTest =new HashMap<>();
        Map<String,Object> dataMapTest =new HashMap<>();
        dataMapTest.put(FORM_ID,TEST_FORM_ID);
        dataMapTest.put(ID,TEST_ID);
        dataMapTest.put(VERSION,TEST_VERSION);
        dataMapTest.put(FORM_DATA,testFormData);
        dataMapTest.put(FORM_META_DATA,testFormMetaData);
        dataMapTest.put(CREATED_BY_NAME,CREATED_BY_USER_NAME);
        dataMapTest.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        dataMapTest.put(CREATED_BY_ID,BIGINTEGER_ID);
        dataMapTest.put(UPDATED_BY_ID,BIGINTEGER_ID);
        dataMapTest.put(CREATED_ON,TEST_CREATED_ON);
        dataMapTest.put(UPDATED_ON,TEST_UPDATED_ON);
        responseMapTest.put(DATA, dataMapTest);
        responseMapTest.put(SUCCESS,true);
        when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
        when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
        Assertions.assertThrows(InvalidInputException.class,()->mockFormDataServiceImpl.deleteAllFormDataByFormId(TEST_FORM_ID));
    }

    @Test
    void deleteFormDataByFormIdAndIdTest()
    {
        when(mockTokenUtils.getTokenFromContext()).thenReturn(EMPTY_STRING);
        when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
        when(mockMongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_ +TEST_FORM_ID)).thenReturn(mockMongoCollection);
        DeleteResult mockDeleteResult=Mockito.mock(DeleteResult.class);
        when(mockMongoCollection.deleteMany(any())).thenReturn(mockDeleteResult);
        when(mockDeleteResult.getDeletedCount()).thenReturn(Long.valueOf(1));
        Assertions.assertThrows(InvalidInputException.class,()->mockFormDataServiceImpl.deleteFormDataByFormIdAndId(TEST_FORM_ID,TEST_ID));
    }

    @Test
    void deleteFormDataByFormIdAndIdInvalidInputExceptionTest() throws JsonProcessingException
    {
        when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        String responseTest =RESPONSE_VALUE_13;
        when(mockWebClientWrapper.webclientRequest(any(),any(),any(),any())).thenThrow(HttpServerErrorException.InternalServerError.class);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        Map<String,Object> responseMapTest =new HashMap<>();
        Map<String,Object> dataMapTest =new HashMap<>();
        dataMapTest.put(FORM_ID,TEST_FORM_ID);
        dataMapTest.put(ID,TEST_ID);
        dataMapTest.put(VERSION,TEST_VERSION);
        dataMapTest.put(FORM_DATA,testFormData);
        dataMapTest.put(FORM_META_DATA,testFormMetaData);
        dataMapTest.put(CREATED_BY_NAME,CREATED_BY_USER_NAME);
        dataMapTest.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        dataMapTest.put(CREATED_BY_ID,BIGINTEGER_ID);
        dataMapTest.put(UPDATED_BY_ID,BIGINTEGER_ID);
        dataMapTest.put(CREATED_ON,TEST_CREATED_ON);
        dataMapTest.put(UPDATED_ON,TEST_UPDATED_ON);
        responseMapTest.put(DATA, null);
        responseMapTest.put(SUCCESS,true);
        when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
        when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
        when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
        when(mockMongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_ +TEST_FORM_ID)).thenReturn(mockMongoCollection);
        DeleteResult mockDeleteResult=Mockito.mock(DeleteResult.class);
        when(mockMongoCollection.deleteMany(any())).thenReturn(mockDeleteResult);
        when(mockDeleteResult.getDeletedCount()).thenReturn(Long.valueOf(1));
        Assertions.assertThrows(InvalidInputException.class,()->mockFormDataServiceImpl.deleteFormDataByFormIdAndId(TEST_FORM_ID,TEST_ID));
    }

    @Test
    void deleteFormDataByFormIdAndIdTokenInvalidInputExceptionTest()
    {
        when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        when(mockWebClientWrapper.webclientRequest(any(),any(),any(),any())).thenThrow(HttpServerErrorException.InternalServerError.class);
        when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME,NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        Map<String,Object> responseMapTest =new HashMap<>();
        Map<String,Object> dataMapTest =new HashMap<>();
        dataMapTest.put(FORM_ID,TEST_FORM_ID);
        dataMapTest.put(ID,TEST_ID);
        dataMapTest.put(VERSION,TEST_VERSION);
        dataMapTest.put(FORM_DATA,testFormData);
        dataMapTest.put(FORM_META_DATA,testFormMetaData);
        dataMapTest.put(CREATED_BY_NAME,CREATED_BY_USER_NAME);
        dataMapTest.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        dataMapTest.put(CREATED_BY_ID,BIGINTEGER_ID);
        dataMapTest.put(UPDATED_BY_ID,BIGINTEGER_ID);
        dataMapTest.put(CREATED_ON,TEST_CREATED_ON);
        dataMapTest.put(UPDATED_ON,TEST_UPDATED_ON);
        responseMapTest.put(DATA, null);
        responseMapTest.put(SUCCESS,true);
        when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
        when(mockMongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_ +TEST_FORM_ID)).thenReturn(mockMongoCollection);
        DeleteResult mockDeleteResult=Mockito.mock(DeleteResult.class);
        when(mockMongoCollection.deleteMany(any())).thenReturn(mockDeleteResult);
        when(mockDeleteResult.getDeletedCount()).thenReturn(Long.valueOf(1));
        Assertions.assertThrows(InvalidInputException.class,()->mockFormDataServiceImpl.deleteFormDataByFormIdAndId(TEST_FORM_ID,TEST_ID));
    }
}
