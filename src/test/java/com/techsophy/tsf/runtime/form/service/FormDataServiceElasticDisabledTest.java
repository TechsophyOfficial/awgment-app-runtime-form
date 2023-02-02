package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.techsophy.idgenerator.IdGeneratorImpl;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.FormDataSchema;
import com.techsophy.tsf.runtime.form.dto.FormResponseSchema;
import com.techsophy.tsf.runtime.form.dto.ValidationResult;
import com.techsophy.tsf.runtime.form.entity.FormDataDefinition;
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
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_FORM_ID, TEST_NAME, TEST_COMPONENTS, list,TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID,
                TEST_CREATED_ON, TEST_UPDATED_BY_ID, TEST_UPDATED_ON);
        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
        ValidationResult validationResult=new ValidationResult("name");
        List<ValidationResult> validationResultList=new ArrayList<>();
        validationResultList.add(validationResult);
        Mockito.when(mockFormValidationServiceImpl.validateData(formResponseSchemaTest,formDataSchemaTest,TEST_FORM_ID)).thenReturn(validationResultList);
        Mockito.when(mockIdGeneratorImpl.nextId()).thenReturn(BigInteger.valueOf(Long.parseLong(TEST_FORM_ID)));
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA +formDataSchemaTest.getFormId())).thenReturn(true);
        Mockito.when(mockMongoTemplate.getCollection(anyString())).thenReturn(mockMongoCollection);
        Map<String, Object> formDataMap = new HashMap<>();
            formDataMap.put(UNDERSCORE_ID,Long.parseLong(UNDERSCORE_ID_VALUE));
            formDataMap.put(FORM_ID,TEST_FORM_ID);
            formDataMap.put(VERSION, String.valueOf(1));
            formDataMap.put(FORM_META_DATA, formDataSchemaTest.getFormMetadata());
            formDataMap.put(FORM_DATA, formDataSchemaTest.getFormData());
            formDataMap.put(CREATED_ON, Date.from(Instant.now()));
            formDataMap.put(CREATED_BY_ID,CREATED_BY_USER_ID);
            formDataMap.put(CREATED_BY_NAME, CREATED_BY_USER_NAME);
            formDataMap.put(UPDATED_ON,TEST_UPDATED_ON);
            formDataMap.put(UPDATED_BY_ID,UPDATED_BY_USER_ID);
            formDataMap.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        Document document = new Document(formDataMap);
        document.put(UNDERSCORE_ID,TEST_ID_VALUE);
        FindIterable<Document> iterable = mock(FindIterable.class);
        MongoCursor cursor = mock(MongoCursor.class);
        Mockito.when(mockMongoCollection.find()).thenReturn(iterable);
        Mockito.when(iterable.iterator()).thenReturn(cursor);
        Mockito.when(cursor.hasNext()).thenReturn(true).thenReturn(false);
        Mockito.when(cursor.next()).thenReturn(document);
        Mockito.when(mockMongoCollection.findOneAndReplace((Bson) any(),any(),any())).thenReturn(formDataMap);
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
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_FORM_ID, TEST_NAME, TEST_COMPONENTS, list,TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID,
                TEST_CREATED_ON, TEST_UPDATED_BY_ID, TEST_UPDATED_ON);
        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
        Mockito.when(mockIdGeneratorImpl.nextId()).thenReturn(BigInteger.valueOf(Long.parseLong(TEST_FORM_ID)));
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA +formDataSchemaTest.getFormId())).thenReturn(false);
        Map<String, Object> formDataMap = new HashMap<>();
        formDataMap.put(UNDERSCORE_ID,Long.parseLong(UNDERSCORE_ID_VALUE));
        formDataMap.put(FORM_ID,TEST_FORM_ID);
        formDataMap.put(VERSION, String.valueOf(1));
        formDataMap.put(FORM_META_DATA, formDataSchemaTest.getFormMetadata());
        formDataMap.put(FORM_DATA, formDataSchemaTest.getFormData());
        formDataMap.put(CREATED_ON, Date.from(Instant.now()));
        formDataMap.put(CREATED_BY_ID,CREATED_BY_USER_ID);
        formDataMap.put(CREATED_BY_NAME, CREATED_BY_USER_NAME);
        formDataMap.put(UPDATED_ON,TEST_UPDATED_ON);
        formDataMap.put(UPDATED_BY_ID,UPDATED_BY_USER_ID);
        formDataMap.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        Mockito.when(mockMongoTemplate.save(any(),anyString())).thenReturn(formDataMap);
        Assertions.assertNotNull(mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
    }

    @Test
    void saveFormDataEmptyDocumentIdTest() throws IOException
    {
        doReturn(userList).when(mockUserDetails).getUserDetails();
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
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
        formDataMap.put(FORM_META_DATA, formDataSchemaTest.getFormMetadata());
        formDataMap.put(FORM_DATA, formDataSchemaTest.getFormData());
        formDataMap.put(CREATED_ON, Date.from(Instant.now()));
        formDataMap.put(CREATED_BY_ID,CREATED_BY_USER_ID);
        formDataMap.put(CREATED_BY_NAME, CREATED_BY_USER_NAME);
        formDataMap.put(UPDATED_ON,TEST_UPDATED_ON);
        formDataMap.put(UPDATED_BY_ID,UPDATED_BY_USER_ID);
        formDataMap.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        Mockito.when(mockMongoTemplate.save(any(),anyString())).thenReturn(formDataMap);
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
        formDataMap.put(FORM_META_DATA, formDataSchemaTest.getFormMetadata());
        formDataMap.put(FORM_DATA, formDataSchemaTest.getFormData());
        formDataMap.put(CREATED_ON, Date.from(Instant.now()));
        formDataMap.put(CREATED_BY_ID,CREATED_BY_USER_ID);
        formDataMap.put(CREATED_BY_NAME, CREATED_BY_USER_NAME);
        formDataMap.put(UPDATED_ON,TEST_UPDATED_ON);
        formDataMap.put(UPDATED_BY_ID,UPDATED_BY_USER_ID);
        formDataMap.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        Document document = new Document(formDataMap);
        document.put(UNDERSCORE_ID,TEST_ID_VALUE);
        FindIterable<Document> iterable = mock(FindIterable.class);
        MongoCursor cursor = mock(MongoCursor.class);
        Mockito.when(mockMongoTemplate.getCollection(anyString())).thenReturn(mockMongoCollection);
        Bson filter = Filters.eq(UNDERSCORE_ID, Long.valueOf("123"));
        Mockito.when(mockMongoCollection.find(filter)).thenReturn(iterable);
        Mockito.when(iterable.iterator()).thenReturn(cursor);
        Mockito.when(cursor.hasNext()).thenReturn(true).thenReturn(false);
        Mockito.when(cursor.next()).thenReturn(document);
        Mockito.when(mockObjectMapper.convertValue(document.get(FORM_DATA),LinkedHashMap.class)).thenReturn(formDataMap);
        Mockito.when(mockObjectMapper.convertValue(document.get(FORM_META_DATA),Map.class)).thenReturn(testFormMetaData);
        Assertions.assertNotNull(mockFormDataServiceImpl.updateFormData(formDataSchemaTest));
    }

