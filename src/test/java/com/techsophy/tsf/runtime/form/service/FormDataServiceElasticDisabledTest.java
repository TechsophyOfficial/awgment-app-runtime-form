package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.techsophy.idgenerator.IdGeneratorImpl;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.*;
import com.techsophy.tsf.runtime.form.entity.FormDataDefinition;
import com.techsophy.tsf.runtime.form.exception.FormIdNotFoundException;
import com.techsophy.tsf.runtime.form.exception.InvalidInputException;
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
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigInteger;
import java.time.Instant;
import java.util.*;

import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.CONTENT;

import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.DATA;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.DEFAULT_PAGE_LIMIT;

import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.FORM_DATA;

import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static shadow.org.assertj.core.api.Assertions.assertThat;

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
    ValidationCheckServiceImpl mockValidationCheckService;
    @Mock
    ObjectMapper mockObjectMapper;
    @Mock
    MongoCursor mongoCursor;
    @Mock
    AggregationResults aggregationResults;
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
//        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_FORM_ID, TEST_NAME, TEST_COMPONENTS, list,TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID, TEST_CREATED_ON, TEST_CREATED_BY_NAME, TEST_UPDATED_BY_ID, TEST_UPDATED_ON, TEST_UPDATED_BY_NAME);        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
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
//            Mockito.when(mockMongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_ +formDataSchemaTest.getFormId())).thenReturn(mockMongoCollection);
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
//            Mockito.when(mockValidationCheckService.allFieldsValidations(any(),any(),any(),any())).thenReturn(list1);
//            mockFormDataServiceImpl.saveFormData(formDataSchemaTest);
//            verify(mockMongoTemplate,times(1)).save(any(),any());
//            verify(mockFormDataAuditServiceImpl,times(1)).saveFormData(formDataAuditSchemaTest);
//        }
//    }
//    @Test
//    void saveFormDataWithException() throws Exception {
//        List<String> list1 = new ArrayList<>();
//        list1.add("10");
//        list1.add("2");
//        doReturn(userList).when(mockUserDetails).getUserDetails();
//        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        Map<String, Object> testFormData = new HashMap<>();
//        Map<String, Object> testFormData1 = new HashMap<>();
//        testFormData.put(NAME, NAME_VALUE);
//        testFormData.put(AGE, AGE_VALUE);
//        testFormData1.put(AGE, AGE_VALUE);
//        testFormData.put(ID, AGE_VALUE);
//        testFormData1.put(ID, null);
//        FormDataSchema formDataSchemaTest = new FormDataSchema(EMPTY_STRING, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
//        FormDataSchema formDataSchemaTest1 = new FormDataSchema(null, TEST_FORM_ID, TEST_VERSION, testFormData1, testFormMetaData);
//       // FormDataSchema formDataSchemaTest2 = new FormDataSchema(null, TEST_FORM_ID, TEST_VERSION, testFormData1, testFormMetaData);
//        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_FORM_ID, TEST_NAME, TEST_COMPONENTS, list, TEST_PROPERTIES, TEST_TYPE_FORM, TEST_VERSION, IS_DEFAULT_VALUE, TEST_CREATED_BY_ID, TEST_CREATED_ON, TEST_CREATED_BY_NAME, TEST_UPDATED_BY_ID, TEST_UPDATED_ON, TEST_UPDATED_BY_NAME);
//        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
//        LinkedHashMap<String, Map<String, Object>> schemaMap = new LinkedHashMap<>();
//        LinkedHashMap<String, Object> fieldsMap = new LinkedHashMap<>();
//        fieldsMap.put(REQUIRED, false);
//        fieldsMap.put(UNIQUE, false);
//        schemaMap.put(NAME, fieldsMap);
//        Map<String, Object> givenData = new HashMap<>();
//        givenData.put(NAME, NAME_VALUE);
//        try (MockedStatic<ValidateFormUtils> mockValidateFormUtils = Mockito.mockStatic(ValidateFormUtils.class)) {
//            mockValidateFormUtils.when(() -> ValidateFormUtils.getSchema(formResponseSchemaTest.getComponents()))
//                    .thenReturn(schemaMap);
//            Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ + formDataSchemaTest.getFormId())).thenReturn(false).thenReturn(false).thenReturn(true);
//            Mockito.when(mockIdGeneratorImpl.nextId()).thenReturn(BigInteger.valueOf(Long.parseLong(TEST_FORM_ID)));
//            Mockito.when(mockMongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_ + formDataSchemaTest.getFormId())).thenReturn(mockMongoCollection);
//            Map<String, Object> formDataMap = new HashMap<>();
//            formDataMap.put(UNDERSCORE_ID, Long.parseLong(UNDERSCORE_ID_VALUE));
//            formDataMap.put(FORM_ID, TEST_FORM_ID);
//            formDataMap.put(VERSION, String.valueOf(1));
//            formDataMap.put(FORM_META_DATA, formDataSchemaTest.getFormMetadata());
//            formDataMap.put(FORM_DATA, formDataSchemaTest.getFormData());
//            formDataMap.put(CREATED_ON, Instant.now());
//            formDataMap.put(CREATED_BY_ID, CREATED_BY_USER_ID);
//            formDataMap.put(CREATED_BY_NAME, CREATED_BY_USER_NAME);
//            formDataMap.put(UPDATED_ON, TEST_UPDATED_ON);
//            formDataMap.put(UPDATED_BY_ID, UPDATED_BY_USER_ID);
//            formDataMap.put(UPDATED_BY_NAME, UPDATED_BY_USER_NAME);
//            Document document = new Document(formDataMap);
//            Mockito.when(mockMongoTemplate.save(any(), any())).thenReturn(document);
//            FormDataAuditSchema formDataAuditSchemaTest = new FormDataAuditSchema(TEST_ID_VALUE, TEST_FORM_DATA_ID, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
//            Mockito.when(mockFormDataAuditServiceImpl.saveFormData(formDataAuditSchemaTest)).thenReturn(new FormDataAuditResponse(TEST_ID_VALUE, TEST_VERSION));
//            Mockito.when(mockValidationCheckService.allFieldsValidations(any(), any(), any(), any())).thenReturn(list1);
//            mockFormDataServiceImpl.saveFormData(formDataSchemaTest1);
//            Assertions.assertThrows(FormIdNotFoundException.class,()->mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
//            mockFormDataServiceImpl.saveFormData(formDataSchemaTest1);
//            verify(mockMongoTemplate, times(2)).save(any(), any());
//            verify(mockFormDataAuditServiceImpl, times(2)).saveFormData(formDataAuditSchemaTest);
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
//        Mockito.when(mockFormDataAuditServiceImpl.saveFormData(any())).thenReturn(formDataAuditResponse);
//        FormDataSchema formDataSchemaTest = new FormDataSchema(null, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
//        FormDataSchema formDataSchemaTest1 = new FormDataSchema("1", TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
//        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_FORM_ID, TEST_NAME, TEST_COMPONENTS, list,TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID, TEST_CREATED_ON, TEST_CREATED_BY_NAME, TEST_UPDATED_BY_ID, TEST_UPDATED_ON, TEST_UPDATED_BY_NAME);        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
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
//            //Mockito.when(mockMongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_+formDataSchemaTest.getFormId())).thenReturn(mockMongoCollection);
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
//            Document document = new Document(formDataMap);
//            Mockito.when(mockMongoTemplate.save(any(),any())).thenReturn(document);
//            when(mockObjectMapper.convertValue(any(), eq(Map.class))).thenReturn(formDataMap);
//            when(mockObjectMapper.convertValue(anyString(), eq(Map.class))).thenReturn(formDataMap);
//            when(mockObjectMapper.readValue(anyString(), eq(Map.class))).thenReturn(formDataMap).thenReturn(null);
//            when(mockObjectMapper.convertValue(any(), eq(LinkedHashMap.class))).thenReturn(data1);
//            Date currentDate = new Date();
//            Document document1 = new Document("version",1);
//            document1.append("formData",formDataMap);
//            document1.append("formMetaData",formDataMap);
//            document1.append("_id","1");
//            document1.append(CREATED_ON,currentDate);
//            document1.append(CREATED_BY_ID,"1");
//            document1.append(CREATED_BY_NAME,STRING);
//            Mockito.when(mockMongoTemplate.getCollection(any())).thenReturn(mongoCollectionDocument);
//            Mockito.when(mongoCollectionDocument.findOneAndReplace(any(Bson.class),any(),any())).thenReturn(document1);
//            FindIterable iterable = mock(FindIterable.class);
//            MongoCursor cursor = mock(MongoCursor.class);
//            when(mongoCollectionDocument.find()).thenReturn(iterable);
//            when(iterable.iterator()).thenReturn(cursor);
//            when(cursor.hasNext()).thenReturn(true).thenReturn(false);
//            when(cursor.next()).thenReturn(document1);
//            FormDataAuditSchema formDataAuditSchemaTest = new FormDataAuditSchema(TEST_ID_VALUE, TEST_FORM_DATA_ID,TEST_FORM_ID,TEST_VERSION, testFormData,testFormMetaData);
//            Mockito.when(mockFormDataAuditServiceImpl.saveFormData(formDataAuditSchemaTest)).thenReturn(new FormDataAuditResponse(TEST_ID_VALUE,TEST_VERSION));
//            Mockito.when(mockValidationCheckService.allFieldsValidations(any(),any(),any(),any())).thenReturn(list1);
//            Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),any(),any())).thenReturn(STRING).thenReturn(STRING).thenReturn("1").thenReturn(String.valueOf(new Exception())).thenReturn(String.valueOf(new Exception()));
//            mockFormDataServiceImpl.saveFormData(formDataSchemaTest1);
//            mockFormDataServiceImpl.saveFormData(formDataSchemaTest);
//            Assertions.assertThrows(InvalidInputException.class,()->mockFormDataServiceImpl.saveFormData(formDataSchemaTest1));
//            //Assertions.assertThrows(RecordUnableToSaveException.class,()->mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
//        }
//    }

    @Test
    void getAllFormDataByFormIdAndQ()
    {
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        FormDataSchema formDataSchemaTest = new FormDataSchema(null, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ +formDataSchemaTest.getFormId())).thenReturn(true);
        FormDataDefinition formDataDefinitionTest=new FormDataDefinition(BigInteger.valueOf(Long.parseLong(TEST_ID)),
                TEST_VERSION,testFormData,testFormMetaData);
        formDataDefinitionTest.setCreatedById(BigInteger.valueOf(Long.parseLong(TEST_CREATED_BY_ID)));
        formDataDefinitionTest.setCreatedOn(TEST_CREATED_ON);
        formDataDefinitionTest.setUpdatedById(BigInteger.valueOf(Long.parseLong(TEST_UPDATED_BY_ID)));
        formDataDefinitionTest.setUpdatedOn(TEST_UPDATED_ON);
        Mockito.doReturn(aggregationResults).when(mockMongoTemplate).aggregate(Mockito.any(Aggregation.class), Mockito.eq(COLLECTION), Mockito.eq(Map.class));
        Mockito.when(mockMongoTemplate.find(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(List.of(formDataDefinitionTest));
        List response = mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,TEST_RELATIONS, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING);
        List responseData = mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING);
        List responseData1 = mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,TEST_RELATIONS, EMPTY_STRING, CREATED_ON, DESCENDING);
