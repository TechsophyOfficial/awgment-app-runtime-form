package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.techsophy.idgenerator.IdGeneratorImpl;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.FormDataSchema;
import com.techsophy.tsf.runtime.form.dto.FormResponseSchema;
import com.techsophy.tsf.runtime.form.entity.FormDataDefinition;
import com.techsophy.tsf.runtime.form.service.impl.FormDataAuditServiceImpl;
import com.techsophy.tsf.runtime.form.service.impl.FormDataServiceImpl;
import com.techsophy.tsf.runtime.form.service.impl.ValidationCheckServiceImpl;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import com.techsophy.tsf.runtime.form.utils.UserDetails;
import com.techsophy.tsf.runtime.form.utils.ValidateFormUtils;
import com.techsophy.tsf.runtime.form.utils.WebClientWrapper;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.CREATED_BY_ID;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.CREATED_BY_NAME;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.CREATED_ON;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.DEFAULT_PAGE_LIMIT;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.EMPTY_STRING;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.ID;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.NULL;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.UPDATED_BY_ID;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.UPDATED_BY_NAME;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.UPDATED_ON;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles(TEST_ACTIVE_PROFILE)
@ExtendWith({SpringExtension.class})
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
    ValidationCheckServiceImpl mockValidationCheckService;
    @Mock
    ObjectMapper mockObjectMapper;

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

//    @Test
//    void saveFormDataCollectionExistsNewDocumentTest() throws JsonProcessingException
//    {
//        doReturn(userList).when(mockUserDetails).getUserDetails();
//        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        Map<String, Object> testFormData = new HashMap<>();
//        testFormData.put(NAME, NAME_VALUE);
//        testFormData.put(AGE,AGE_VALUE);
//        FormDataSchema formDataSchemaTest = new FormDataSchema(EMPTY_STRING, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
//        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_FORM_ID, TEST_NAME, TEST_COMPONENTS, TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID, TEST_CREATED_ON, TEST_CREATED_BY_NAME, TEST_UPDATED_BY_ID, TEST_UPDATED_ON, TEST_UPDATED_BY_NAME);
//        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
//        Map<String,Map<String,Object>> schemaMap=new HashMap<>();
//        Map<String,Object> fieldsMap=new HashMap<>();
//        fieldsMap.put(REQUIRED,false);
//        fieldsMap.put(UNIQUE,false);
//        schemaMap.put(NAME,fieldsMap);
//        Map<String, Object> givenData = new HashMap<>();
//        givenData.put(NAME, NAME_VALUE);
//        try (MockedStatic<ValidateFormUtils> mockValidateFormUtils = Mockito.mockStatic(ValidateFormUtils.class))
//        {
//            mockValidateFormUtils.when(() -> ValidateFormUtils.getSchema(formResponseSchemaTest.getComponents()))
//                    .thenReturn(schemaMap);
//            Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_+formDataSchemaTest.getFormId())).thenReturn(true);
//            Mockito.when(mockIdGeneratorImpl.nextId()).thenReturn(BigInteger.valueOf(Long.parseLong(TEST_FORM_ID)));
//            Mockito.when(mockMongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_+formDataSchemaTest.getFormId())).thenReturn(mockMongoCollection);
//            Map<String, Object> formDataMap = new HashMap<>();
//            formDataMap.put(UNDERSCORE_ID,Long.parseLong(UNDERSCORE_ID_VALUE));
//            formDataMap.put(FORM_ID,TEST_FORM_ID);
//            formDataMap.put(VERSION, String.valueOf(1));
//            formDataMap.put(FORM_META_DATA, formDataSchemaTest.getFormMetadata());
//            formDataMap.put(FORM_DATA, formDataSchemaTest.getFormData());
//            formDataMap.put(CREATED_ON, Instant.now());
//            formDataMap.put(CREATED_BY_ID,CREATED_BY_USER_ID);
//            formDataMap.put(CREATED_BY_NAME, CREATED_BY_USER_NAME);
//            formDataMap.put(UPDATED_ON,TEST_UPDATED_ON);
//            formDataMap.put(UPDATED_BY_ID,UPDATED_BY_USER_ID);
//            formDataMap.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
//            Document document = new Document(formDataMap);
//            Mockito.when(mockMongoTemplate.save(any(),any())).thenReturn(document);
//            FormDataAuditSchema formDataAuditSchemaTest = new FormDataAuditSchema(TEST_ID_VALUE, TEST_FORM_DATA_ID,TEST_FORM_ID,TEST_VERSION, testFormData,testFormMetaData);
//            Mockito.when(mockFormDataAuditServiceImpl.saveFormData(formDataAuditSchemaTest)).thenReturn(new FormDataAuditResponse(TEST_ID_VALUE,TEST_VERSION));
//            Mockito.when(mockValidationCheckService.allFieldsValidations(any(),any(),any(),any())).thenReturn(10);
//            mockFormDataServiceImpl.saveFormData(formDataSchemaTest);
//            verify(mockMongoTemplate,times(1)).save(any(),any());
//            verify(mockFormDataAuditServiceImpl,times(1)).saveFormData(formDataAuditSchemaTest);
//        }
//    }