//    @Test
//    void saveFormDataCollectionExistsNewDocumentTest() throws JsonProcessingException
//    {
//        List<String> list1 = new ArrayList<>();
//        list1.add("10");
//        list1.add("2");
//        doReturn(userList).when(mockUserDetails).getUserDetails();
//        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        Map<String, Object> testFormData = new HashMap<>();
//        testFormData.put(NAME, NAME_VALUE);
//        testFormData.put(AGE,AGE_VALUE);
//        FormDataSchema formDataSchemaTest = new FormDataSchema(EMPTY_STRING, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
//        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_FORM_ID, TEST_NAME, TEST_COMPONENTS, list,TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID,
//                TEST_CREATED_ON, TEST_UPDATED_BY_ID, TEST_UPDATED_ON);
//        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
//        LinkedHashMap<String,Map<String,Object>> schemaMap=new LinkedHashMap<>();
//        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
//        fieldsMap.put(REQUIRED,false);
//        fieldsMap.put(UNIQUE,false);
//        schemaMap.put(NAME,fieldsMap);
//        Map<String, Object> givenData = new HashMap<>();
//        givenData.put(NAME, NAME_VALUE);
//        try (MockedStatic<ValidateFormUtils> mockValidateFormUtils = Mockito.mockStatic(ValidateFormUtils.class))
//        {
//            mockValidateFormUtils.when(() -> ValidateFormUtils.getSchema(formResponseSchemaTest.getComponents()))
//                    .thenReturn(schemaMap);
//            Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ +formDataSchemaTest.getFormId())).thenReturn(true);
//            Mockito.when(mockIdGeneratorImpl.nextId()).thenReturn(BigInteger.valueOf(Long.parseLong(TEST_FORM_ID)));
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
//            Mockito.when(mockFormDataAuditServiceImpl.saveFormDataAudit(formDataAuditSchemaTest)).thenReturn(new FormDataAuditResponse(TEST_ID_VALUE,TEST_VERSION));
//            Mockito.when(mockValidationCheckService.allFieldsValidations(any(),any(),any(),any())).thenReturn(list1);
//            mockFormDataServiceImpl.saveFormData(formDataSchemaTest);
//            verify(mockMongoTemplate,times(1)).save(any(),any());
//            verify(mockFormDataAuditServiceImpl,times(1)).saveFormDataAudit(formDataAuditSchemaTest);
//        }
//    }