//        Assertions.assertThrows(InvalidInputException.class,()->mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,EMPTY_STRING, EMPTY_STRING, CREATED_ON, DESCENDING));
        assertThat(response).isInstanceOf(List.class);
        assertThat(responseData).isInstanceOf(List.class);
        assertThat(responseData1).isInstanceOf(List.class);
    }
    //@Test
    void getAllFormDataByFormIdAndQElastic() throws JsonProcessingException {
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
        testFormData2.put(DATA,AGE_VALUE);
        testFormData2.put(CONTENT,AGE_VALUE);
        when(mockWebClientWrapper.createWebClient(any())).thenReturn(mockWebClient);
        when(mockWebClientWrapper.webclientRequest(any(WebClient.class), anyString(), anyString(), eq(null))).thenReturn(STRING);
        when(mockObjectMapper.readValue(anyString(), eq(Map.class))).thenReturn(testFormData2);
        when(mockObjectMapper.convertValue(anyString(), eq(Map.class))).thenReturn(testFormData2);
        when(mockObjectMapper.convertValue(anyString(), eq(ArrayList.class))).thenReturn(list1);
        when(mockObjectMapper.convertValue(any(), eq(LinkedHashMap.class))).thenReturn(data1);
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        FormDataSchema formDataSchemaTest = new FormDataSchema(TEST_ID, TEST_FORM_ID, TEST_VERSION, testFormData2, testFormMetaData);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ +formDataSchemaTest.getFormId())).thenReturn(true);
        Mockito.doReturn(aggregationResults).when(mockMongoTemplate).aggregate(Mockito.any(Aggregation.class), Mockito.eq(COLLECTION), Mockito.eq(Map.class));
        List responseData = mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,null, EMPTY_STRING, CREATED_ON, DESCENDING);
        List responseData1 = mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,null, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING);
        List responseData2 = mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,null, STRING, EMPTY_STRING, EMPTY_STRING);
        List responseData3 = mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,null, STRING, CREATED_ON, DESCENDING);
        assertThat(responseData).isInstanceOf(List.class);
        assertThat(responseData1).isInstanceOf(List.class);
        assertThat(responseData2).isInstanceOf(List.class);
        assertThat(responseData3).isInstanceOf(List.class);
    }
    //@Test
    void getAllFormDataByFormIdAndQEmptySortBySortOrderPaginationTest()
    {
        Pageable pageable = PageRequest.of(0,5);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        FormDataSchema formDataSchemaTest = new FormDataSchema(null, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ +formDataSchemaTest.getFormId())).thenReturn(true);
        MongoCollection mongoCollection=mock(MongoCollection.class);
        Mockito.when(mockMongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_ +formDataSchemaTest.getFormId())).thenReturn(mongoCollection);
        Mockito.when(mongoCollection.countDocuments()).thenReturn(Long.valueOf(2));
        Mockito.doReturn(aggregationResults).when(mockMongoTemplate).aggregate(Mockito.any(Aggregation.class), Mockito.eq(COLLECTION), Mockito.eq(Map.class));
        FormDataDefinition formDataDefinitionTest=new FormDataDefinition(BigInteger.valueOf(Long.parseLong(TEST_ID)),
                TEST_VERSION,testFormData,testFormMetaData);
        formDataDefinitionTest.setCreatedById(BigInteger.valueOf(Long.parseLong(TEST_CREATED_BY_ID)));
        formDataDefinitionTest.setCreatedOn(TEST_CREATED_ON);
        formDataDefinitionTest.setUpdatedById(BigInteger.valueOf(Long.parseLong(TEST_UPDATED_BY_ID)));
        formDataDefinitionTest.setUpdatedOn(TEST_UPDATED_ON);
        Mockito.when(mockMongoTemplate.find(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(List.of(formDataDefinitionTest));
        PaginationResponsePayload response1 = mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,null, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, PageRequest.of(0,5));
        PaginationResponsePayload response2 = mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,TEST_RELATIONS, EMPTY_STRING, STRING, DESCENDING, PageRequest.of(0,5));
        PaginationResponsePayload response3 = mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,TEST_RELATIONS, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, PageRequest.of(0,5));
        assertThat(response1).isInstanceOf(PaginationResponsePayload.class);
        assertThat(response2).isInstanceOf(PaginationResponsePayload.class);
        assertThat(response3).isInstanceOf(PaginationResponsePayload.class);
        //Assertions.assertThrows(InvalidInputException.class,()->mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,null, EMPTY_STRING, STRING, DESCENDING, pageable));
    }

    //@Test
    void getAllFormDataByFormIdAndQSortBySortOrderPaginationTest() throws JsonProcessingException
    {
        ReflectionTestUtils.setField(mockFormDataServiceImpl,"elasticEnable", true);
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),any(),any())).thenReturn(STRING);
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
        when(mockObjectMapper.readValue(anyString(), eq(Map.class))).thenReturn(testFormData2);
        when(mockObjectMapper.convertValue(anyString(), eq(ArrayList.class))).thenReturn(list1);
        when(mockObjectMapper.convertValue(anyString(), eq(Map.class))).thenReturn(testFormData2);
        when(mockObjectMapper.convertValue(any(), eq(LinkedHashMap.class))).thenReturn(data1);
        PaginationResponsePayload response = mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,null, STRING, CREATED_ON, DESCENDING, PageRequest.of(0,5));
        PaginationResponsePayload response1 = mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,null, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, PageRequest.of(0,5));
        PaginationResponsePayload response2 = mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,null, EMPTY_STRING, CREATED_ON, DESCENDING, PageRequest.of(0,5));
        PaginationResponsePayload response3 = mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,null, STRING, EMPTY_STRING, EMPTY_STRING, PageRequest.of(0,5));
        assertThat(response).isInstanceOf(PaginationResponsePayload.class);
        assertThat(response1).isInstanceOf(PaginationResponsePayload.class);
        assertThat(response2).isInstanceOf(PaginationResponsePayload.class);
        assertThat(response3).isInstanceOf(PaginationResponsePayload.class);
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
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ +formDataSchemaTest.getFormId())).thenReturn(true);
        FormDataResponse formDataResponse = mockFormDataServiceImpl.updateFormData(formDataSchemaTest);
        assertThat(formDataResponse).isInstanceOf(FormDataResponse.class);
    }

    @Test
    void deleteAllFormDataByFormIdTest() {
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ + TEST_FORM_ID)).thenReturn(true);
        mockFormDataServiceImpl.deleteAllFormDataByFormId(TEST_FORM_ID);
        verify(mockMongoTemplate, times(1)).dropCollection(TP_RUNTIME_FORM_DATA_ + TEST_FORM_ID);
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
//        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_ID, TEST_NAME, TEST_COMPONENTS,list,TEST_PROPERTIES, TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID, TEST_CREATED_ON, TEST_CREATED_BY_NAME, TEST_UPDATED_BY_ID, TEST_UPDATED_ON, TEST_UPDATED_BY_NAME);
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
//    void validateFormDataByFormIdException() throws Exception
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
//        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_ID, TEST_NAME, TEST_COMPONENTS,list,TEST_PROPERTIES, TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID, TEST_CREATED_ON, TEST_CREATED_BY_NAME, TEST_UPDATED_BY_ID, TEST_UPDATED_ON, TEST_UPDATED_BY_NAME);
//        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
//        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
//        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
//        fieldsMap.put(REQUIRED,false);
//        fieldsMap.put(UNIQUE,false);
//        schemaMap.put(NAME,fieldsMap);
//        try (MockedStatic<ValidateFormUtils> mockValidateFormUtils = Mockito.mockStatic(ValidateFormUtils.class)) {
//            mockValidateFormUtils.when(() -> ValidateFormUtils.getSchema(formResponseSchemaTest.getComponents()))
//                    .thenReturn(schemaMap);
//            for(int i=0;i<=9;i++) {
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
//        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_ID, TEST_NAME, TEST_COMPONENTS,list,TEST_PROPERTIES, TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID, TEST_CREATED_ON, TEST_CREATED_BY_NAME, TEST_UPDATED_BY_ID, TEST_UPDATED_ON, TEST_UPDATED_BY_NAME);
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
//        }
}