//    @Test
//    void getAllFormDataByFormIdEmptySortBySortOrderTest()
//    {
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        Map<String, Object> testFormData = new HashMap<>();
//        testFormData.put(NAME, NAME_VALUE);
//        testFormData.put(AGE,AGE_VALUE);
//        FormDataSchema formDataSchemaTest = new FormDataSchema(null, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
//        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_+formDataSchemaTest.getFormId())).thenReturn(true);
//        FormDataDefinition formDataDefinitionTest=new FormDataDefinition(BigInteger.valueOf(Long.parseLong(TEST_ID)),
//                TEST_VERSION,testFormData,testFormMetaData);
//        formDataDefinitionTest.setCreatedById(BigInteger.valueOf(Long.parseLong(TEST_CREATED_BY_ID)));
//        formDataDefinitionTest.setCreatedOn(TEST_CREATED_ON);
//        formDataDefinitionTest.setCreatedByName(TEST_CREATED_BY_NAME);
//        formDataDefinitionTest.setUpdatedById(BigInteger.valueOf(Long.parseLong(TEST_UPDATED_BY_ID)));
//        formDataDefinitionTest.setUpdatedByName(TEST_UPDATED_BY_NAME);
//        formDataDefinitionTest.setUpdatedOn(TEST_UPDATED_ON);
//        Mockito.when(mockMongoTemplate.find(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(List.of(formDataDefinitionTest));
//        mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,TEST_RELATIONS, TEST_FILTER, EMPTY_STRING, EMPTY_STRING);
//        verify(mockMongoTemplate,times(1)).find(any(),any(),any());
//    }

//    @Test
//    void getAllFormDataByFormIdSortBySortOrderTest()
//    {
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        Map<String, Object> testFormData = new HashMap<>();
//        testFormData.put(NAME, NAME_VALUE);
//        testFormData.put(AGE,AGE_VALUE);
//        FormDataSchema formDataSchemaTest = new FormDataSchema(null, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
//        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_+formDataSchemaTest.getFormId())).thenReturn(true);
//        FormDataDefinition formDataDefinitionTest=new FormDataDefinition(BigInteger.valueOf(Long.parseLong(TEST_ID)),
//                TEST_VERSION,testFormData,testFormMetaData);
//        formDataDefinitionTest.setCreatedById(BigInteger.valueOf(Long.parseLong(TEST_CREATED_BY_ID)));
//        formDataDefinitionTest.setCreatedOn(TEST_CREATED_ON);
//        formDataDefinitionTest.setCreatedByName(TEST_CREATED_BY_NAME);
//        formDataDefinitionTest.setUpdatedById(BigInteger.valueOf(Long.parseLong(TEST_UPDATED_BY_ID)));
//        formDataDefinitionTest.setUpdatedByName(TEST_UPDATED_BY_NAME);
//        formDataDefinitionTest.setUpdatedOn(TEST_UPDATED_ON);
//        Mockito.when(mockMongoTemplate.find(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(List.of(formDataDefinitionTest));
//        mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,TEST_RELATIONS, TEST_FILTER, CREATED_ON, DESCENDING);
//        verify(mockMongoTemplate,times(1)).find(any(),any(),any());
//    }