//    @Test
//    void saveFormData() throws JsonProcessingException
//    {
//        ReflectionTestUtils.setField(mockFormDataServiceImpl,"elasticEnable",true);
//        List<String> list1 = new ArrayList<>();
//        list1.add("10");
//        list1.add("2");
//        doReturn(userList).when(mockUserDetails).getUserDetails();
//        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        Map<String, Object> testFormData = new HashMap<>();
//        testFormData.put(NAME, NAME_VALUE);
//        testFormData.put("id",null);
//        FormDataAuditResponse formDataAuditResponse = new FormDataAuditResponse("1",1);
//        Mockito.when(mockFormDataAuditServiceImpl.saveFormDataAudit(any())).thenReturn(formDataAuditResponse);
//        FormDataSchema formDataSchemaTest = new FormDataSchema(null, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
//        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_FORM_ID, TEST_NAME, TEST_COMPONENTS, list,TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE,
//                TEST_CREATED_BY_ID, TEST_CREATED_ON,
//                TEST_UPDATED_BY_ID, TEST_UPDATED_ON);        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
//        LinkedHashMap<String,Map<String,Object>> schemaMap=new LinkedHashMap<>();
//        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
//        fieldsMap.put(REQUIRED,false);
//        fieldsMap.put(UNIQUE,false);
//        schemaMap.put(NAME,fieldsMap);
//        Map<String, Object> givenData = new HashMap<>();
//        givenData.put(NAME, NAME_VALUE);
//        try (MockedStatic<ValidateFormUtils> mockValidateFormUtils = Mockito.mockStatic(ValidateFormUtils.class))
//        {
//            mockValidateFormUtils.when(() -> ValidateFormUtils.getSchema(formResponseSchemaTest.getComponents()))
//                    .thenReturn(schemaMap);
//            Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ +formDataSchemaTest.getFormId())).thenReturn(true);
//            Mockito.when(mockIdGeneratorImpl.nextId()).thenReturn(BigInteger.valueOf(Long.parseLong(TEST_FORM_ID)));
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
//            formDataMap.put(DATA,UPDATED_BY_USER_NAME);
//            LinkedHashMap data1 = new LinkedHashMap<>();
//            data1.put("abc","abc");
//            data1.put(VERSION,1);
//            data1.put(CREATED_BY_ID,CREATED_BY_USER_ID);
//            data1.put(CREATED_ON,TEST_CREATED_ON);
//            data1.put(CREATED_BY_NAME,CREATED_BY_USER_NAME);
//            Document document = new Document(formDataMap);
//            Mockito.when(mockMongoTemplate.save(any(),any())).thenReturn(document);
//            Date currentDate = new Date();
//            Document document1 = new Document("version",1);
//            document1.append("formData",formDataMap);
//            document1.append("formMetaData",formDataMap);
//            document1.append("_id","1");
//            document1.append(CREATED_ON,currentDate);
//            document1.append(CREATED_BY_ID,"1");
//            document1.append(CREATED_BY_NAME,STRING);
//            FormDataAuditSchema formDataAuditSchemaTest = new FormDataAuditSchema(TEST_ID_VALUE, TEST_FORM_DATA_ID,TEST_FORM_ID,TEST_VERSION, testFormData,testFormMetaData);
//            Mockito.when(mockFormDataAuditServiceImpl.saveFormDataAudit(formDataAuditSchemaTest)).thenReturn(new FormDataAuditResponse(TEST_ID_VALUE,TEST_VERSION));
//            Mockito.when(mockValidationCheckService.allFieldsValidations(any(),any(),any(),any())).thenReturn(list1);
//            Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),any(),any())).thenReturn(STRING).thenReturn(STRING).thenReturn("1").thenReturn(String.valueOf(new Exception())).thenReturn(String.valueOf(new Exception()));
//            Assertions.assertNotNull(mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
//        }
//    }

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
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,TEST_RELATIONS,FILTER,EMPTY_STRING, EMPTY_STRING));
    }

    @Test
    void getAllFormDataByFormIdAggregationSortTest()
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
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,TEST_RELATIONS,FILTER,TEST_SORT_BY, TEST_SORT_ORDER));
    }

    @Test
    void getAllFormDataByFormIdPaginationEmptySortTest()
    {
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
    void getAllFormDataByFormIdPaginationSortTest()
    {
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
    void getAllFormDataByFormIdFilterAndRelationsTest()
    {
        Mockito.when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
        List<FormDataDefinition> formDataDefinitionsList=new ArrayList<>();
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        FormDataDefinition formDataDefinition=new FormDataDefinition();
        formDataDefinition.setId(BigInteger.valueOf(Long.parseLong(TEST_ID_VALUE)));
        formDataDefinition.setFormData(testFormData);
        formDataDefinition.setFormMetadata(testFormMetaData);
        formDataDefinition.setVersion(TEST_VERSION);
        formDataDefinition.setCreatedById(BigInteger.valueOf(Long.parseLong(TEST_CREATED_BY_ID)));
        formDataDefinition.setCreatedOn(Instant.now());
        formDataDefinition.setUpdatedById(BigInteger.valueOf(Long.parseLong(TEST_UPDATED_BY_ID)));
        formDataDefinition.setUpdatedOn(Instant.now());
        formDataDefinitionsList.add(formDataDefinition);
        Mockito.when(mockMongoTemplate.find(any(),eq(FormDataDefinition.class),anyString())).thenReturn(formDataDefinitionsList);
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,EMPTY_STRING,FILTER, EMPTY_STRING, EMPTY_STRING));
    }

    @Test
    void getAllFormDataByFormIdFilterAndRelationsSortBySortOrderTest()
    {
        Mockito.when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
        List<FormDataDefinition> formDataDefinitionsList=new ArrayList<>();
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        FormDataDefinition formDataDefinition=new FormDataDefinition();
        formDataDefinition.setId(BigInteger.valueOf(Long.parseLong(TEST_ID_VALUE)));
        formDataDefinition.setFormData(testFormData);
        formDataDefinition.setFormMetadata(testFormMetaData);
        formDataDefinition.setVersion(TEST_VERSION);
        formDataDefinition.setCreatedById(BigInteger.valueOf(Long.parseLong(TEST_CREATED_BY_ID)));
        formDataDefinition.setCreatedOn(Instant.now());
        formDataDefinition.setUpdatedById(BigInteger.valueOf(Long.parseLong(TEST_UPDATED_BY_ID)));
        formDataDefinition.setUpdatedOn(Instant.now());
        formDataDefinitionsList.add(formDataDefinition);
//        Mockito.when(mockMongoTemplate.find(any(),eq(FormDataDefinition.class),anyString())).thenReturn(formDataDefinitionsList);
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,EMPTY_STRING,FILTER,TEST_SORT_BY,TEST_SORT_ORDER));
    }

    @Test
    void getAllFormDataByFormIdEmptySortPaginationTest()
    {
        List<FormDataDefinition> formDataDefinitionsList=new ArrayList<>();
        FormDataDefinition formDataDefinition=new FormDataDefinition();
        formDataDefinition.setId(BigInteger.valueOf(Long.parseLong(TEST_ID_VALUE)));
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        formDataDefinition.setFormData(testFormData);
        formDataDefinition.setFormMetadata(testFormMetaData);
        formDataDefinition.setVersion(TEST_VERSION);
        formDataDefinition.setCreatedById(BigInteger.valueOf(Long.parseLong(TEST_CREATED_BY_ID)));
        formDataDefinition.setCreatedOn(Instant.now());
        formDataDefinition.setUpdatedById(BigInteger.valueOf(Long.parseLong(TEST_UPDATED_BY_ID)));
        formDataDefinition.setUpdatedOn(Instant.now());
        formDataDefinitionsList.add(formDataDefinition);
        Mockito.when(mockMongoTemplate.find(any(),eq(FormDataDefinition.class),anyString())).thenReturn(formDataDefinitionsList);
        Mockito.when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,EMPTY_STRING,FILTER, EMPTY_STRING,EMPTY_STRING, PageRequest.of(1,5)));
    }

    @Test
    void getAllFormDataByFormIdSortPaginationTest()
    {
        List<FormDataDefinition> formDataDefinitionsList=new ArrayList<>();
        FormDataDefinition formDataDefinition=new FormDataDefinition();
        formDataDefinition.setId(BigInteger.valueOf(Long.parseLong(TEST_ID_VALUE)));
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        formDataDefinition.setFormData(testFormData);
        formDataDefinition.setFormMetadata(testFormMetaData);
        formDataDefinition.setVersion(TEST_VERSION);
        formDataDefinition.setCreatedById(BigInteger.valueOf(Long.parseLong(TEST_CREATED_BY_ID)));
        formDataDefinition.setCreatedOn(Instant.now());
        formDataDefinition.setUpdatedById(BigInteger.valueOf(Long.parseLong(TEST_UPDATED_BY_ID)));
        formDataDefinition.setUpdatedOn(Instant.now());
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
        formDataDefinition.setId(BigInteger.valueOf(Long.parseLong(TEST_ID_VALUE)));
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        formDataDefinition.setFormData(testFormData);
        formDataDefinition.setFormMetadata(testFormMetaData);
        formDataDefinition.setVersion(TEST_VERSION);
        formDataDefinition.setCreatedById(BigInteger.valueOf(Long.parseLong(TEST_CREATED_BY_ID)));
        formDataDefinition.setCreatedOn(Instant.now());
        formDataDefinition.setUpdatedById(BigInteger.valueOf(Long.parseLong(TEST_UPDATED_BY_ID)));
        formDataDefinition.setUpdatedOn(Instant.now());
        formDataDefinitionsList.add(formDataDefinition);
        Mockito.when(mockMongoTemplate.find(any(),eq(FormDataDefinition.class),anyString())).thenReturn(formDataDefinitionsList);
        List<Map> aggregateList=new ArrayList<>();
        Map<String,Object> map=new HashMap<>();
        map.put(UNDERSCORE_ID,TEST_ID_VALUE);
        aggregateList.add(map);
        Document document=new Document();
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,EMPTY_STRING,Q,EMPTY_STRING,EMPTY_STRING));
    }

    @Test
    void getAllFormDataByFormIdSortWithoutRelationsTest()
    {
        Mockito.when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
        List<FormDataDefinition> formDataDefinitionsList=new ArrayList<>();
        FormDataDefinition formDataDefinition=new FormDataDefinition();
        formDataDefinition.setId(BigInteger.valueOf(Long.parseLong(TEST_ID_VALUE)));
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        formDataDefinition.setFormData(testFormData);
        formDataDefinition.setFormMetadata(testFormMetaData);
        formDataDefinition.setVersion(TEST_VERSION);
        formDataDefinition.setCreatedById(BigInteger.valueOf(Long.parseLong(TEST_CREATED_BY_ID)));
        formDataDefinition.setCreatedOn(Instant.now());
        formDataDefinition.setUpdatedById(BigInteger.valueOf(Long.parseLong(TEST_UPDATED_BY_ID)));
        formDataDefinition.setUpdatedOn(Instant.now());
        formDataDefinitionsList.add(formDataDefinition);
//        Mockito.when(mockMongoTemplate.find(any(),eq(FormDataDefinition.class),anyString())).thenReturn(formDataDefinitionsList);
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
        FormDataDefinition formDataDefinitionTest=new FormDataDefinition(BigInteger.valueOf(Long.parseLong(TEST_ID)),
                TEST_VERSION,testFormData,testFormMetaData);
        formDataDefinitionTest.setCreatedById(BigInteger.valueOf(Long.parseLong(TEST_CREATED_BY_ID)));
        formDataDefinitionTest.setCreatedOn(TEST_CREATED_ON);
        formDataDefinitionTest.setUpdatedById(BigInteger.valueOf(Long.parseLong(TEST_UPDATED_BY_ID)));
        formDataDefinitionTest.setUpdatedOn(TEST_UPDATED_ON);
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
        formDataDefinition.setId(BigInteger.valueOf(Long.parseLong(TEST_ID_VALUE)));
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        formDataDefinition.setFormData(testFormData);
        formDataDefinition.setFormMetadata(testFormMetaData);
        formDataDefinition.setVersion(TEST_VERSION);
        formDataDefinition.setCreatedById(BigInteger.valueOf(Long.parseLong(TEST_CREATED_BY_ID)));
        formDataDefinition.setCreatedOn(Instant.now());
        formDataDefinition.setUpdatedById(BigInteger.valueOf(Long.parseLong(TEST_UPDATED_BY_ID)));
        formDataDefinition.setUpdatedOn(Instant.now());
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
        formDataDefinition.setId(BigInteger.valueOf(Long.parseLong(TEST_ID_VALUE)));
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        formDataDefinition.setFormData(testFormData);
        formDataDefinition.setFormMetadata(testFormMetaData);
        formDataDefinition.setVersion(TEST_VERSION);
        formDataDefinition.setCreatedById(BigInteger.valueOf(Long.parseLong(TEST_CREATED_BY_ID)));
        formDataDefinition.setCreatedOn(Instant.now());
        formDataDefinition.setUpdatedById(BigInteger.valueOf(Long.parseLong(TEST_UPDATED_BY_ID)));
        formDataDefinition.setUpdatedOn(Instant.now());
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
        formDataDefinition.setId(BigInteger.valueOf(Long.parseLong(TEST_ID_VALUE)));
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        formDataDefinition.setFormData(testFormData);
        formDataDefinition.setFormMetadata(testFormMetaData);
        formDataDefinition.setVersion(TEST_VERSION);
        formDataDefinition.setCreatedById(BigInteger.valueOf(Long.parseLong(TEST_CREATED_BY_ID)));
        formDataDefinition.setCreatedOn(Instant.now());
        formDataDefinition.setUpdatedById(BigInteger.valueOf(Long.parseLong(TEST_UPDATED_BY_ID)));
        formDataDefinition.setUpdatedOn(Instant.now());
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
        formDataMap.put(FORM_META_DATA, formDataSchemaTest.getFormMetadata());
        formDataMap.put(FORM_DATA, formDataSchemaTest.getFormData());
        formDataMap.put(CREATED_ON, Date.from(Instant.now()));
        formDataMap.put(CREATED_BY_ID,CREATED_BY_USER_ID);
        formDataMap.put(CREATED_BY_NAME, CREATED_BY_USER_NAME);
        formDataMap.put(UPDATED_ON,Date.from(Instant.now()));
        formDataMap.put(UPDATED_BY_ID,UPDATED_BY_USER_ID);
        formDataMap.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        Document document = new Document(formDataMap);
        document.put(UNDERSCORE_ID,TEST_ID_VALUE);
        FindIterable<Document> iterable = mock(FindIterable.class);
        MongoCursor cursor = mock(MongoCursor.class);
        Mockito.when(mockMongoTemplate.getCollection(any())).thenReturn(mongoCollectionDocument);
        Mockito.when(mongoCollectionDocument.find(any(Bson.class))).thenReturn(mockDocuments);
        Mockito.when(mockDocuments.iterator()).thenReturn(mongoCursor);
        Mockito.when(mongoCursor.hasNext()).thenReturn(true).thenReturn(false);
        Mockito.when(mongoCursor.next()).thenReturn(document);
        List<FormDataDefinition> formDataDefinitionsList=new ArrayList<>();
        FormDataDefinition formDataDefinition=new FormDataDefinition();
        formDataDefinition.setId(BigInteger.valueOf(Long.parseLong(TEST_ID_VALUE)));
        formDataDefinition.setFormData(testFormData);
        formDataDefinition.setFormMetadata(testFormMetaData);
        formDataDefinition.setVersion(TEST_VERSION);
        formDataDefinition.setCreatedById(BigInteger.valueOf(Long.parseLong(TEST_CREATED_BY_ID)));
        formDataDefinition.setCreatedOn(Instant.now());
        formDataDefinition.setUpdatedById(BigInteger.valueOf(Long.parseLong(TEST_UPDATED_BY_ID)));
        formDataDefinition.setUpdatedOn(Instant.now());
        formDataDefinitionsList.add(formDataDefinition);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA +formDataSchemaTest.getFormId())).thenReturn(true);
        Mockito.when(mockObjectMapper.convertValue(document.get(FORM_DATA),Map.class)).thenReturn(formDataMap);
        Mockito.when(mockObjectMapper.convertValue(document.get(FORM_META_DATA),Map.class)).thenReturn(testFormMetaData);
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
        formDataDefinition.setId(BigInteger.valueOf(Long.parseLong(TEST_ID_VALUE)));
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        formDataDefinition.setFormData(testFormData);
        formDataDefinition.setFormMetadata(testFormMetaData);
        formDataDefinition.setVersion(TEST_VERSION);
        formDataDefinition.setCreatedById(BigInteger.valueOf(Long.parseLong(TEST_CREATED_BY_ID)));
        formDataDefinition.setCreatedOn(Instant.now());
        formDataDefinition.setUpdatedById(BigInteger.valueOf(Long.parseLong(TEST_UPDATED_BY_ID)));
        formDataDefinition.setUpdatedOn(Instant.now());
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
        FormDataDefinition formDataDefinitionTest=new FormDataDefinition(BigInteger.valueOf(Long.parseLong(TEST_ID)),
                TEST_VERSION,testFormData,testFormMetaData);
        formDataDefinitionTest.setCreatedById(BigInteger.valueOf(Long.parseLong(TEST_CREATED_BY_ID)));
        formDataDefinitionTest.setCreatedOn(TEST_CREATED_ON);
        formDataDefinitionTest.setUpdatedById(BigInteger.valueOf(Long.parseLong(TEST_UPDATED_BY_ID)));
        formDataDefinitionTest.setUpdatedOn(TEST_UPDATED_ON);
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
        ReflectionTestUtils.setField(mockFormDataServiceImpl,"elasticEnable", true);
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
    void updateFormData() throws Exception
    {
        ReflectionTestUtils.setField(mockFormDataServiceImpl,"elasticEnable", true);
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        Date currentDate = new Date();
        Document document = new Document("version",1);
        document.append("formData",STRING);
        document.append("formMetaData",STRING);
        document.append("_id","1");
        document.append(CREATED_ON,currentDate);
        document.append(CREATED_BY_ID,"1");
        document.append(CREATED_BY_NAME,STRING);
        doReturn(userList).when(mockUserDetails).getUserDetails();
        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),any(),any())).thenReturn(STRING);
        Mockito.when(mockObjectMapper.convertValue(STRING,Map.class)).thenReturn(testFormData);
        Mockito.when(mockMongoTemplate.getCollection(any())).thenReturn(mongoCollectionDocument);
        Mockito.when(mongoCollectionDocument.find(any(Bson.class))).thenReturn(mockDocuments);
        Mockito.when(mockDocuments.iterator()).thenReturn(mongoCursor);
        Mockito.when(mongoCursor.hasNext()).thenReturn(true).thenReturn(false);
        Mockito.when(mongoCursor.next()).thenReturn(document);
        FormDataSchema formDataSchemaTest = new FormDataSchema("1", TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA +formDataSchemaTest.getFormId())).thenReturn(true);
        Assertions.assertNotNull(mockFormDataServiceImpl.updateFormData(formDataSchemaTest));
    }

    @Test
    void deleteAllFormDataByFormIdTest()
    {
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA + TEST_FORM_ID)).thenReturn(true);
        mockFormDataServiceImpl.deleteAllFormDataByFormId(TEST_FORM_ID);
        verify(mockMongoTemplate, times(1)).dropCollection(TP_RUNTIME_FORM_DATA + TEST_FORM_ID);
    }

