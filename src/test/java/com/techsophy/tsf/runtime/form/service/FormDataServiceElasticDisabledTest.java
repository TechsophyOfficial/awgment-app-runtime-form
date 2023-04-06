package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoException;
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
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
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
    @Captor
    ArgumentCaptor<Query> queryArgumentCaptor;
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
    void saveFormDataValidationExceptionTest()
    {
        FormResponseSchema formResponseSchema=new FormResponseSchema();
        Mockito.when(mockFormService.getRuntimeFormById(anyString())).thenReturn(formResponseSchema);
        FormDataSchema formDataSchema=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,TEST_FORM_DATA,TEST_FORM_META_DATA);
        ValidationResult validationResult=new ValidationResult("name","name field cannot be empty");
        List<ValidationResult> validationResultList=new ArrayList<>();
        validationResultList.add(validationResult);
        Mockito.when(mockFormValidationServiceImpl.validateData(any(),any(),anyString())).thenReturn(validationResultList);
        Assertions.assertThrows(InvalidInputException.class,()->mockFormDataServiceImpl.saveFormData(formDataSchema,"formData.name:akhil"));
    }
    @Test
    void saveFormDataValidationExceptionTest1() throws JsonProcessingException {
        FormDataSchema formDataSchema=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,TEST_FORM_DATA,TEST_FORM_META_DATA);
        Mockito.when(mockObjectMapper.convertValue(any(),eq(FormDataDefinition.class))).thenReturn(new FormDataDefinition());
        Mockito.when(mockUserDetails.getUserDetails()).thenReturn(userList);
        Mockito.when(mockMongoTemplate.save(any(),anyString())).thenThrow(new MongoException(" E11000 Duplicate key index : officialEmail dup key"));
        Assertions.assertThrows(InvalidInputException.class,()->mockFormDataServiceImpl.saveFormData(formDataSchema,"formData.name:akhil"));
    }

    @Test
    void saveFormDataNewCollectionTest() throws IOException
    {
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema();
        Mockito.when(mockFormService.getRuntimeFormById(anyString())).thenReturn(formResponseSchemaTest);
        FormDataSchema formDataSchema=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,TEST_FORM_DATA,TEST_FORM_META_DATA);
        ValidationResult validationResult=new ValidationResult("name");
        List<ValidationResult> validationResultList=new ArrayList<>();
        validationResultList.add(validationResult);
        Mockito.when(mockFormValidationServiceImpl.validateData(any(),any(),anyString())).thenReturn(validationResultList);
        Mockito.when(mockObjectMapper.convertValue(any(),eq(FormDataDefinition.class))).thenReturn(new FormDataDefinition());
        Mockito.when(mockUserDetails.getUserDetails()).thenReturn(userList);
        mockFormDataServiceImpl.saveFormData(formDataSchema,"formData.name:akhil");
        Mockito.verify(mockMongoTemplate,times(1)).save(any(),anyString());
    }

    @Test
    void saveFormDataCreateNewRecordSameCollectionTest() throws IOException
    {
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema();
        Mockito.when(mockFormService.getRuntimeFormById(anyString())).thenReturn(formResponseSchemaTest);
        FormDataSchema formDataSchema=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,TEST_FORM_DATA,TEST_FORM_META_DATA);
        ValidationResult validationResult=new ValidationResult("name");
        List<ValidationResult> validationResultList=new ArrayList<>();
        validationResultList.add(validationResult);
        Mockito.when(mockFormValidationServiceImpl.validateData(any(),any(),anyString())).thenReturn(validationResultList);
        Mockito.when(mockObjectMapper.convertValue(any(),eq(FormDataDefinition.class))).thenReturn(new FormDataDefinition());
        Mockito.when(mockUserDetails.getUserDetails()).thenReturn(userList);
        mockFormDataServiceImpl.saveFormData(formDataSchema,"formData.name:akhil");
        Mockito.verify(mockMongoTemplate,times(1)).save(any(),anyString());
    }

    @Test
    void saveFormDataCreateNewRecordSameCollectionNotFoundExceptionTest() throws IOException
    {
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema();
        Mockito.when(mockFormService.getRuntimeFormById(anyString())).thenReturn(formResponseSchemaTest);
        FormDataSchema formDataSchema=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,TEST_FORM_DATA,TEST_FORM_META_DATA);
        ValidationResult validationResult=new ValidationResult("name");
        List<ValidationResult> validationResultList=new ArrayList<>();
        validationResultList.add(validationResult);
        Mockito.when(mockFormValidationServiceImpl.validateData(any(),any(),anyString())).thenReturn(validationResultList);
        FormDataDefinition formDataDefinition=new FormDataDefinition();
        formDataDefinition.setId("101");
        Mockito.when(mockObjectMapper.convertValue(any(),eq(FormDataDefinition.class))).thenReturn(formDataDefinition);
        Mockito.when(mockMongoTemplate.findOne(any(),any(),anyString())).thenReturn(null);
        Mockito.when(mockUserDetails.getUserDetails()).thenReturn(userList);
        Assertions.assertThrows(RuntimeException.class,()->mockFormDataServiceImpl.saveFormData(formDataSchema,"formData.name:akhil"));
    }

    @Test
    void saveFormDataUpdateRecordSameCollectionWithFilterTest() throws IOException
    {
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema();
        Mockito.when(mockFormService.getRuntimeFormById(anyString())).thenReturn(formResponseSchemaTest);
        FormDataSchema formDataSchema=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,TEST_FORM_DATA,TEST_FORM_META_DATA);
        ValidationResult validationResult=new ValidationResult("name");
        List<ValidationResult> validationResultList=new ArrayList<>();
        validationResultList.add(validationResult);
        Mockito.when(mockFormValidationServiceImpl.validateData(any(),any(),anyString())).thenReturn(validationResultList);
        FormDataDefinition formDataDefinition=new FormDataDefinition();
        formDataDefinition.setId("101");
        Mockito.when(mockObjectMapper.convertValue(any(),eq(FormDataDefinition.class))).thenReturn(formDataDefinition);
        Mockito.when(mockMongoTemplate.findOne(queryArgumentCaptor.capture(),any(),anyString())).thenReturn(formDataDefinition);
        Mockito.when(mockUserDetails.getUserDetails()).thenReturn(userList);
        mockFormDataServiceImpl.saveFormData(formDataSchema,"formData.name:akhil");
        Assertions.assertEquals("Query: { \"$and\" : [{ \"_id\" : \"1\"}, { \"$and\" : [{ \"formData.name\" : \"akhil\"}]}]}, Fields: {}, Sort: {}",queryArgumentCaptor.getValue().toString());
        Mockito.verify(mockMongoTemplate,times(1)).save(any(),anyString());
    }

    @Test
    void saveFormDataUpdateRecordSameCollectionWithoutFilterTest() throws IOException
    {
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema();
        Mockito.when(mockFormService.getRuntimeFormById(anyString())).thenReturn(formResponseSchemaTest);
        FormDataSchema formDataSchema=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,TEST_FORM_DATA,TEST_FORM_META_DATA);
        ValidationResult validationResult=new ValidationResult("name");
        List<ValidationResult> validationResultList=new ArrayList<>();
        validationResultList.add(validationResult);
        Mockito.when(mockFormValidationServiceImpl.validateData(any(),any(),anyString())).thenReturn(validationResultList);
        FormDataDefinition formDataDefinition=new FormDataDefinition();
        formDataDefinition.setId("101");
        Mockito.when(mockObjectMapper.convertValue(any(),eq(FormDataDefinition.class))).thenReturn(formDataDefinition);
        Mockito.when(mockMongoTemplate.findOne(any(),any(),anyString())).thenReturn(formDataDefinition);
        Mockito.when(mockUserDetails.getUserDetails()).thenReturn(userList);
        mockFormDataServiceImpl.saveFormData(formDataSchema,null);
        Mockito.verify(mockMongoTemplate,times(1)).save(any(),anyString());
    }
    @Test
    void updateFormDataWithFiltersTest()
    {
        FormDataSchema formDataSchema=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,new HashMap<>(),TEST_FORM_META_DATA);
        FormDataDefinition formDataDefinition=new FormDataDefinition();
        formDataDefinition.setFormData(new HashMap<>());
        formDataDefinition.setId("101");
        Mockito.when(mockMongoTemplate.findOne(queryArgumentCaptor.capture(),any(),anyString())).thenReturn(formDataDefinition);
        mockFormDataServiceImpl.updateFormData(formDataSchema,"formData.name:akhil");
        Assertions.assertEquals("Query: { \"$and\" : [{ \"_id\" : \"1\"}, { \"$and\" : [{ \"formData.name\" : \"akhil\"}]}]}, Fields: {}, Sort: {}",queryArgumentCaptor.getValue().toString());
        Mockito.verify(mockMongoTemplate,times(1)).save(any(),anyString());
    }

    @Test
    void updateFormDataWithOutFiltersTest()
    {
        FormDataSchema formDataSchema=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,new HashMap<>(),TEST_FORM_META_DATA);
        FormDataDefinition formDataDefinition=new FormDataDefinition();
        formDataDefinition.setFormData(new HashMap<>());
        formDataDefinition.setId("101");
        Mockito.when(mockMongoTemplate.findOne(any(),any(),anyString())).thenReturn(formDataDefinition);
        mockFormDataServiceImpl.updateFormData(formDataSchema,null);
        Mockito.verify(mockMongoTemplate,times(1)).save(any(),anyString());
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
        Mockito.when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
        FormDataDefinition formDataDefinitionTest=new FormDataDefinition();
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
        Mockito.when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
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
        Mockito.when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
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
        Mockito.when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
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
        FormDataSchema formDataSchema=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,TEST_FORM_DATA,TEST_FORM_META_DATA);
        LinkedHashMap<String, Object> formDataMap = new LinkedHashMap<>();
        formDataMap.put(UNDERSCORE_ID,Long.parseLong(UNDERSCORE_ID_VALUE));
        formDataMap.put(FORM_ID,TEST_FORM_ID);
        formDataMap.put(VERSION, String.valueOf(1));
        formDataMap.put(FORM_META_DATA, formDataSchema.getFormMetaData());
        formDataMap.put(FORM_DATA, formDataSchema.getFormData());
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
        Mockito.when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
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
        Mockito.when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
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
        Mockito.when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
        FormDataDefinition formDataDefinitionTest=new FormDataDefinition();
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
        Mockito.when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
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
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
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
        Mockito.when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
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