//    @Test
//    void getAllFormDataByFormIdEmptySortBySortOrderPaginationTest()
//    {
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        Map<String, Object> testFormData = new HashMap<>();
//        testFormData.put(NAME, NAME_VALUE);
//        testFormData.put(AGE,AGE_VALUE);
//        FormDataSchema formDataSchemaTest = new FormDataSchema(null, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
//        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_+formDataSchemaTest.getFormId())).thenReturn(true);
//        MongoCollection mongoCollection=mock(MongoCollection.class);
//        Mockito.when(mockMongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_+formDataSchemaTest.getFormId())).thenReturn(mongoCollection);
//        Mockito.when(mongoCollection.countDocuments()).thenReturn(Long.valueOf(2));
//        FormDataDefinition formDataDefinitionTest=new FormDataDefinition(BigInteger.valueOf(Long.parseLong(TEST_ID)),
//                TEST_VERSION,testFormData,testFormMetaData);
//        formDataDefinitionTest.setCreatedById(BigInteger.valueOf(Long.parseLong(TEST_CREATED_BY_ID)));
//        formDataDefinitionTest.setCreatedOn(TEST_CREATED_ON);
//        formDataDefinitionTest.setCreatedByName(TEST_CREATED_BY_NAME);
//        formDataDefinitionTest.setUpdatedById(BigInteger.valueOf(Long.parseLong(TEST_UPDATED_BY_ID)));
//        formDataDefinitionTest.setUpdatedByName(TEST_UPDATED_BY_NAME);
//        formDataDefinitionTest.setUpdatedOn(TEST_UPDATED_ON);
//        Mockito.when(mockMongoTemplate.find(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(List.of(formDataDefinitionTest));
//        mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,TEST_RELATIONS, TEST_FILTER, EMPTY_STRING, EMPTY_STRING, PageRequest.of(1,5));
//        verify(mockMongoTemplate,times(1)).find(any(),any(),any());
//    }

//    @Test
//    void getAllFormDataByFormIdSortBySortOrderPaginationTest()
//    {
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        Map<String, Object> testFormData = new HashMap<>();
//        testFormData.put(NAME,NAME_VALUE);
//        testFormData.put(AGE,AGE_VALUE);
//        FormDataSchema formDataSchemaTest = new FormDataSchema(null, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
//        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_+formDataSchemaTest.getFormId())).thenReturn(true);
//        MongoCollection mongoCollection=mock(MongoCollection.class);
//        Mockito.when(mockMongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_+formDataSchemaTest.getFormId())).thenReturn(mongoCollection);
//        Mockito.when(mongoCollection.countDocuments()).thenReturn(Long.valueOf(2));
//        FormDataDefinition formDataDefinitionTest=new FormDataDefinition(BigInteger.valueOf(Long.parseLong(TEST_ID)),
//                TEST_VERSION,testFormData,testFormMetaData);
//        formDataDefinitionTest.setCreatedById(BigInteger.valueOf(Long.parseLong(TEST_CREATED_BY_ID)));
//        formDataDefinitionTest.setCreatedOn(TEST_CREATED_ON);
//        formDataDefinitionTest.setCreatedByName(TEST_CREATED_BY_NAME);
//        formDataDefinitionTest.setUpdatedById(BigInteger.valueOf(Long.parseLong(TEST_UPDATED_BY_ID)));
//        formDataDefinitionTest.setUpdatedByName(TEST_UPDATED_BY_NAME);
//        formDataDefinitionTest.setUpdatedOn(TEST_UPDATED_ON);
//        Mockito.when(mockMongoTemplate.find(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(List.of(formDataDefinitionTest));
//        mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,TEST_RELATIONS, TEST_FILTER, CREATED_ON, DESCENDING, PageRequest.of(1,5));
//        verify(mockMongoTemplate,times(1)).find(any(),any(),any());
//    }