//    @Test
//    void validateFormDataByFormIdNotMissingTest()
//    {
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        List list1 = new ArrayList<>();
//        list1.add(STRING);
//        LinkedHashMap linkedHashMap = new LinkedHashMap<>();
//        linkedHashMap.put(STRING,list1);
//        ArrayList list = new ArrayList<>();
//        list.add(linkedHashMap);
//        Map<String, Object> testFormData = new HashMap<>();
//        testFormData.put(NAME, list);
//        testFormData.put(AGE,AGE_VALUE);
//        FormDataSchema formDataSchemaTest=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,testFormData,testFormMetaData);
//        Map<String, Object> testFormData2 = new HashMap<>();
//        testFormData2.put(NAME, NAME_VALUE);
//        testFormData2.put(AGE,AGE_VALUE);
//        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_ID,
//                TEST_NAME, TEST_COMPONENTS,list,
//                TEST_PROPERTIES, TEST_TYPE_FORM,
//                TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID,
//                TEST_CREATED_ON,
//                TEST_UPDATED_BY_ID, TEST_UPDATED_ON);
//        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
//        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
//        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
//        fieldsMap.put(REQUIRED,false);
//        fieldsMap.put(UNIQUE,false);
//        schemaMap.put(NAME,fieldsMap);
//        try (MockedStatic<ValidateFormUtils> mockValidateFormUtils = Mockito.mockStatic(ValidateFormUtils.class))
//        {
//            mockValidateFormUtils.when(() -> ValidateFormUtils.getSchema(formResponseSchemaTest.getComponents()))
//                    .thenReturn(schemaMap);
//        Mockito.when(mockValidationCheckService.allFieldsValidations(any(),any(),any(),any())).thenReturn(List.of(String.valueOf(10),EMPTY_STRING));
//        mockFormDataServiceImpl.validateFormDataByFormId(formDataSchemaTest);
//        verify(mockValidationCheckService,times(1)).allFieldsValidations(any(),any(),any(),any());
//    }}
//    @Test
//    void validateFormDataByFormIdException()
//    {
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        Map<String, Object> testFormData = new HashMap<>();
//        testFormData.put(NAME, NAME_VALUE);
//        testFormData.put(AGE,AGE_VALUE);
//        FormDataSchema formDataSchemaTest=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,testFormData,testFormMetaData);
//        Map<String, Object> testFormData2 = new HashMap<>();
//        testFormData2.put(NAME, NAME_VALUE);
//        testFormData2.put(AGE,AGE_VALUE);
//        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_ID, TEST_NAME, TEST_COMPONENTS,list,TEST_PROPERTIES, TEST_TYPE_FORM,
//                TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID,
//                TEST_CREATED_ON, TEST_UPDATED_BY_ID, TEST_UPDATED_ON);
//        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
//        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
//        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
//        fieldsMap.put(REQUIRED,false);
//        fieldsMap.put(UNIQUE,false);
//        schemaMap.put(NAME,fieldsMap);
//        try (MockedStatic<ValidateFormUtils> mockValidateFormUtils = Mockito.mockStatic(ValidateFormUtils.class)) {
//            mockValidateFormUtils.when(() -> ValidateFormUtils.getSchema(formResponseSchemaTest.getComponents()))
//                    .thenReturn(schemaMap);
//            for(int i=0;i<=9;i++)
//            {
//                Mockito.when(mockValidationCheckService.allFieldsValidations(any(), any(), any(), any())).thenReturn(List.of(String.valueOf(i), EMPTY_STRING));
//                Assertions.assertThrows(InvalidInputException.class,()->mockFormDataServiceImpl.validateFormDataByFormId(formDataSchemaTest));
//            }
//        }
//    }
//    @Test
//    void validateFormDataByFormIdMissingTest()
//    {
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        Map<String, Object> testFormData = new HashMap<>();
//        testFormData.put(NAME, NAME_VALUE);
//        testFormData.put(AGE,AGE_VALUE);
//        FormDataSchema formDataSchemaTest=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,testFormData,testFormMetaData);
//        Map<String, Object> testFormData2 = new HashMap<>();
//        testFormData2.put(NAME, NAME_VALUE);
//        testFormData2.put(AGE,AGE_VALUE);
//        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_ID, TEST_NAME, TEST_COMPONENTS,list,TEST_PROPERTIES, TEST_TYPE_FORM,
//                TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID,
//                TEST_CREATED_ON, TEST_UPDATED_BY_ID, TEST_UPDATED_ON);
//        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
//        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
//        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
//        fieldsMap.put(REQUIRED,false);
//        fieldsMap.put(UNIQUE,false);
//        schemaMap.put(NAME,fieldsMap);
//        try (MockedStatic<ValidateFormUtils> mockValidateFormUtils = Mockito.mockStatic(ValidateFormUtils.class)) {
//            mockValidateFormUtils.when(() -> ValidateFormUtils.getSchema(formResponseSchemaTest.getComponents()))
//                    .thenReturn(schemaMap);
//            Mockito.when(mockValidationCheckService.allFieldsValidations(any(),any(),any(),any())).thenReturn(List.of(String.valueOf(10),EMPTY_STRING));
//            mockFormDataServiceImpl.validateFormDataByFormId(formDataSchemaTest);
//            verify(mockValidationCheckService, times(1)).allFieldsValidations(any(), any(),any(),any());
//        }
//    }



    @Test
    void aggregateByFormIdFilterGroupByTest()
    {
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
