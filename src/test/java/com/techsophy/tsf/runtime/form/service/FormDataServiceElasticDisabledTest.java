package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.techsophy.idgenerator.IdGeneratorImpl;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.FormDataSchema;
import com.techsophy.tsf.runtime.form.dto.FormResponseSchema;
import com.techsophy.tsf.runtime.form.dto.ValidationResult;
import com.techsophy.tsf.runtime.form.entity.FormDataDefinition;
import com.techsophy.tsf.runtime.form.entity.Status;
import com.techsophy.tsf.runtime.form.exception.InvalidInputException;
import com.techsophy.tsf.runtime.form.service.impl.FormDataAuditServiceImpl;
import com.techsophy.tsf.runtime.form.service.impl.FormDataServiceImpl;
import com.techsophy.tsf.runtime.form.service.impl.FormValidationServiceImpl;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import com.techsophy.tsf.runtime.form.utils.UserDetails;
import com.techsophy.tsf.runtime.form.utils.WebClientWrapper;
import org.bson.Document;
import org.bson.conversions.Bson;
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
import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.*;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.CONTENT;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.DATA;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.DEFAULT_PAGE_LIMIT;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.FILTER;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.FORM_DATA;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.Q;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class FormDataServiceElasticDisabledTest
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
    GlobalMessageSource mockGlobalMessageSource;
    @Mock
    FormDataAuditServiceImpl mockFormDataAuditServiceImpl;
    @Mock
    WebClientWrapper mockWebClientWrapper;
    @Mock
    FormDataDefinition mockFormDataDefinition;
    @Mock
    MongoCollection mockMongoCollection;
    @Mock
    MongoCollection<Document> mongoCollectionDocument;
    @Mock
    WebClient mockWebClient;
    @Mock
    FormService mockFormService;
    @Mock
    IdGeneratorImpl mockIdGeneratorImpl;
    @Mock
    InsertOneResult insertOneResult;
    @Mock
    FindIterable<Document> mockDocuments;
    @Mock
    DeleteResult mockDeleteResult;
    @Mock
    ObjectMapper mockObjectMapper;
    @Mock
    MongoCursor mongoCursor;
    @Mock
    AggregationResults aggregationResults;
    @Mock
    FormValidationServiceImpl mockFormValidationServiceImpl;
    @InjectMocks
    FormDataServiceImpl mockFormDataServiceImpl;

    List<Map<String, Object>> userList = new ArrayList<>();
    Map<String, Object> map = new HashMap<>();
    List<Map<String,Object>> list=new ArrayList<>();

    @BeforeEach
    public void init()
    {
        ReflectionTestUtils.setField(mockFormDataServiceImpl,DEFAULT_PAGE_LIMIT, 20);
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
    void saveFormDataCollectionExistsTest() throws IOException
    {
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        FormDataSchema formDataSchemaTest=new FormDataSchema(TEST_ID_VALUE,TEST_FORM_ID,
                TEST_VERSION,testFormData,testFormMetaData);
        doReturn(userList).when(mockUserDetails).getUserDetails();
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_FORM_ID, TEST_NAME, TEST_COMPONENTS, list,TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID,
                String.valueOf(TEST_CREATED_ON), TEST_UPDATED_BY_ID, String.valueOf(TEST_UPDATED_ON),Status.DISABLED);
        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
        ValidationResult validationResult=new ValidationResult("name");
        List<ValidationResult> validationResultList=new ArrayList<>();
        validationResultList.add(validationResult);
        Mockito.when(mockFormValidationServiceImpl.validateData(any(),any(),anyString())).thenReturn(validationResultList);
        Mockito.when(mockIdGeneratorImpl.nextId()).thenReturn(BigInteger.valueOf(Long.parseLong(TEST_FORM_ID)));
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA +formDataSchemaTest.getFormId())).thenReturn(true);
        Mockito.when(mockMongoTemplate.getCollection(anyString())).thenReturn(mockMongoCollection);
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
        Document document = new Document();
        document.put(VERSION,1);
        FindIterable<Document> iterable = mock(FindIterable.class);
        MongoCursor cursor = mock(MongoCursor.class);
        Mockito.when(mockMongoCollection.find((Bson) any())).thenReturn(iterable);
        Mockito.when(iterable.iterator()).thenReturn(cursor);
        Mockito.when(cursor.next()).thenReturn(document);
        Mockito.when(mockObjectMapper.convertValue(any(),eq(FormDataDefinition.class))).thenReturn(new FormDataDefinition());
        Mockito.when(mockMongoTemplate.getCollection(anyString())).thenReturn(mockMongoCollection);
        Assertions.assertNotNull(mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
    }

    @Test
    void saveFormDataWithoutCollectionTest() throws IOException
    {
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        FormDataSchema formDataSchemaTest=new FormDataSchema(null,TEST_FORM_ID,
                TEST_VERSION,testFormData,testFormMetaData);
        doReturn(userList).when(mockUserDetails).getUserDetails();
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_FORM_ID, TEST_NAME, TEST_COMPONENTS, list,TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID,
                String.valueOf(TEST_CREATED_ON), TEST_UPDATED_BY_ID, String.valueOf(TEST_UPDATED_ON),Status.DISABLED);
        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
        Mockito.when(mockIdGeneratorImpl.nextId()).thenReturn(BigInteger.valueOf(Long.parseLong(TEST_FORM_ID)));
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA +formDataSchemaTest.getFormId())).thenReturn(false);
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
        Mockito.when(mockMongoTemplate.save(any(),anyString())).thenReturn(formDataMap);
        Mockito.when(mockObjectMapper.convertValue(any(),eq(FormDataDefinition.class))).thenReturn(new FormDataDefinition());
        Assertions.assertNotNull(mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
    }

    @Test
    void saveFormDataWithUniqueKeyCheck() throws IOException
    {
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        FormDataSchema formDataSchemaTest=new FormDataSchema(null,TEST_FORM_ID,
                TEST_VERSION,testFormData,testFormMetaData);
        doReturn(userList).when(mockUserDetails).getUserDetails();
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_FORM_ID, TEST_NAME, TEST_COMPONENTS, list,TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID,
                String.valueOf(TEST_CREATED_ON), TEST_UPDATED_BY_ID, String.valueOf(TEST_UPDATED_ON),Status.DISABLED);
        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
        Mockito.when(mockIdGeneratorImpl.nextId()).thenReturn(BigInteger.valueOf(Long.parseLong(TEST_FORM_ID)));
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA +formDataSchemaTest.getFormId())).thenReturn(false);
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
        mockMongoSave();
        Mockito.when(mockObjectMapper.convertValue(any(),eq(FormDataDefinition.class))).thenReturn(new FormDataDefinition());
        Assertions.assertThrows(InvalidInputException.class,()->mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
    }

    @Test
    void saveFormDataWithUniqueKeyCheckCollectionNotExist() throws IOException
    {
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        FormDataSchema formDataSchemaTest=new FormDataSchema(null,TEST_FORM_ID,
                TEST_VERSION,testFormData,testFormMetaData);
        doReturn(userList).when(mockUserDetails).getUserDetails();
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_FORM_ID, TEST_NAME, TEST_COMPONENTS, list,TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID,
                String.valueOf(TEST_CREATED_ON), TEST_UPDATED_BY_ID, String.valueOf(TEST_UPDATED_ON),Status.DISABLED);
        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
        Mockito.when(mockIdGeneratorImpl.nextId()).thenReturn(BigInteger.valueOf(Long.parseLong(TEST_FORM_ID)));
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA +formDataSchemaTest.getFormId())).thenReturn(false);
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
        Mockito.when(mockMongoTemplate.save(any(),anyString())).thenThrow(new MongoException("Duplicate key"));
        Mockito.when(mockObjectMapper.convertValue(any(),eq(FormDataDefinition.class))).thenReturn(new FormDataDefinition());
        Assertions.assertThrows(InvalidInputException.class,()->mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
    }

    @Test
    void saveFormDataWithUniqueKeyCheckWithMongoErrorCode() throws IOException
    {
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        FormDataSchema formDataSchemaTest=new FormDataSchema(null,TEST_FORM_ID,
                TEST_VERSION,testFormData,testFormMetaData);
        doReturn(userList).when(mockUserDetails).getUserDetails();
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_FORM_ID, TEST_NAME, TEST_COMPONENTS, list,TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID,
                String.valueOf(TEST_CREATED_ON), TEST_UPDATED_BY_ID, String.valueOf(TEST_UPDATED_ON),Status.DISABLED);
        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
        Mockito.when(mockIdGeneratorImpl.nextId()).thenReturn(BigInteger.valueOf(Long.parseLong(TEST_FORM_ID)));
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
        mockMongoSave();
        Mockito.when(mockObjectMapper.convertValue(any(),eq(FormDataDefinition.class))).thenReturn(new FormDataDefinition());
        Assertions.assertThrows(InvalidInputException.class,()->mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
    }

    public void mockMongoSave()
    {
        Mockito.when(mockMongoTemplate.save(any(),anyString())).thenThrow(new RuntimeException(DUPLICATE_KEY_ERROR_CODE));
    }

    @Test
    void saveFormDataWithUniqueKeyCollectionExist() throws IOException
    {
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        FormDataSchema formDataSchemaTest=new FormDataSchema(null,TEST_FORM_ID,
                TEST_VERSION,testFormData,testFormMetaData);
        doReturn(userList).when(mockUserDetails).getUserDetails();
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_FORM_ID, TEST_NAME, TEST_COMPONENTS, list,TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID,
                String.valueOf(TEST_CREATED_ON), TEST_UPDATED_BY_ID, String.valueOf(TEST_UPDATED_ON),Status.DISABLED);
        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
        Mockito.when(mockIdGeneratorImpl.nextId()).thenReturn(BigInteger.valueOf(Long.parseLong(TEST_FORM_ID)));
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
        Mockito.when(mockMongoTemplate.save(any(),anyString())).thenThrow(new RuntimeException("duplicate key"));
        Mockito.when(mockObjectMapper.convertValue(any(),eq(FormDataDefinition.class))).thenReturn(new FormDataDefinition());
        Assertions.assertThrows(InvalidInputException.class,()->mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
    }

    @Test
    void saveFormDataWithUniqueKeyCheckNotNull() throws IOException
    {
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        FormDataSchema formDataSchemaTest=new FormDataSchema("1",TEST_FORM_ID,
                TEST_VERSION,testFormData,testFormMetaData);
        doReturn(userList).when(mockUserDetails).getUserDetails();
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_FORM_ID, TEST_NAME, TEST_COMPONENTS, list,TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID,
                String.valueOf(TEST_CREATED_ON), TEST_UPDATED_BY_ID, String.valueOf(TEST_UPDATED_ON),Status.DISABLED);
        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
        Mockito.when(mockIdGeneratorImpl.nextId()).thenReturn(BigInteger.valueOf(Long.parseLong(TEST_FORM_ID)));
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
        Bson filter = new Document("_id", "1");
        Bson update = Updates.combine(
                Updates.set(FORM_DATA, null),
                Updates.set(VERSION, "2"));
        Mockito.doThrow(new RuntimeException(DUPLICATE_KEY_ERROR_CODE)).when(mockMongoCollection).updateOne(filter, update);
        Mockito.when(mockObjectMapper.convertValue(any(),eq(FormDataDefinition.class))).thenReturn(new FormDataDefinition());
        Assertions.assertThrows(InvalidInputException.class,()->mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
    }

    @Test
    void saveFormDataEmptyDocumentIdTest() throws IOException
    {
        doReturn(userList).when(mockUserDetails).getUserDetails();  
        Mockito.when(mockIdGeneratorImpl.nextId()).thenReturn(BigInteger.valueOf(Long.parseLong(TEST_FORM_ID)));
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        FormDataSchema formDataSchemaTest=new FormDataSchema(null,TEST_FORM_ID,
                TEST_VERSION,testFormData,testFormMetaData);
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
        Document document=new Document();
        document.append("version",1);
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_FORM_ID, TEST_NAME, TEST_COMPONENTS, list,TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID,
                String.valueOf(TEST_CREATED_ON), TEST_UPDATED_BY_ID,String.valueOf(TEST_UPDATED_ON),Status.DISABLED);
        Mockito.when(mockFormService.getRuntimeFormById(anyString())).thenReturn(formResponseSchemaTest);
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
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_FORM_ID, TEST_NAME, TEST_COMPONENTS, list,TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID,
                String.valueOf(TEST_CREATED_ON), TEST_UPDATED_BY_ID,String.valueOf(TEST_UPDATED_ON),Status.DISABLED);
        Mockito.when(mockFormService.getRuntimeFormById(anyString())).thenReturn(formResponseSchemaTest);
        Assertions.assertNotNull(mockFormDataServiceImpl.updateFormData(formDataSchemaTest));
    }

    @Test
    void updateFormDataTestException() throws JsonProcessingException
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
        Bson filter = new Document("_id", "123");
        Bson update = Updates.combine(
                Updates.set(FORM_DATA, testFormData),
                Updates.set(VERSION, "2"),
                Updates.set(FORM_META_DATA, testFormMetaData));
        Mockito.doThrow(new RuntimeException(DUPLICATE_KEY_ERROR_CODE)).when(mockMongoCollection).updateOne(filter, update);
        Assertions.assertThrows(InvalidInputException.class,()->mockFormDataServiceImpl.updateFormData(formDataSchemaTest));
    }

    @Test
    void getAllFormDataByFormIdAggregationEmptySortTest()
    {
        Mockito.when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
        List<Map> aggregateList=new ArrayList<>();
        Map<String,Object> map=new HashMap<>();
        map.put(UNDERSCORE_ID,TEST_ID_VALUE);
        aggregateList.add(map);
        Document document=new Document();
        List<Document> documentList=new ArrayList<>();
        documentList.add(document);
        Mockito.when(mockMongoTemplate.aggregate((Aggregation) any(),anyString(),eq(Document.class))).thenReturn( new AggregationResults<>(documentList,document));
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,TEST_RELATIONS,FILTER_VERSION_2,EMPTY_STRING, EMPTY_STRING));
    }

    @Test
    void getAllFormDataByFormIdAggregationSortTest() {
        Mockito.when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
        List<Map> aggregateList=new ArrayList<>();
        Map<String,Object> map=new HashMap<>();
        map.put(UNDERSCORE_ID,TEST_ID_VALUE);
        aggregateList.add(map);
        Document document=new Document();
        List<Document> documentList=new ArrayList<>();
        documentList.add(document);
        Mockito.when(mockMongoTemplate.aggregate((Aggregation) any(),anyString(),eq(Document.class))).thenReturn( new AggregationResults<>(documentList,document));
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,TEST_RELATIONS,FILTER,TEST_SORT_BY, TEST_SORT_ORDER));
    }

    @Test
    void getAllFormDataByFormIdPaginationEmptySortTest() {
        Mockito.when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
        List<Map> aggregateList=new ArrayList<>();
        Map<String,Object> map=new HashMap<>();
        List<Map<String,Object>> dataList=new ArrayList<>();
        List<Map<String,Object>> metaDataList=new ArrayList<>();
        Map<String,Object> metaDataMap=new HashMap<>();
        metaDataMap.put(COUNT,10);
        metaDataList.add(metaDataMap);
        Map<String,Object> dataMap=new HashMap<>();
        dataMap.put(UNDERSCORE_ID,TEST_ID_VALUE);
        dataList.add(dataMap);
        map.put(UNDERSCORE_ID,TEST_ID_VALUE);
        map.put(DATA,dataList);
        map.put(METADATA,metaDataList);
        aggregateList.add(map);
        Document document=new Document(map);
        List<Document> documentList=new ArrayList<>();
        documentList.add(document);
        Mockito.when(mockMongoTemplate.aggregate((Aggregation) any(),anyString(),eq(Document.class))).thenReturn( new AggregationResults<>(documentList,document));
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,TEST_RELATIONS,FILTER,EMPTY_STRING,EMPTY_STRING, PageRequest.of(1,5)));
    }

    @Test
    void getAllFormDataByFormIdPaginationSortTest() {
        Mockito.when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
        List<Map> aggregateList=new ArrayList<>();
        Map<String,Object> map=new HashMap<>();
        List<Map<String,Object>> dataList=new ArrayList<>();
        List<Map<String,Object>> metaDataList=new ArrayList<>();
        Map<String,Object> metaDataMap=new HashMap<>();
        metaDataMap.put(COUNT,10);
        metaDataList.add(metaDataMap);
        Map<String,Object> dataMap=new HashMap<>();
        dataMap.put(UNDERSCORE_ID,TEST_ID_VALUE);
        dataList.add(dataMap);
        map.put(UNDERSCORE_ID,TEST_ID_VALUE);
        map.put(DATA,dataList);
        map.put(METADATA,metaDataList);
        aggregateList.add(map);
        Document document=new Document(map);
        List<Document> documentList=new ArrayList<>();
        documentList.add(document);
        Mockito.when(mockMongoTemplate.aggregate((Aggregation) any(),anyString(),eq(Document.class))).thenReturn( new AggregationResults<>(documentList,document));
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,TEST_RELATIONS,FILTER,TEST_SORT_BY,TEST_SORT_ORDER, PageRequest.of(1,5)));
    }

    @Test
    void getAllFormDataByFormIdFilterAndRelationsTest() {
        Mockito.when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
        List<FormDataDefinition> formDataDefinitionsList=new ArrayList<>();
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        FormDataDefinition formDataDefinition=new FormDataDefinition();
        formDataDefinition.setId(TEST_ID_VALUE);
        formDataDefinition.setFormData(testFormData);
        formDataDefinition.setFormMetaData(testFormMetaData);
        formDataDefinition.setVersion(TEST_VERSION);
        formDataDefinition.setCreatedById(TEST_CREATED_BY_ID);
        formDataDefinition.setCreatedOn(String.valueOf(Instant.now()));
        formDataDefinition.setUpdatedById(TEST_UPDATED_BY_ID);
        formDataDefinition.setUpdatedOn(String.valueOf(Instant.now()));
        formDataDefinitionsList.add(formDataDefinition);
        Mockito.when(mockMongoTemplate.find(any(),eq(FormDataDefinition.class),anyString())).thenReturn(formDataDefinitionsList);
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,EMPTY_STRING,FILTER, EMPTY_STRING, EMPTY_STRING));
    }

    @Test
    void getAllFormDataByFormIdFilterAndRelationsSortBySortOrderTest() {
        Mockito.when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
        List<FormDataDefinition> formDataDefinitionsList=new ArrayList<>();
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        FormDataDefinition formDataDefinition=new FormDataDefinition();
        formDataDefinition.setId(TEST_ID_VALUE);
        formDataDefinition.setFormData(testFormData);
        formDataDefinition.setFormMetaData(testFormMetaData);
        formDataDefinition.setVersion(TEST_VERSION);
        formDataDefinition.setCreatedById(TEST_CREATED_BY_ID);
        formDataDefinition.setCreatedOn(String.valueOf(Instant.now()));
        formDataDefinition.setUpdatedById(TEST_UPDATED_BY_ID);
        formDataDefinition.setUpdatedOn(String.valueOf(Instant.now()));
        formDataDefinitionsList.add(formDataDefinition);
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,EMPTY_STRING,FILTER,TEST_SORT_BY,TEST_SORT_ORDER));
    }

    @Test
    void getAllFormDataByFormIdEmptySortPaginationTest() {
        List<FormDataDefinition> formDataDefinitionsList=new ArrayList<>();
        FormDataDefinition formDataDefinition=new FormDataDefinition();
        formDataDefinition.setId(TEST_ID_VALUE);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        formDataDefinition.setFormData(testFormData);
        formDataDefinition.setFormMetaData(testFormMetaData);
        formDataDefinition.setVersion(TEST_VERSION);
        formDataDefinition.setCreatedById(TEST_CREATED_BY_ID);
        formDataDefinition.setCreatedOn(String.valueOf(Instant.now()));
        formDataDefinition.setUpdatedById(TEST_UPDATED_BY_ID);
        formDataDefinition.setUpdatedOn(String.valueOf(Instant.now()));
        formDataDefinitionsList.add(formDataDefinition);
        Mockito.when(mockMongoTemplate.find(any(),eq(FormDataDefinition.class),anyString())).thenReturn(formDataDefinitionsList);
        Mockito.when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,EMPTY_STRING,FILTER, EMPTY_STRING,EMPTY_STRING, PageRequest.of(1,5)));
    }

    @Test
    void getAllFormDataByFormIdSortPaginationTest() {
        List<FormDataDefinition> formDataDefinitionsList=new ArrayList<>();
        FormDataDefinition formDataDefinition=new FormDataDefinition();
        formDataDefinition.setId(TEST_ID_VALUE);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        formDataDefinition.setFormData(testFormData);
        formDataDefinition.setFormMetaData(testFormMetaData);
        formDataDefinition.setVersion(TEST_VERSION);
        formDataDefinition.setCreatedById(TEST_CREATED_BY_ID);
        formDataDefinition.setCreatedOn(String.valueOf(Instant.now()));
        formDataDefinition.setUpdatedById(TEST_UPDATED_BY_ID);
        formDataDefinition.setUpdatedOn(String.valueOf(Instant.now()));
        formDataDefinitionsList.add(formDataDefinition);
        Mockito.when(mockMongoTemplate.find(any(),eq(FormDataDefinition.class),anyString())).thenReturn(formDataDefinitionsList);
        Mockito.when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,EMPTY_STRING,FILTER,TEST_SORT_BY,TEST_SORT_ORDER, PageRequest.of(1,5)));
    }

    @Test
    void getAllFormDataByFormIdEmptySortTest()
    {
        Mockito.when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
        List<Map> aggregateList=new ArrayList<>();
        Map<String,Object> map=new HashMap<>();
        map.put(UNDERSCORE_ID,TEST_ID_VALUE);
        aggregateList.add(map);
        Document document=new Document(map);
        List<Document> documentList=new ArrayList<>();
        documentList.add(document);
        Mockito.when(mockMongoTemplate.aggregate((Aggregation) any(),anyString(),eq(Document.class))).thenReturn( new AggregationResults<>(documentList,document));
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,TEST_RELATIONS,Q,EMPTY_STRING,EMPTY_STRING));
    }

    @Test
    void getAllFormDataByFormIdEmptySortWithoutRelationsTest()
    {
        Mockito.when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
        List<FormDataDefinition> formDataDefinitionsList=new ArrayList<>();
        FormDataDefinition formDataDefinition=new FormDataDefinition();
        formDataDefinition.setId(TEST_ID_VALUE);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        formDataDefinition.setFormData(testFormData);
        formDataDefinition.setFormMetaData(testFormMetaData);
        formDataDefinition.setVersion(TEST_VERSION);
        formDataDefinition.setCreatedById(TEST_CREATED_BY_ID);
        formDataDefinition.setCreatedOn(String.valueOf(Instant.now()));
        formDataDefinition.setUpdatedById(TEST_UPDATED_BY_ID);
        formDataDefinition.setUpdatedOn(String.valueOf(Instant.now()));
        formDataDefinitionsList.add(formDataDefinition);
        Mockito.when(mockMongoTemplate.find(any(),eq(FormDataDefinition.class),anyString())).thenReturn(formDataDefinitionsList);
        List<Map> aggregateList=new ArrayList<>();
        Map<String,Object> map=new HashMap<>();
        map.put(UNDERSCORE_ID,TEST_ID_VALUE);
        aggregateList.add(map);
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,EMPTY_STRING,Q,EMPTY_STRING,EMPTY_STRING));
    }

    @Test
    void getAllFormDataByFormIdSortWithoutRelationsTest()
    {
        Mockito.when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
        List<FormDataDefinition> formDataDefinitionsList=new ArrayList<>();
        FormDataDefinition formDataDefinition=new FormDataDefinition();
        formDataDefinition.setId(TEST_ID_VALUE);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        formDataDefinition.setFormData(testFormData);
        formDataDefinition.setFormMetaData(testFormMetaData);
        formDataDefinition.setVersion(TEST_VERSION);
        formDataDefinition.setCreatedById(TEST_CREATED_BY_ID);
        formDataDefinition.setCreatedOn(String.valueOf(Instant.now()));
        formDataDefinition.setUpdatedById(TEST_UPDATED_BY_ID);
        formDataDefinition.setUpdatedOn(String.valueOf(Instant.now()));
        formDataDefinitionsList.add(formDataDefinition);
        List<Map> aggregateList=new ArrayList<>();
        Map<String,Object> map=new HashMap<>();
        map.put(UNDERSCORE_ID,TEST_ID_VALUE);
        aggregateList.add(map);
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,EMPTY_STRING,Q,TEST_SORT_BY,TEST_SORT_ORDER));
    }

    @Test
    void getAllFormDataByFormIdAndQ()
    {
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        FormDataSchema formDataSchemaTest = new FormDataSchema(null, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA +formDataSchemaTest.getFormId())).thenReturn(true);
        FormDataDefinition formDataDefinitionTest=new FormDataDefinition(TEST_ID,TEST_FORM_ID,TEST_VERSION,testFormData,testFormMetaData);
        formDataDefinitionTest.setCreatedById(TEST_CREATED_BY_ID);
        formDataDefinitionTest.setCreatedOn(String.valueOf(TEST_CREATED_ON));
        formDataDefinitionTest.setUpdatedById(TEST_UPDATED_BY_ID);
        formDataDefinitionTest.setUpdatedOn(String.valueOf(TEST_UPDATED_ON));
        List<Map> aggregateList=new ArrayList<>();
        Map<String,Object> map=new HashMap<>();
        map.put(UNDERSCORE_ID,TEST_ID_VALUE);
        aggregateList.add(map);
        Document document=new Document();
        List<Document> documentList=new ArrayList<>();
        documentList.add(document);
        Mockito.when(mockMongoTemplate.aggregate((Aggregation) any(),anyString(),eq(Document.class))).thenReturn( new AggregationResults<>(documentList,document));
       Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,TEST_RELATIONS, EMPTY_STRING, CREATED_ON, DESCENDING));
    }

    @Test
    void getAllFormDataByFormIdAndQEmptySortPaginationTest()
    {
        List<FormDataDefinition> formDataDefinitionsList=new ArrayList<>();
        FormDataDefinition formDataDefinition=new FormDataDefinition();
        formDataDefinition.setId(TEST_ID_VALUE);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        formDataDefinition.setFormData(testFormData);
        formDataDefinition.setFormMetaData(testFormMetaData);
        formDataDefinition.setVersion(TEST_VERSION);
        formDataDefinition.setCreatedById(TEST_CREATED_BY_ID);
        formDataDefinition.setCreatedOn(String.valueOf(Instant.now()));
        formDataDefinition.setUpdatedById(TEST_UPDATED_BY_ID);
        formDataDefinition.setUpdatedOn(String.valueOf(Instant.now()));
        formDataDefinitionsList.add(formDataDefinition);
        FormDataSchema formDataSchemaTest = new FormDataSchema(null, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA +formDataSchemaTest.getFormId())).thenReturn(true);
        Mockito.when(mockMongoTemplate.find(any(),eq(FormDataDefinition.class),anyString())).thenReturn(formDataDefinitionsList);
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,EMPTY_STRING, Q,EMPTY_STRING,EMPTY_STRING,PageRequest.of(1,5)));
    }

    @Test
    void getAllFormDataByFormIdAndQSortPaginationTest()
    {
        List<FormDataDefinition> formDataDefinitionsList=new ArrayList<>();
        FormDataDefinition formDataDefinition=new FormDataDefinition();
        formDataDefinition.setId(TEST_ID_VALUE);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        formDataDefinition.setFormData(testFormData);
        formDataDefinition.setFormMetaData(testFormMetaData);
        formDataDefinition.setVersion(TEST_VERSION);
        formDataDefinition.setCreatedById(TEST_CREATED_BY_ID);
        formDataDefinition.setCreatedOn(String.valueOf(Instant.now()));
        formDataDefinition.setUpdatedById(TEST_UPDATED_BY_ID);
        formDataDefinition.setUpdatedOn(String.valueOf(Instant.now()));
        formDataDefinitionsList.add(formDataDefinition);
        FormDataSchema formDataSchemaTest = new FormDataSchema(null, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA +formDataSchemaTest.getFormId())).thenReturn(true);
        Mockito.when(mockMongoTemplate.find(any(),eq(FormDataDefinition.class),anyString())).thenReturn(formDataDefinitionsList);
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,EMPTY_STRING, Q,TEST_SORT_BY,TEST_SORT_ORDER,PageRequest.of(1,5)));
    }

    @Test
    void getAllFormDataByFormIdEmptyRelationsPaginationTest()
    {
        List<FormDataDefinition> formDataDefinitionsList=new ArrayList<>();
        FormDataDefinition formDataDefinition=new FormDataDefinition();
        formDataDefinition.setId(TEST_ID_VALUE);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        formDataDefinition.setFormData(testFormData);
        formDataDefinition.setFormMetaData(testFormMetaData);
        formDataDefinition.setVersion(TEST_VERSION);
        formDataDefinition.setCreatedById(TEST_CREATED_BY_ID);
        formDataDefinition.setCreatedOn(String.valueOf(Instant.now()));
        formDataDefinition.setUpdatedById(TEST_UPDATED_BY_ID);
        formDataDefinition.setUpdatedOn(String.valueOf(Instant.now()));
        formDataDefinitionsList.add(formDataDefinition);
        FormDataSchema formDataSchemaTest = new FormDataSchema(null, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA +formDataSchemaTest.getFormId())).thenReturn(true);
        Mockito.when(mockMongoTemplate.find(any(),eq(FormDataDefinition.class),anyString())).thenReturn(formDataDefinitionsList);
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,EMPTY_STRING));
    }

    @Test
    void getFormDataByFormIdAndIdEmptyRelationsTest()
    {
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        FormDataSchema formDataSchemaTest=new FormDataSchema(TEST_ID_VALUE,TEST_FORM_ID,
                TEST_VERSION,testFormData,testFormMetaData);
        LinkedHashMap<String, Object> formDataMap = new LinkedHashMap<>();
        formDataMap.put(UNDERSCORE_ID,Long.parseLong(UNDERSCORE_ID_VALUE));
        formDataMap.put(FORM_ID,TEST_FORM_ID);
        formDataMap.put(VERSION, String.valueOf(1));
        formDataMap.put(FORM_META_DATA, formDataSchemaTest.getFormMetaData());
        formDataMap.put(FORM_DATA, formDataSchemaTest.getFormData());
        formDataMap.put(CREATED_ON, Date.from(Instant.now()));
        formDataMap.put(CREATED_BY_ID,CREATED_BY_USER_ID);
        formDataMap.put(CREATED_BY_NAME, CREATED_BY_USER_NAME);
        formDataMap.put(UPDATED_ON,Date.from(Instant.now()));
        formDataMap.put(UPDATED_BY_ID,UPDATED_BY_USER_ID);
        formDataMap.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        Document document = new Document(formDataMap);
        document.put(UNDERSCORE_ID,TEST_ID_VALUE);
        Mockito.when(mockMongoTemplate.getCollection(any())).thenReturn(mongoCollectionDocument);
        Mockito.when(mongoCollectionDocument.find(any(Bson.class))).thenReturn(mockDocuments);
        Mockito.when(mockDocuments.iterator()).thenReturn(mongoCursor);
        Mockito.when(mongoCursor.hasNext()).thenReturn(true).thenReturn(false);
        Mockito.when(mongoCursor.next()).thenReturn(document);
        List<FormDataDefinition> formDataDefinitionsList=new ArrayList<>();
        FormDataDefinition formDataDefinition=new FormDataDefinition();
        formDataDefinition.setId(TEST_ID_VALUE);
        formDataDefinition.setFormData(testFormData);
        formDataDefinition.setFormMetaData(testFormMetaData);
        formDataDefinition.setVersion(TEST_VERSION);
        formDataDefinition.setCreatedById(TEST_CREATED_BY_ID);
        formDataDefinition.setCreatedOn(String.valueOf(Instant.now()));
        formDataDefinition.setUpdatedById(TEST_UPDATED_BY_ID);
        formDataDefinition.setUpdatedOn(String.valueOf(Instant.now()));
        formDataDefinitionsList.add(formDataDefinition);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA +formDataSchemaTest.getFormId())).thenReturn(true);
        Assertions.assertNotNull(mockFormDataServiceImpl.getFormDataByFormIdAndId(TEST_FORM_ID,TEST_ID,EMPTY_STRING));
    }

    @Test
    void getFormDataByFormIdAndIdRelationsTest()
    {
        List<Map> aggregateList=new ArrayList<>();
        Map<String,Object> map=new HashMap<>();
        map.put(UNDERSCORE_ID,TEST_ID_VALUE);
        List<Map<String,Object>> dataMapList=new ArrayList<>();
        List<Map<String,Object>> metaDatList=new ArrayList<>();
        Map<String,Object> dataMap=new HashMap<>();
        Map<String,Object> metaDataMap=new HashMap<>();
        metaDataMap.put(COUNT,10);
        metaDatList.add(metaDataMap);
        dataMap.put(UNDERSCORE_ID,TEST_ID_VALUE);
        dataMapList.add(dataMap);
        map.put("data",dataMapList);
        map.put("metaData",metaDatList);
        aggregateList.add(map);
        Document document=new Document();
        List<Document> documentList=new ArrayList<>();
        documentList.add(document);
        Mockito.when(mockMongoTemplate.aggregate((Aggregation) any(),anyString(),eq(Document.class))).thenReturn( new AggregationResults<>(documentList,document));
        Assertions.assertNotNull(mockFormDataServiceImpl.getFormDataByFormIdAndId(TEST_FORM_ID,TEST_ID,TEST_RELATIONS));
    }

    @Test
    void getAllFormDataByFormIdRelationsPaginationTest()
    {
        List<FormDataDefinition> formDataDefinitionsList=new ArrayList<>();
        FormDataDefinition formDataDefinition=new FormDataDefinition();
        formDataDefinition.setId(TEST_ID_VALUE);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        formDataDefinition.setFormData(testFormData);
        formDataDefinition.setFormMetaData(testFormMetaData);
        formDataDefinition.setVersion(TEST_VERSION);
        formDataDefinition.setCreatedById(TEST_CREATED_BY_ID);
        formDataDefinition.setCreatedOn(String.valueOf(Instant.now()));
        formDataDefinition.setUpdatedById(TEST_UPDATED_BY_ID);
        formDataDefinition.setUpdatedOn(String.valueOf(Instant.now()));
        formDataDefinitionsList.add(formDataDefinition);
        FormDataSchema formDataSchemaTest = new FormDataSchema(null, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA +formDataSchemaTest.getFormId())).thenReturn(true);
        List<Map> aggregateList=new ArrayList<>();
        Map<String,Object> map=new HashMap<>();
        map.put(UNDERSCORE_ID,TEST_ID_VALUE);
        List<Map<String,Object>> dataMapList=new ArrayList<>();
        List<Map<String,Object>> metaDatList=new ArrayList<>();
        Map<String,Object> dataMap=new HashMap<>();
        Map<String,Object> metaDataMap=new HashMap<>();
        metaDataMap.put(COUNT,10);
        metaDatList.add(metaDataMap);
        dataMap.put(UNDERSCORE_ID,TEST_ID_VALUE);
        dataMapList.add(dataMap);
        map.put("data",dataMapList);
        map.put("metaData",metaDatList);
        aggregateList.add(map);
        Document document=new Document(map);
        List<Document> documentList=new ArrayList<>();
        documentList.add(document);
        Mockito.when(mockMongoTemplate.aggregate((Aggregation) any(),anyString(),eq(Document.class))).thenReturn( new AggregationResults<>(documentList,document));
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,TEST_RELATIONS));
    }

    @Test
    void getAllFormDataByFormIdAndQEmptySortBySortOrderPaginationTest()
    {
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        FormDataSchema formDataSchemaTest = new FormDataSchema(null, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA +formDataSchemaTest.getFormId())).thenReturn(true);
        FormDataDefinition formDataDefinitionTest=new FormDataDefinition(TEST_ID,TEST_FORM_ID,
                TEST_VERSION,testFormData,testFormMetaData);
        formDataDefinitionTest.setCreatedById(TEST_CREATED_BY_ID);
        formDataDefinitionTest.setCreatedOn(String.valueOf(TEST_CREATED_ON));
        formDataDefinitionTest.setUpdatedById(TEST_UPDATED_BY_ID);
        formDataDefinitionTest.setUpdatedOn(String.valueOf(TEST_UPDATED_ON));
        List<Map> aggregateList=new ArrayList<>();
        Map<String,Object> map=new HashMap<>();
        map.put(UNDERSCORE_ID,TEST_ID_VALUE);
        List<Map<String,Object>> dataMapList=new ArrayList<>();
        List<Map<String,Object>> metaDatList=new ArrayList<>();
        Map<String,Object> dataMap=new HashMap<>();
        Map<String,Object> metaDataMap=new HashMap<>();
        metaDataMap.put(COUNT,10);
        metaDatList.add(metaDataMap);
        dataMap.put(UNDERSCORE_ID,TEST_ID_VALUE);
        dataMapList.add(dataMap);
        map.put("data",dataMapList);
        map.put("metaData",metaDatList);
        aggregateList.add(map);
        Document document=new Document(map);
        List<Document> documentList=new ArrayList<>();
        documentList.add(document);
        Mockito.when(mockMongoTemplate.aggregate((Aggregation) any(),anyString(),eq(Document.class))).thenReturn( new AggregationResults<>(documentList,document));
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,TEST_RELATIONS, Q, EMPTY_STRING, EMPTY_STRING, PageRequest.of(0,10)));
    }

    @Test
    void getAllFormDataByFormIdAndQSortBySortOrderPaginationTest()
    {
        Map<String, Object> testFormData2 = new HashMap<>();
        testFormData2.put(NAME, NAME_VALUE);
        testFormData2.put(AGE,AGE_VALUE);
        testFormData2.put(DATA,AGE_VALUE);
        testFormData2.put(CONTENT,AGE_VALUE);
        testFormData2.put(PAGE,AGE_VALUE);
        testFormData2.put(SIZE,AGE_VALUE);
        testFormData2.put(TOTAL_PAGES,AGE_VALUE);
        testFormData2.put(TOTAL_ELEMENTS,1L);
        testFormData2.put(NUMBER_OF_ELEMENTS,AGE_VALUE);
        LinkedHashMap<String,Object> data = new LinkedHashMap<>();
        LinkedHashMap data1 = new LinkedHashMap<>();
        data1.put("abc","abc");
        data.put(FORM_DATA,data1);
        ArrayList list1 = new ArrayList<>();
        list1.add(data);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        FormDataSchema formDataSchemaTest = new FormDataSchema(null, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA +formDataSchemaTest.getFormId())).thenReturn(true);
        List<Map> aggregateList=new ArrayList<>();
        Map<String,Object> map=new HashMap<>();
        map.put(UNDERSCORE_ID,TEST_ID_VALUE);
        List<Map<String,Object>> dataMapList=new ArrayList<>();
        List<Map<String,Object>> metaDatList=new ArrayList<>();
        Map<String,Object> dataMap=new HashMap<>();
        Map<String,Object> metaDataMap=new HashMap<>();
        metaDataMap.put(COUNT,10);
        metaDatList.add(metaDataMap);
        dataMap.put(UNDERSCORE_ID,TEST_ID_VALUE);
        dataMapList.add(dataMap);
        map.put("data",dataMapList);
        map.put("metaData",metaDatList);
        aggregateList.add(map);
        Document document=new Document(map);
        List<Document> documentList=new ArrayList<>();
        documentList.add(document);
            Mockito.when(mockMongoTemplate.aggregate((Aggregation) any(),anyString(),eq(Document.class))).thenReturn( new AggregationResults<>(documentList,document));
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,TEST_RELATIONS, Q,ID, DESCENDING, PageRequest.of(0,10)));
    }

    @Test
    void deleteAllFormDataByFormIdTest()
    {
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA + TEST_FORM_ID)).thenReturn(true);
        FormResponseSchema formResponseSchema=new FormResponseSchema("101","form1",new HashMap<>(),new ArrayList<>(),
                new HashMap<>(),"form",1,false,TEST_CREATED_BY_ID,String.valueOf(TEST_CREATED_ON),TEST_UPDATED_BY_ID,
                String.valueOf(TEST_UPDATED_ON), Status.DISABLED);
        Mockito.when(mockFormService.getRuntimeFormById(anyString())).thenReturn(formResponseSchema);
        mockFormDataServiceImpl.deleteAllFormDataByFormId(TEST_FORM_ID);
        verify(mockMongoTemplate, times(1)).dropCollection(TP_RUNTIME_FORM_DATA + TEST_FORM_ID);
    }
    @Test
    void deleteAllFormDataByFormIdTestException()
    {
        Assertions.assertThrows(InvalidInputException.class,()->mockFormDataServiceImpl.deleteAllFormDataByFormId(TEST_FORM_ID));
    }

    @Test
    void aggregateByFormIdFilterGroupByTest() {
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        FormDataSchema formDataSchemaTest = new FormDataSchema("1", TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA +formDataSchemaTest.getFormId())).thenReturn(true);
        List<Map> aggregateList=new ArrayList<>();
        Map<String,Object> map=new HashMap<>();
        map.put(UNDERSCORE_ID,TEST_ID_VALUE);
        List<Map<String,Object>> dataMapList=new ArrayList<>();
        List<Map<String,Object>> metaDatList=new ArrayList<>();
        Map<String,Object> dataMap=new HashMap<>();
        Map<String,Object> metaDataMap=new HashMap<>();
        metaDataMap.put(COUNT,10);
        metaDatList.add(metaDataMap);
        dataMap.put(UNDERSCORE_ID,TEST_ID_VALUE);
        dataMapList.add(dataMap);
        map.put("data",dataMapList);
        map.put("metaData",metaDatList);
        aggregateList.add(map);
        Document document=new Document();
        document.putAll(map);
        List<Document> documentList=new ArrayList<>();
        documentList.add(document);
        Mockito.when(mockMongoTemplate.aggregate((Aggregation) any(),anyString(),eq(Document.class))).thenReturn(new AggregationResults<>(documentList,document));
        Assertions.assertNotNull(mockFormDataServiceImpl.aggregateByFormIdFilterGroupBy(TEST_FORM_ID,FILTER,TEST_GROUP_BY,TEST_OPERATION));
    }
}