//    @Test
//    void getAllFormDataByFormIdAndQEmptySortBySortOrderTest()
//    {
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        Map<String, Object> testFormData = new HashMap<>();
//        testFormData.put(NAME, NAME_VALUE);
//        testFormData.put(AGE,AGE_VALUE);
//        FormDataSchema formDataSchemaTest = new FormDataSchema(null, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
//        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_+formDataSchemaTest.getFormId())).thenReturn(true);
//        FormDataDefinition formDataDefinitionTest=new FormDataDefinition(BigInteger.valueOf(Long.parseLong(TEST_ID)),
//                TEST_VERSION,testFormData,testFormMetaData);
//        formDataDefinitionTest.setCreatedById(BigInteger.valueOf(Long.parseLong(TEST_CREATED_BY_ID)));
//        formDataDefinitionTest.setCreatedOn(TEST_CREATED_ON);
//        formDataDefinitionTest.setCreatedByName(TEST_CREATED_BY_NAME);
//        formDataDefinitionTest.setUpdatedById(BigInteger.valueOf(Long.parseLong(TEST_UPDATED_BY_ID)));
//        formDataDefinitionTest.setUpdatedByName(TEST_UPDATED_BY_NAME);
//        formDataDefinitionTest.setUpdatedOn(TEST_UPDATED_ON);
//        Mockito.when(mockMongoTemplate.find(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(List.of(formDataDefinitionTest));
//        mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,TEST_RELATIONS, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING);
//        verify(mockMongoTemplate,times(1)).find(any(),any(),any());
//    }

//    @Test
//    void getAllFormDataByFormIdAndQSortBySortQTest()
//    {
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        Map<String, Object> testFormData = new HashMap<>();
//        testFormData.put(NAME, NAME_VALUE);
//        testFormData.put(AGE,AGE_VALUE);
//        FormDataSchema formDataSchemaTest = new FormDataSchema(null, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
//        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_+formDataSchemaTest.getFormId())).thenReturn(true);
//        FormDataDefinition formDataDefinitionTest=new FormDataDefinition(BigInteger.valueOf(Long.parseLong(TEST_ID)),
//                TEST_VERSION,testFormData,testFormMetaData);
//        formDataDefinitionTest.setCreatedById(BigInteger.valueOf(Long.parseLong(TEST_CREATED_BY_ID)));
//        formDataDefinitionTest.setCreatedOn(TEST_CREATED_ON);
//        formDataDefinitionTest.setCreatedByName(TEST_CREATED_BY_NAME);
//        formDataDefinitionTest.setUpdatedById(BigInteger.valueOf(Long.parseLong(TEST_UPDATED_BY_ID)));
//        formDataDefinitionTest.setUpdatedByName(TEST_UPDATED_BY_NAME);
//        formDataDefinitionTest.setUpdatedOn(TEST_UPDATED_ON);
//        Mockito.when(mockMongoTemplate.find(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(List.of(formDataDefinitionTest));
//        mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,TEST_RELATIONS, EMPTY_STRING, CREATED_ON, DESCENDING);
//        verify(mockMongoTemplate,times(1)).find(any(),any(),any());
//    }

