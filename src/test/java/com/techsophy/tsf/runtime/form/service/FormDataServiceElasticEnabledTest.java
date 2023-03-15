package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.assertions.Assertions;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.techsophy.idgenerator.IdGeneratorImpl;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.FormDataSchema;
import com.techsophy.tsf.runtime.form.dto.FormResponseSchema;
import com.techsophy.tsf.runtime.form.dto.ValidationResult;
import com.techsophy.tsf.runtime.form.entity.FormDataDefinition;
import com.techsophy.tsf.runtime.form.entity.Status;
import com.techsophy.tsf.runtime.form.service.impl.FormDataAuditServiceImpl;
import com.techsophy.tsf.runtime.form.service.impl.FormDataServiceImpl;
import com.techsophy.tsf.runtime.form.service.impl.FormValidationServiceImpl;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import com.techsophy.tsf.runtime.form.utils.UserDetails;
import com.techsophy.tsf.runtime.form.utils.WebClientWrapper;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.*;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.DATA;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.FORM_DATA;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class FormDataServiceElasticEnabledTest
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
    GlobalMessageSource mockGlobalMessageSource;
    @Mock
    FormDataAuditServiceImpl mockFormDataAuditServiceImpl;
    @Mock
    WebClientWrapper mockWebClientWrapper;
    @Mock
    Logger mockLogger;
    @Mock
    FormService mockFormService;
    @Mock
    IdGeneratorImpl mockIdGeneratorImpl;
    @Mock
    WebClient mockWebClient;
    @Mock
    MongoTemplate mockMongoTemplate;
    @Mock
    AggregationResults aggregationResults;
    @Mock
    MongoCollection mockMongoCollection;
    @Mock
    InsertOneResult mockInsertOneResult;
    @Mock
    MongoCursor mongoCursor;
    @Mock
    MongoCollection<Document> mongoCollectionDocument;
    @Mock
    FindIterable<Document> mockDocuments;
    @Mock
    DeleteResult mockDeleteResult;
    @Mock
    FormValidationServiceImpl mockFormValidationServiceImpl;
    @InjectMocks
    FormDataServiceImpl mockFormDataServiceImpl;

    List<Map<String, Object>> userList = new ArrayList<>();
    List<Map<String,Object>> list=new ArrayList<>();

    @BeforeEach
    public void init()
    {
        ReflectionTestUtils.setField(mockFormDataServiceImpl,GATEWAY_API,GATEWAY_API_VALUE);
        ReflectionTestUtils.setField(mockFormDataServiceImpl,"defaultPageLimit",20);
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
    void saveFormDataEmptyUniqueDocumentIdTest() throws IOException
    {
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_FORM_ID, TEST_NAME, TEST_COMPONENTS,new ArrayList<>(),TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID,
                String.valueOf(TEST_CREATED_ON), TEST_UPDATED_BY_ID,String.valueOf(TEST_UPDATED_ON), Status.ENABLED);
        Mockito.when(mockFormService.getRuntimeFormById(anyString())).thenReturn(formResponseSchemaTest);
        doReturn(userList).when(mockUserDetails).getUserDetails();
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockIdGeneratorImpl.nextId()).thenReturn(BigInteger.valueOf(Long.parseLong(TEST_FORM_ID)));
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        FormDataSchema formDataSchemaTest=new FormDataSchema(EMPTY_STRING,TEST_FORM_ID,
                TEST_VERSION,testFormData,testFormMetaData);
        ValidationResult validationResult=new ValidationResult("name");
        List<ValidationResult> validationResultList=new ArrayList<>();
        validationResultList.add(validationResult);
        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
        Mockito.when(mockFormValidationServiceImpl.validateData(any(),any(),any())).thenReturn(validationResultList);
        String responseTest="{\n" +
                "    \"data\": {\n" +
                "        \"id\": \"963403130239434752\",\n" +
                "        \"version\": 1\n" +
                "    },\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"Form data saved successfully\"\n" +
                "}";
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(POST),any())).thenReturn(responseTest);
        String getResponse="{\n" +
                "    \"data\": [\n" +
                "        {\n" +
                "            \"id\": \"945292224435081216\",\n" +
                "            \"formId\": \"928232634435125248\",\n" +
                "            \"version\": 2,\n" +
                "            \"formData\": {\n" +
                "                \"name\": \"akhil\",\n" +
                "                \"id\": \"945292224435081216\",\n" +
                "                \"age\": \"202\"\n" +
                "            },\n" +
                "            \"formMetadata\": {\n" +
                "                \"formVersion\": \"101\"\n" +
                "            },\n" +
                "            \"createdById\": \"910797699334508544\",\n" +
                "            \"createdOn\": \"2022-02-21T12:13:48.985338Z\",\n" +
                "            \"createdByName\": \"tejaswini kaza\",\n" +
                "            \"updatedById\": \"910797699334508544\",\n" +
                "            \"updatedOn\": \"2022-02-21T12:14:01.117149Z\",\n" +
                "            \"updatedByName\": \"tejaswini kaza\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"Form data retrieved successfully\"\n" +
                "}";
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(GET),any())).thenReturn(getResponse);
        Map<String,Object> responseMap=new HashMap<>();
        LinkedHashMap<String,Object> dataMap=new LinkedHashMap<>();
        dataMap.put("id","963403130239434752");
        dataMap.put("version",1);
        responseMap.put("success",true);
        responseMap.put("message","FormData saved successfully");
        responseMap.put("data",dataMap);
        Mockito.when(mockObjectMapper.readValue(getResponse,Map.class)).thenReturn(responseMap);
        Mockito.when(mockObjectMapper.convertValue(responseMap.get(DATA),LinkedHashMap.class)).thenReturn(dataMap);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA +formDataSchemaTest.getFormId())).thenReturn(true);
        Map<String, Object> formDataMap = new HashMap<>();
        formDataMap.put(UNDERSCORE_ID,Long.parseLong(UNDERSCORE_ID_VALUE));
        formDataMap.put(FORM_ID,TEST_FORM_ID);
        formDataMap.put(VERSION, String.valueOf(1));
        formDataMap.put(FORM_META_DATA, formDataSchemaTest.getFormMetaData());
        formDataMap.put(FORM_DATA, formDataSchemaTest.getFormData());
        formDataMap.put(CREATED_ON, Date.from(Instant.now()));
        formDataMap.put(CREATED_BY_ID,CREATED_BY_USER_ID);
        formDataMap.put(CREATED_BY_NAME, CREATED_BY_USER_NAME);
        formDataMap.put(UPDATED_ON,TEST_UPDATED_ON);
        formDataMap.put(UPDATED_BY_ID,UPDATED_BY_USER_ID);
        formDataMap.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        Mockito.when(mockObjectMapper.convertValue(any(),eq(FormDataDefinition.class))).thenReturn(new FormDataDefinition());
        Document document = new Document();
        document.put(VERSION,1);
        Assertions.assertNotNull(mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
    }

    @Test
    void saveFormDataNullUniqueDocumentIdTest() throws IOException
    {
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_FORM_ID, TEST_NAME, TEST_COMPONENTS,new ArrayList<>(),TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID,
                String.valueOf(TEST_CREATED_ON), TEST_UPDATED_BY_ID,String.valueOf(TEST_UPDATED_ON), Status.ENABLED);
        Mockito.when(mockFormService.getRuntimeFormById(anyString())).thenReturn(formResponseSchemaTest);
        doReturn(userList).when(mockUserDetails).getUserDetails();
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockIdGeneratorImpl.nextId()).thenReturn(BigInteger.valueOf(Long.parseLong(TEST_FORM_ID)));
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        FormDataSchema formDataSchemaTest=new FormDataSchema(EMPTY_STRING,TEST_FORM_ID,
                TEST_VERSION,testFormData,testFormMetaData);
        ValidationResult validationResult=new ValidationResult("name");
        List<ValidationResult> validationResultList=new ArrayList<>();
        validationResultList.add(validationResult);
        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
        Mockito.when(mockFormValidationServiceImpl.validateData(any(),any(),any())).thenReturn(validationResultList);
        String responseTest="{\n" +
                "    \"data\": {\n" +
                "        \"id\": \"963403130239434752\",\n" +
                "        \"version\": 1\n" +
                "    },\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"Form data saved successfully\"\n" +
                "}";
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(POST),any())).thenReturn(responseTest);
        String getResponse="{\n" +
                "    \"data\": [\n" +
                "        {\n" +
                "            \"id\": \"945292224435081216\",\n" +
                "            \"formId\": \"928232634435125248\",\n" +
                "            \"version\": 2,\n" +
                "            \"formData\": {\n" +
                "                \"name\": \"akhil\",\n" +
                "                \"id\": \"945292224435081216\",\n" +
                "                \"age\": \"202\"\n" +
                "            },\n" +
                "            \"formMetadata\": {\n" +
                "                \"formVersion\": \"101\"\n" +
                "            },\n" +
                "            \"createdById\": \"910797699334508544\",\n" +
                "            \"createdOn\": \"2022-02-21T12:13:48.985338Z\",\n" +
                "            \"createdByName\": \"tejaswini kaza\",\n" +
                "            \"updatedById\": \"910797699334508544\",\n" +
                "            \"updatedOn\": \"2022-02-21T12:14:01.117149Z\",\n" +
                "            \"updatedByName\": \"tejaswini kaza\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"Form data retrieved successfully\"\n" +
                "}";
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(GET),any())).thenReturn(getResponse);
        Map<String,Object> responseMap=new HashMap<>();
        LinkedHashMap<String,Object> dataMap=new LinkedHashMap<>();
        dataMap.put("id","963403130239434752");
        dataMap.put("version",1);
        responseMap.put("success",true);
        responseMap.put("message","FormData saved successfully");
        responseMap.put("data",dataMap);
        Mockito.when(mockObjectMapper.readValue(getResponse,Map.class)).thenReturn(responseMap);
        Mockito.when(mockObjectMapper.convertValue(responseMap.get(DATA),LinkedHashMap.class)).thenReturn(dataMap);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA +formDataSchemaTest.getFormId())).thenReturn(true);
        Map<String, Object> formDataMap = new HashMap<>();
        formDataMap.put(UNDERSCORE_ID,Long.parseLong(UNDERSCORE_ID_VALUE));
        formDataMap.put(FORM_ID,TEST_FORM_ID);
        formDataMap.put(VERSION, String.valueOf(1));
        formDataMap.put(FORM_META_DATA, formDataSchemaTest.getFormMetaData());
        formDataMap.put(FORM_DATA, formDataSchemaTest.getFormData());
        formDataMap.put(CREATED_ON, Date.from(Instant.now()));
        formDataMap.put(CREATED_BY_ID,CREATED_BY_USER_ID);
        formDataMap.put(CREATED_BY_NAME, CREATED_BY_USER_NAME);
        formDataMap.put(UPDATED_ON,TEST_UPDATED_ON);
        formDataMap.put(UPDATED_BY_ID,UPDATED_BY_USER_ID);
        formDataMap.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        Mockito.when(mockObjectMapper.convertValue(any(),eq(FormDataDefinition.class))).thenReturn(new FormDataDefinition());
        Document document = new Document();
        document.put(VERSION,1);
        Assertions.assertNotNull(mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
    }

    @Test
    void saveFormDataUniqueDocumentIdTest() throws IOException
    {
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_FORM_ID, TEST_NAME, TEST_COMPONENTS,new ArrayList<>(),TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID,
                String.valueOf(TEST_CREATED_ON), TEST_UPDATED_BY_ID,String.valueOf(TEST_UPDATED_ON), Status.ENABLED);
        Mockito.when(mockFormService.getRuntimeFormById(anyString())).thenReturn(formResponseSchemaTest);
        doReturn(userList).when(mockUserDetails).getUserDetails();
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockIdGeneratorImpl.nextId()).thenReturn(BigInteger.valueOf(Long.parseLong(TEST_FORM_ID)));
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        FormDataSchema formDataSchemaTest=new FormDataSchema(TEST_ID_VALUE,TEST_FORM_ID,
                TEST_VERSION,testFormData,testFormMetaData);
        ValidationResult validationResult=new ValidationResult("name");
        List<ValidationResult> validationResultList=new ArrayList<>();
        validationResultList.add(validationResult);
        Mockito.when(mockFormValidationServiceImpl.validateData(any(),any(),any())).thenReturn(validationResultList);
        String postResponse="{\n" +
                "    \"data\": {\n" +
                "        \"id\": \"963403130239434752\",\n" +
                "        \"version\": 1\n" +
                "    },\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"Form data saved successfully\"\n" +
                "}";
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(POST),any())).thenReturn(postResponse);
        String getResponse="{\n" +
                "    \"data\": [\n" +
                "        {\n" +
                "            \"id\": \"945292224435081216\",\n" +
                "            \"formId\": \"928232634435125248\",\n" +
                "            \"version\": 2,\n" +
                "            \"formData\": {\n" +
                "                \"name\": \"akhil\",\n" +
                "                \"id\": \"945292224435081216\",\n" +
                "                \"age\": \"202\"\n" +
                "            },\n" +
                "            \"formMetadata\": {\n" +
                "                \"formVersion\": \"101\"\n" +
                "            },\n" +
                "            \"createdById\": \"910797699334508544\",\n" +
                "            \"createdOn\": \"2022-02-21T12:13:48.985338Z\",\n" +
                "            \"createdByName\": \"tejaswini kaza\",\n" +
                "            \"updatedById\": \"910797699334508544\",\n" +
                "            \"updatedOn\": \"2022-02-21T12:14:01.117149Z\",\n" +
                "            \"updatedByName\": \"tejaswini kaza\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"Form data retrieved successfully\"\n" +
                "}";
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(GET),any())).thenReturn(getResponse);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA +formDataSchemaTest.getFormId())).thenReturn(true);
        Map<String, Object> formDataMap = new HashMap<>();
        formDataMap.put(UNDERSCORE_ID,Long.parseLong(UNDERSCORE_ID_VALUE));
        formDataMap.put(FORM_ID,TEST_FORM_ID);
        formDataMap.put(VERSION, String.valueOf(1));
        formDataMap.put(FORM_META_DATA, formDataSchemaTest.getFormMetaData());
        formDataMap.put(FORM_DATA, formDataSchemaTest.getFormData());
        formDataMap.put(CREATED_ON, Date.from(Instant.now()));
        formDataMap.put(CREATED_BY_ID,CREATED_BY_USER_ID);
        formDataMap.put(CREATED_BY_NAME, CREATED_BY_USER_NAME);
        formDataMap.put(UPDATED_ON,TEST_UPDATED_ON);
        formDataMap.put(UPDATED_BY_ID,UPDATED_BY_USER_ID);
        formDataMap.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        Mockito.when(mockMongoTemplate.getCollection(anyString())).thenReturn(mockMongoCollection);
        Document document = new Document();
        document.append("version",1);
        document.put(UNDERSCORE_ID,TEST_ID_VALUE);
        FindIterable<Document> iterable = mock(FindIterable.class);
        MongoCursor cursor = mock(MongoCursor.class);
        Mockito.when(iterable.iterator()).thenReturn(cursor);
        Mockito.when(cursor.next()).thenReturn(document);
        Map<String,Object> responseMap=new HashMap<>();
        LinkedHashMap<String,Object> dataMap=new LinkedHashMap<>();
        dataMap.put("id","963403130239434752");
        dataMap.put("version",1);
        responseMap.put("success",true);
        responseMap.put("message","FormData saved successfully");
        responseMap.put("data",dataMap);
        Mockito.when(mockObjectMapper.readValue(getResponse,Map.class)).thenReturn(responseMap);
        Mockito.when(mockObjectMapper.convertValue(responseMap.get(DATA),LinkedHashMap.class)).thenReturn(dataMap);
        Mockito.when(mockObjectMapper.convertValue(any(),eq(FormDataDefinition.class))).thenReturn(new FormDataDefinition());
        Mockito.when(mockMongoCollection.find((Bson) any())).thenReturn(iterable);
        Mockito.when(iterable.iterator()).thenReturn(cursor);
        Mockito.when(cursor.next()).thenReturn(document);
        Assertions.assertNotNull(mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
    }

    @Test
    void updateFormDataTest() throws JsonProcessingException
    {
        doReturn(userList).when(mockUserDetails).getUserDetails();
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        FormDataSchema formDataSchemaTest=new FormDataSchema(TEST_ID_VALUE,TEST_FORM_ID,
                TEST_VERSION,testFormData,testFormMetaData);
        Mockito.when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
        LinkedHashMap<String, Object> formDataMap = new LinkedHashMap<>();
        formDataMap.put(UNDERSCORE_ID,Long.parseLong(UNDERSCORE_ID_VALUE));
        formDataMap.put(FORM_ID,TEST_FORM_ID);
        formDataMap.put(VERSION, String.valueOf(1));
        formDataMap.put(FORM_META_DATA, formDataSchemaTest.getFormMetaData());
        formDataMap.put(FORM_DATA, formDataSchemaTest.getFormData());
        formDataMap.put(CREATED_ON, Date.from(Instant.now()));
        formDataMap.put(CREATED_BY_ID,CREATED_BY_USER_ID);
        formDataMap.put(CREATED_BY_NAME, CREATED_BY_USER_NAME);
        formDataMap.put(UPDATED_ON,TEST_UPDATED_ON);
        formDataMap.put(UPDATED_BY_ID,UPDATED_BY_USER_ID);
        formDataMap.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        Document document = new Document();
        document.put(UNDERSCORE_ID,TEST_ID_VALUE);
        document.put(FORM_DATA,testFormData);
        document.put(FORM_META_DATA,testFormMetaData);
        document.put(VERSION,1);
        FindIterable<Document> iterable = mock(FindIterable.class);
        MongoCursor cursor = mock(MongoCursor.class);
        Mockito.when(mockMongoTemplate.getCollection(anyString())).thenReturn(mockMongoCollection);
        Mockito.when(mockMongoCollection.find((Bson) any())).thenReturn(iterable);
        Mockito.when(iterable.iterator()).thenReturn(cursor);
        Mockito.when(cursor.hasNext()).thenReturn(true);
        Mockito.when(cursor.next()).thenReturn(document);
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_FORM_ID, TEST_NAME, TEST_COMPONENTS,new ArrayList<>(),TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID,
                String.valueOf(TEST_CREATED_ON), TEST_UPDATED_BY_ID,String.valueOf(TEST_UPDATED_ON), Status.ENABLED);
        Mockito.when(mockFormService.getRuntimeFormById(anyString())).thenReturn(formResponseSchemaTest);
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        String postResponse="{\n" +
                "    \"data\": {\n" +
                "        \"id\": \"963403130239434752\",\n" +
                "        \"version\": 1\n" +
                "    },\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"Form data saved successfully\"\n" +
                "}";
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(POST),any())).thenReturn(postResponse);
        Assertions.assertNotNull(mockFormDataServiceImpl.updateFormData(formDataSchemaTest));
    }

    @Test
    void deleteAllFormDataByFormIdTest()
    {
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_FORM_ID, TEST_NAME, TEST_COMPONENTS,new ArrayList<>(),TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID,
                String.valueOf(TEST_CREATED_ON), TEST_UPDATED_BY_ID,String.valueOf(TEST_UPDATED_ON), Status.ENABLED);
        Mockito.when(mockFormService.getRuntimeFormById(anyString())).thenReturn(formResponseSchemaTest);
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        String responseTest =RESPONSE_VALUE_30;
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),any(),any())).thenReturn(responseTest);
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
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA +TEST_FORM_ID)).thenReturn(true);
        mockFormDataServiceImpl.deleteAllFormDataByFormId(TEST_FORM_ID);
        verify(mockWebClientWrapper,times(1)).webclientRequest(any(),any(),any(),any());
    }

    @Test
    void deleteFormDataByFormIdAndIdTest()
    {
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        String responseTest =RESPONSE_VALUE_31;
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),any(),any())).thenReturn(responseTest);
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
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA +TEST_FORM_ID)).thenReturn(true);
        Mockito.when(mockMongoTemplate.getCollection(anyString())).thenReturn(mockMongoCollection);
        Mockito.when(mockMongoCollection.deleteMany(any())).thenReturn(mockDeleteResult);
        Mockito.when(mockDeleteResult.getDeletedCount()).thenReturn(Long.valueOf(1));
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_FORM_ID, TEST_NAME, TEST_COMPONENTS,new ArrayList<>(),TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID,
                String.valueOf(TEST_CREATED_ON), TEST_UPDATED_BY_ID,String.valueOf(TEST_UPDATED_ON), Status.ENABLED);
        Mockito.when(mockFormService.getRuntimeFormById(anyString())).thenReturn(formResponseSchemaTest);
        mockFormDataServiceImpl.deleteFormDataByFormIdAndId(TEST_FORM_ID,TEST_ID);
              verify(mockWebClientWrapper,times(1)).webclientRequest(any(),any(),any(),any());
    }
}