//    @Test
//    void getAllFormDataByFormIdAndQEmptySortBySortOrderPaginationTest()
//    {
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        Map<String, Object> testFormData = new HashMap<>();
//        testFormData.put(NAME, NAME_VALUE);
//        testFormData.put(AGE,AGE_VALUE);
//        FormDataSchema formDataSchemaTest = new FormDataSchema(null, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
//        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_+formDataSchemaTest.getFormId())).thenReturn(true);
//        MongoCollection mongoCollection=mock(MongoCollection.class);
//        Mockito.when(mockMongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_+formDataSchemaTest.getFormId())).thenReturn(mongoCollection);
//        Mockito.when(mongoCollection.countDocuments()).thenReturn(Long.valueOf(2));
//        FormDataDefinition formDataDefinitionTest=new FormDataDefinition(BigInteger.valueOf(Long.parseLong(TEST_ID)),
//                TEST_VERSION,testFormData,testFormMetaData);
//        formDataDefinitionTest.setCreatedById(BigInteger.valueOf(Long.parseLong(TEST_CREATED_BY_ID)));
//        formDataDefinitionTest.setCreatedOn(TEST_CREATED_ON);
//        formDataDefinitionTest.setCreatedByName(TEST_CREATED_BY_NAME);
//        formDataDefinitionTest.setUpdatedById(BigInteger.valueOf(Long.parseLong(TEST_UPDATED_BY_ID)));
//        formDataDefinitionTest.setUpdatedByName(TEST_UPDATED_BY_NAME);
//        formDataDefinitionTest.setUpdatedOn(TEST_UPDATED_ON);
//        Mockito.when(mockMongoTemplate.find(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(List.of(formDataDefinitionTest));
//        mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,TEST_RELATIONS, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, PageRequest.of(0,5));
//        verify(mockMongoTemplate,times(1)).find(any(),any(),any());
//    }

//    @Test
//    void getAllFormDataByFormIdAndQSortBySortOrderPaginationTest()
//    {
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        Map<String, Object> testFormData = new HashMap<>();
//        testFormData.put(NAME, NAME_VALUE);
//        testFormData.put(AGE,AGE_VALUE);
//        FormDataSchema formDataSchemaTest = new FormDataSchema(null, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
//        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_+formDataSchemaTest.getFormId())).thenReturn(true);
//        MongoCollection mongoCollection=mock(MongoCollection.class);
//        Mockito.when(mockMongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_+formDataSchemaTest.getFormId())).thenReturn(mongoCollection);
//        Mockito.when(mongoCollection.countDocuments()).thenReturn(Long.valueOf(2));
//        FormDataDefinition formDataDefinitionTest=new FormDataDefinition(BigInteger.valueOf(Long.parseLong(TEST_ID)),
//                TEST_VERSION,testFormData,testFormMetaData);
//        formDataDefinitionTest.setCreatedById(BigInteger.valueOf(Long.parseLong(TEST_CREATED_BY_ID)));
//        formDataDefinitionTest.setCreatedOn(TEST_CREATED_ON);
//        formDataDefinitionTest.setCreatedByName(TEST_CREATED_BY_NAME);
//        formDataDefinitionTest.setUpdatedById(BigInteger.valueOf(Long.parseLong(TEST_UPDATED_BY_ID)));
//        formDataDefinitionTest.setUpdatedByName(TEST_UPDATED_BY_NAME);
//        formDataDefinitionTest.setUpdatedOn(TEST_UPDATED_ON);
//        Mockito.when(mockMongoTemplate.find(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(List.of(formDataDefinitionTest));
//        mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,TEST_RELATIONS, EMPTY_STRING, CREATED_ON, DESCENDING, PageRequest.of(0,5));
//        verify(mockMongoTemplate,times(1)).find(any(),any(),any());
//    }

//    @Test
//    void getAllFormDataByFormIdTest()
//    {
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        Map<String, Object> testFormData = new HashMap<>();
//        testFormData.put(NAME, NAME_VALUE);
//        testFormData.put(AGE,AGE_VALUE);
//        FormDataSchema formDataSchemaTest = new FormDataSchema(null, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
//        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_+formDataSchemaTest.getFormId())).thenReturn(true);
//        MongoCollection mongoCollection=mock(MongoCollection.class);
//        Mockito.when(mockMongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_+formDataSchemaTest.getFormId())).thenReturn(mongoCollection);
//        Mockito.when(mongoCollection.countDocuments()).thenReturn(Long.valueOf(2));
//        FormDataDefinition formDataDefinitionTest=new FormDataDefinition(BigInteger.valueOf(Long.parseLong(TEST_ID)),
//                TEST_VERSION,testFormData,testFormMetaData);
//        formDataDefinitionTest.setCreatedById(BigInteger.valueOf(Long.parseLong(TEST_CREATED_BY_ID)));
//        formDataDefinitionTest.setCreatedOn(TEST_CREATED_ON);
//        formDataDefinitionTest.setCreatedByName(TEST_CREATED_BY_NAME);
//        formDataDefinitionTest.setUpdatedById(BigInteger.valueOf(Long.parseLong(TEST_UPDATED_BY_ID)));
//        formDataDefinitionTest.setUpdatedByName(TEST_UPDATED_BY_NAME);
//        formDataDefinitionTest.setUpdatedOn(TEST_UPDATED_ON);
//        Mockito.when(mockMongoTemplate.find(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(List.of(formDataDefinitionTest));
//        mockFormDataServiceImpl.getAllFormDataByFormId(formDataSchemaTest.getFormId(),TEST_RELATIONS);
//        verify(mockMongoTemplate,times(1)).find(any(),any(),any());
//    }

//    @Test
//    void getAllFormDataByFormIdAndIdTest()
//    {
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        Map<String, Object> testFormData = new HashMap<>();
//        testFormData.put(NAME, NAME_VALUE);
//        testFormData.put(AGE,AGE_VALUE);
//        FormDataSchema formDataSchemaTest = new FormDataSchema(null, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
//        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_+TEST_FORM_ID)).thenReturn(true);
//        Map<String, Object> formDataMap = new HashMap<>();
//        formDataMap.put(UNDERSCORE_ID,Long.parseLong(UNDERSCORE_ID_VALUE));
//        formDataMap.put(FORM_ID,TEST_FORM_ID);
//        formDataMap.put(VERSION, String.valueOf(1));
//        formDataMap.put(FORM_META_DATA, formDataSchemaTest.getFormMetadata());
//        formDataMap.put(FORM_DATA, formDataSchemaTest.getFormData());
//        Date date=new Date();
//        formDataMap.put(CREATED_ON, date);
//        formDataMap.put(CREATED_BY_ID,CREATED_BY_USER_ID);
//        formDataMap.put(CREATED_BY_NAME, CREATED_BY_USER_NAME);
//        formDataMap.put(UPDATED_ON,date);
//        formDataMap.put(UPDATED_BY_ID,UPDATED_BY_USER_ID);
//        formDataMap.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
//        Document document = new Document(formDataMap);
//        MongoCollection<Document> mongoCollection=mock(MongoCollection.class);
//        FindIterable<Document> iterable = mock(FindIterable.class);
//        MongoCursor cursor = mock(MongoCursor.class);
//        Mockito.when(mockMongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_+TEST_FORM_ID)).thenReturn(mongoCollection);
//        Bson filter= Filters.eq(UNDERSCORE_ID,Long.valueOf(TEST_ID));
//        Mockito.when(mongoCollection.find(filter)).thenReturn(iterable);
//        Mockito.when(iterable.iterator()).thenReturn(cursor);
//        Mockito.when(cursor.hasNext()).thenReturn(true).thenReturn(false);
//        Mockito.when(cursor.next()).thenReturn(document);
//        Mockito.when(mockObjectMapper.convertValue(document.get(FORM_DATA),Map.class)).thenReturn(testFormData);
//        Mockito.when(mockObjectMapper.convertValue(document.get(FORM_META_DATA),Map.class)).thenReturn(testFormMetaData);
//        mockFormDataServiceImpl.getFormDataByFormIdAndId(TEST_FORM_ID,TEST_ID,TEST_RELATIONS);
//        verify(mongoCollection,times(1)).find(any(Bson.class));
//    }

    @Test
    void deleteAllFormDataByFormIdTest() {
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ + TEST_FORM_ID)).thenReturn(true);
        mockFormDataServiceImpl.deleteAllFormDataByFormId(TEST_FORM_ID);
        verify(mockMongoTemplate, times(1)).dropCollection(TP_RUNTIME_FORM_DATA_ + TEST_FORM_ID);
    }

    @Test
    void validateFormDataByFormIdNotMissingTest()
    {
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        LinkedHashMap<String, Object> testFormData = new LinkedHashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        FormDataSchema formDataSchemaTest=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,testFormData,testFormMetaData);
        Map<String, Object> testFormData2 = new HashMap<>();
        testFormData2.put(NAME, NAME_VALUE);
        testFormData2.put(AGE,AGE_VALUE);
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_ID, TEST_NAME, TEST_COMPONENTS,list,TEST_PROPERTIES, TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID, TEST_CREATED_ON, TEST_CREATED_BY_NAME, TEST_UPDATED_BY_ID, TEST_UPDATED_ON, TEST_UPDATED_BY_NAME);
        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
        fieldsMap.put(REQUIRED,false);
        fieldsMap.put(UNIQUE,false);
        schemaMap.put(NAME,fieldsMap);
        try (MockedStatic<ValidateFormUtils> mockValidateFormUtils = Mockito.mockStatic(ValidateFormUtils.class))
        {
            mockValidateFormUtils.when(() -> ValidateFormUtils.getSchema(formResponseSchemaTest.getComponents()))
                    .thenReturn(schemaMap);
        Mockito.when(mockValidationCheckService.allFieldsValidations(any(),any(),any(),any())).thenReturn(List.of(String.valueOf(10),EMPTY_STRING));
        mockFormDataServiceImpl.validateFormDataByFormId(formDataSchemaTest);
        verify(mockValidationCheckService,times(1)).allFieldsValidations(any(),any(),any(),any());
    }}

    @Test
    void validateFormDataByFormIdMissingTest()
    {
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        LinkedHashMap<String, Object> testFormData = new LinkedHashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        FormDataSchema formDataSchemaTest=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,testFormData,testFormMetaData);
        Map<String, Object> testFormData2 = new HashMap<>();
        testFormData2.put(NAME, NAME_VALUE);
        testFormData2.put(AGE,AGE_VALUE);
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_ID, TEST_NAME, TEST_COMPONENTS,list,TEST_PROPERTIES, TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID, TEST_CREATED_ON, TEST_CREATED_BY_NAME, TEST_UPDATED_BY_ID, TEST_UPDATED_ON, TEST_UPDATED_BY_NAME);
        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
        fieldsMap.put(REQUIRED,false);
        fieldsMap.put(UNIQUE,false);
        schemaMap.put(NAME,fieldsMap);
        try (MockedStatic<ValidateFormUtils> mockValidateFormUtils = Mockito.mockStatic(ValidateFormUtils.class)) {
            mockValidateFormUtils.when(() -> ValidateFormUtils.getSchema(formResponseSchemaTest.getComponents()))
                    .thenReturn(schemaMap);
            Mockito.when(mockValidationCheckService.allFieldsValidations(any(),any(),any(),any())).thenReturn(List.of(String.valueOf(10),EMPTY_STRING));
            mockFormDataServiceImpl.validateFormDataByFormId(formDataSchemaTest);
            verify(mockValidationCheckService, times(1)).allFieldsValidations(any(), any(),any(),any());
        }
        }
}
