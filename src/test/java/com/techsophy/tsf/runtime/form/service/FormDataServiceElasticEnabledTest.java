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
import com.techsophy.tsf.runtime.form.service.impl.FormDataAuditServiceImpl;
import com.techsophy.tsf.runtime.form.service.impl.FormDataServiceImpl;
import com.techsophy.tsf.runtime.form.service.impl.ValidationCheckServiceImpl;
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
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
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
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.ELASTIC_ENABLE;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.ELASTIC_SOURCE;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.FORM_DATA;

import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.ONE;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.SUCCESS;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.ZERO;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static shadow.org.assertj.core.api.Assertions.assertThat;

@ExtendWith({SpringExtension.class})
@SpringBootTest
@ActiveProfiles(TEST_ACTIVE_PROFILE)
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
    ValidationCheckServiceImpl mockValidationCheckService;
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
    @InjectMocks
    FormDataServiceImpl mockFormDataServiceImpl;

    List<Map<String, Object>> userList = new ArrayList<>();
    Map<String, Object> map = new HashMap<>();

    @BeforeEach
    public void init()
    {
        ReflectionTestUtils.setField(mockFormDataServiceImpl,ELASTIC_SOURCE,true);
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
    void getAllFormDataByFormIdEmptySortBySortOrderTest() throws JsonProcessingException
    {
        ReflectionTestUtils.setField(mockFormDataServiceImpl, ELASTIC_ENABLE, true);
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        String responseTest =RESPONSE_VALUE_16;
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(GET),any())).thenReturn(responseTest);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        LinkedHashMap<String, Object> testFormData = new LinkedHashMap<>();
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
        responseMapTest.put(DATA, dataMapTest);
        responseMapTest.put(SUCCESS,true);
        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ +TEST_FORM_ID)).thenReturn(true);
        Mockito.doReturn(aggregationResults).when(mockMongoTemplate).aggregate(Mockito.any(Aggregation.class), Mockito.eq(COLLECTION), Mockito.eq(Map.class));
        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
        Mockito.when(mockObjectMapper.convertValue(dataMapTest.get(CONTENT),ArrayList.class)).thenReturn(contentListTest);
        Mockito.when(mockObjectMapper.convertValue(contentListTest.get(0), LinkedHashMap.class)).thenReturn(singleContentTest);
        List response = mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID, TEST_RELATIONS, TEST_FILTER, null, null);
        assertThat(response).isInstanceOf(List.class);
    }

//    @Test
//    void getAllFormDataByFormIdSortBySortOrderTest() throws JsonProcessingException
//    {
//        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        String responseTest =RESPONSE_VALUE_17;
//        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(GET),any())).thenReturn(responseTest);
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        LinkedHashMap<String, Object> testFormData = new LinkedHashMap<>();
//        testFormData.put(NAME, NAME_VALUE);
//        testFormData.put(AGE,AGE_VALUE);
//        Map<String,Object> responseMapTest =new HashMap<>();
//        ArrayList contentListTest =new ArrayList();
//        LinkedHashMap<String,Object> singleContentTest =new LinkedHashMap<>();
//        singleContentTest.put(FORM_ID,TEST_FORM_ID);
//        singleContentTest.put(ID,TEST_ID);
//        singleContentTest.put(VERSION,TEST_VERSION);
//        singleContentTest.put(FORM_DATA,testFormData);
//        singleContentTest.put(FORM_META_DATA,testFormMetaData);
//        singleContentTest.put(CREATED_BY_NAME,CREATED_BY_USER_NAME);
//        singleContentTest.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
//        singleContentTest.put(CREATED_BY_ID,BIGINTEGER_ID);
//        singleContentTest.put(UPDATED_BY_ID,BIGINTEGER_ID);
//        singleContentTest.put(CREATED_ON,TEST_CREATED_ON);
//        singleContentTest.put(UPDATED_ON,TEST_UPDATED_ON);
//        contentListTest.add(singleContentTest);
//        Map<String,Object> dataMapTest =new HashMap<>();
//        dataMapTest.put(CONTENT, contentListTest);
//        dataMapTest.put(TOTAL_PAGES, ONE);
//        dataMapTest.put(TOTAL_ELEMENTS,ONE);
//        dataMapTest.put(PAGE,ZERO);
//        dataMapTest.put(SIZE,PAGE_SIZE);
//        dataMapTest.put(NUMBER_OF_ELEMENTS,ONE);
//        responseMapTest.put(DATA, dataMapTest);
//        responseMapTest.put(SUCCESS,true);
//        FormDataDefinition formDataDefinition = new FormDataDefinition(BigInteger.ONE,1,dataMapTest,dataMapTest);
//        Mockito.when(mockMongoTemplate.find(any(),any(),any())).thenReturn(List.of(formDataDefinition));
//        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA +TEST_FORM_ID)).thenReturn(true);
//        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
//        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
//        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
//        Mockito.when(mockObjectMapper.convertValue(dataMapTest.get(CONTENT),ArrayList.class)).thenReturn(contentListTest);
//        Mockito.when(mockObjectMapper.convertValue(contentListTest.get(0), LinkedHashMap.class)).thenReturn(singleContentTest);
//        Mockito.doReturn(aggregationResults).when(mockMongoTemplate).aggregate(Mockito.any(Aggregation.class), Mockito.eq(COLLECTION), Mockito.eq(Map.class));
//        List response = mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID, null, TEST_FILTER, EMPTY_STRING, EMPTY_STRING);
//        List response1 = mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID, TEST_RELATIONS, TEST_FILTER, CREATED_ON, DESCENDING);
//        List response2 = mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID, null, TEST_FILTER, CREATED_ON, DESCENDING);
//        assertThat(response).isInstanceOf(List.class);
//        assertThat(response1).isInstanceOf(List.class);
//        assertThat(response2).isInstanceOf(List.class);
//    }
   // @Test
    void getAllFormDataByFormIdElastic() throws Exception
    {
        ReflectionTestUtils.setField(mockFormDataServiceImpl,"elasticEnable",true);
        List<String> list1 = new ArrayList<>();
        list1.add("10");
        list1.add("2");
        doReturn(userList).when(mockUserDetails).getUserDetails();
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put("id",null);
        FormDataAuditResponse formDataAuditResponse = new FormDataAuditResponse("1",1);
        Mockito.when(mockFormDataAuditServiceImpl.saveFormDataAudit(any())).thenReturn(formDataAuditResponse);
        FormDataSchema formDataSchemaTest = new FormDataSchema(null, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
        FormDataSchema formDataSchemaTest1 = new FormDataSchema("1", TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
        LinkedHashMap<String,Map<String,Object>> schemaMap=new LinkedHashMap<>();
        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
        fieldsMap.put(REQUIRED,false);
        fieldsMap.put(UNIQUE,false);
        schemaMap.put(NAME,fieldsMap);
        Map<String, Object> givenData = new HashMap<>();
        givenData.put(NAME, NAME_VALUE);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ +formDataSchemaTest.getFormId())).thenReturn(true);
        Mockito.when(mockIdGeneratorImpl.nextId()).thenReturn(BigInteger.valueOf(Long.parseLong(TEST_FORM_ID)));
        Map<String, Object> formDataMap = new HashMap<>();
        formDataMap.put(UNDERSCORE_ID,Long.parseLong(UNDERSCORE_ID_VALUE));
        formDataMap.put(FORM_ID,TEST_FORM_ID);
        formDataMap.put(VERSION, String.valueOf(1));
        formDataMap.put(FORM_META_DATA, formDataSchemaTest.getFormMetadata());
        formDataMap.put(FORM_DATA, formDataSchemaTest.getFormData());
        formDataMap.put(CREATED_ON, Instant.now());
        formDataMap.put(CREATED_BY_ID,CREATED_BY_USER_ID);
        formDataMap.put(CREATED_BY_NAME, CREATED_BY_USER_NAME);
        formDataMap.put(UPDATED_ON,TEST_UPDATED_ON);
        formDataMap.put(UPDATED_BY_ID,UPDATED_BY_USER_ID);
        formDataMap.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        formDataMap.put(DATA,UPDATED_BY_USER_NAME);
        LinkedHashMap data1 = new LinkedHashMap<>();
        data1.put("abc","abc");
        LinkedHashMap<String,Object> data = new LinkedHashMap<>();
        data.put(FORM_DATA,data1);
        ArrayList list2 = new ArrayList<>();
        list2.add(data);
        Document document = new Document(formDataMap);
        Mockito.when(mockMongoTemplate.save(any(),any())).thenReturn(document);
        when(mockObjectMapper.convertValue(any(), eq(Map.class))).thenReturn(formDataMap);
        when(mockObjectMapper.convertValue(anyString(), eq(Map.class))).thenReturn(formDataMap);
        when(mockObjectMapper.readValue(anyString(), eq(Map.class))).thenReturn(formDataMap);
        when(mockObjectMapper.convertValue(any(), eq(LinkedHashMap.class))).thenReturn(data1);
        when(mockObjectMapper.convertValue(any(), eq(ArrayList.class))).thenReturn(list2);

        Date currentDate = new Date();
        Document document1 = new Document("version",1);
        document1.append("formData",formDataMap);
        document1.append("formMetaData",formDataMap);
        document1.append("_id","1");
        document1.append(CREATED_ON,currentDate);
        document1.append(CREATED_BY_ID,"1");
        document1.append(CREATED_BY_NAME,STRING);
        Mockito.when(mockMongoTemplate.getCollection(any())).thenReturn(mongoCollectionDocument);
        Mockito.when(mongoCollectionDocument.findOneAndReplace(any(Bson.class),any(),any())).thenReturn(document1);
        FindIterable iterable = mock(FindIterable.class);
        MongoCursor cursor = mock(MongoCursor.class);
        when(mongoCollectionDocument.find()).thenReturn(iterable);
        when(iterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true).thenReturn(false);
        when(cursor.next()).thenReturn(document1);
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),any(),any())).thenReturn(STRING);
        List response = mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID, null, TEST_FILTER, CREATED_ON, DESCENDING);
        assertThat(response).isInstanceOf(List.class);
    }
    //@Test
    void getAllFormDataByFormId() throws Exception
    {
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        String responseTest =RESPONSE_VALUE_17;
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(GET),any())).thenReturn(responseTest);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        LinkedHashMap<String, Object> testFormData = new LinkedHashMap<>();
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
        dataMapTest.put(TOTAL_PAGES, ONE);
        dataMapTest.put(TOTAL_ELEMENTS,ONE);
        dataMapTest.put(PAGE,ZERO);
        dataMapTest.put(SIZE,PAGE_SIZE);
        dataMapTest.put(NUMBER_OF_ELEMENTS,ONE);
        responseMapTest.put(DATA, dataMapTest);
        responseMapTest.put(SUCCESS,true);
        FormDataDefinition formDataDefinition = new FormDataDefinition(BigInteger.ONE,1,dataMapTest,dataMapTest);
        Mockito.when(mockMongoTemplate.find(any(),any(),any())).thenReturn(List.of(formDataDefinition));
        Mockito.when(mockMongoTemplate.getCollection(any())).thenReturn(mockMongoCollection);
        Mockito.when(mockMongoCollection.countDocuments()).thenReturn(1L);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ +TEST_FORM_ID)).thenReturn(true);
        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
        Mockito.doReturn(aggregationResults).when(mockMongoTemplate).aggregate(Mockito.any(Aggregation.class), Mockito.eq(COLLECTION), Mockito.eq(Map.class));
        PaginationResponsePayload response = mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID, TEST_RELATIONS);
        PaginationResponsePayload response1 = mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID, null);
        assertThat(response).isInstanceOf(PaginationResponsePayload.class);
        assertThat(response1).isInstanceOf(PaginationResponsePayload.class);
    }
    //@Test
    void getAllFormDataByFormIdElasticEnable() throws Exception
    {
        ReflectionTestUtils.setField(mockFormDataServiceImpl, ELASTIC_ENABLE, true);
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        String responseTest =RESPONSE_VALUE_17;
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(GET),any())).thenReturn(responseTest);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        LinkedHashMap<String, Object> testFormData = new LinkedHashMap<>();
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
        dataMapTest.put(TOTAL_PAGES, ONE);
        dataMapTest.put(TOTAL_ELEMENTS,ONE);
        dataMapTest.put(PAGE,ZERO);
        dataMapTest.put(SIZE,PAGE_SIZE);
        dataMapTest.put(NUMBER_OF_ELEMENTS,ONE);
        responseMapTest.put(DATA, dataMapTest);
        responseMapTest.put(SUCCESS,true);
        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
        Mockito.when(mockObjectMapper.convertValue(dataMapTest.get(CONTENT),ArrayList.class)).thenReturn(contentListTest);
        Mockito.when(mockObjectMapper.convertValue(any(), eq(LinkedHashMap.class))).thenReturn(singleContentTest);
        FormDataDefinition formDataDefinition = new FormDataDefinition(BigInteger.ONE,1,dataMapTest,dataMapTest);
        Mockito.when(mockMongoTemplate.find(any(),any(),any())).thenReturn(List.of(formDataDefinition));
        Mockito.when(mockMongoTemplate.getCollection(any())).thenReturn(mockMongoCollection);
        Mockito.when(mockMongoCollection.countDocuments()).thenReturn(1L);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ +TEST_FORM_ID)).thenReturn(true);
        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
        Mockito.doReturn(aggregationResults).when(mockMongoTemplate).aggregate(Mockito.any(Aggregation.class), Mockito.eq(COLLECTION), Mockito.eq(Map.class));
        PaginationResponsePayload response = mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID, null);
        assertThat(response).isInstanceOf(PaginationResponsePayload.class);
    }

    @Test
    void getFormDataByFormIdAndIdElastic() throws Exception
    {
        ReflectionTestUtils.setField(mockFormDataServiceImpl,"elasticEnable",true);
        List<Map<String,Object>> list = new ArrayList<>();
        LinkedHashMap<String,Object> data = new LinkedHashMap<>();
        List<LinkedHashMap> data1 = new ArrayList();
        LinkedHashMap linkedHashMap = new LinkedHashMap<>();
        LinkedHashMap linkedHashMap1 = new LinkedHashMap<>();
        linkedHashMap1.put("abc","abc");
        linkedHashMap.put(FORM_DATA,linkedHashMap1);
        data1.add(linkedHashMap);
        data.put(FORM_DATA,data1);
        ArrayList list1 = new ArrayList<>();
        list1.add(data);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        list.add(testFormMetaData);
        Map<String, Object> testFormData2 = new HashMap<>();
        Map<String, Object> testFormData3 = new HashMap<>();
        testFormData3.put(FORM_DATA, linkedHashMap);
        testFormData2.put(NAME, NAME_VALUE);
        testFormData2.put(AGE,AGE_VALUE);
        testFormData2.put(DATA,AGE_VALUE);
        testFormData2.put(CONTENT,AGE_VALUE);
        testFormData2.put(PAGE,AGE_VALUE);
        testFormData2.put(SIZE,AGE_VALUE);
        testFormData2.put(TOTAL_PAGES,AGE_VALUE);
        testFormData2.put(TOTAL_ELEMENTS,1L);
        testFormData2.put(NUMBER_OF_ELEMENTS,AGE_VALUE);
        when(mockWebClientWrapper.createWebClient(any())).thenReturn(mockWebClient);
        when(mockWebClientWrapper.webclientRequest(any(WebClient.class), anyString(), anyString(), eq(null))).thenReturn(STRING);
        when(mockObjectMapper.readValue(anyString(), eq(Map.class))).thenReturn(testFormData2);
        when(mockObjectMapper.convertValue(any(), eq(Map.class))).thenReturn(testFormData3);
        when(mockObjectMapper.convertValue(anyString(), eq(ArrayList.class))).thenReturn(list1);
        when(mockObjectMapper.convertValue(any(), eq(LinkedHashMap.class))).thenReturn(linkedHashMap);
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        FormDataSchema formDataSchemaTest = new FormDataSchema(TEST_ID, TEST_FORM_ID, TEST_VERSION, testFormData2, testFormMetaData);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ +formDataSchemaTest.getFormId())).thenReturn(true);
        Mockito.doReturn(aggregationResults).when(mockMongoTemplate).aggregate(Mockito.any(Aggregation.class), Mockito.eq(COLLECTION), Mockito.eq(Map.class));
        List response = mockFormDataServiceImpl.getFormDataByFormIdAndId(TEST_FORM_ID,TEST_FORM_ID, null);
        assertThat(response).isInstanceOf(List.class);
    }
    @Test
    void getFormDataByFormIdAndId() throws Exception
    {
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        String responseTest =RESPONSE_VALUE_17;
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(GET),any())).thenReturn(responseTest);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        LinkedHashMap<String, Object> testFormData = new LinkedHashMap<>();
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
        dataMapTest.put(TOTAL_PAGES, ONE);
        dataMapTest.put(TOTAL_ELEMENTS,ONE);
        dataMapTest.put(PAGE,ZERO);
        dataMapTest.put(SIZE,PAGE_SIZE);
        dataMapTest.put(NUMBER_OF_ELEMENTS,ONE);
        responseMapTest.put(DATA, dataMapTest);
        responseMapTest.put(SUCCESS,true);
        FormDataDefinition formDataDefinition = new FormDataDefinition(BigInteger.ONE,1,dataMapTest,dataMapTest);
        Mockito.when(mockMongoTemplate.find(any(),any(),any())).thenReturn(List.of(formDataDefinition));
        Mockito.when(mockMongoTemplate.getCollection(any())).thenReturn(mockMongoCollection);
        Mockito.when(mockMongoCollection.countDocuments()).thenReturn(1L);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ +TEST_FORM_ID)).thenReturn(true);
        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
        Date currentDate = new Date();
        Document document = new Document("version",1);
        document.append("formData",STRING);
        document.append("formMetaData",STRING);
        document.append(UNDERSCORE_ID,"1");
        document.append(VERSION,"1");
        document.append(CREATED_ON,currentDate);
        document.append(UPDATED_ON,currentDate);
        document.append(CREATED_BY_ID,"1");
        document.append(UPDATED_BY_ID,"1");
        document.append(CREATED_BY_NAME,STRING);
        document.append(UPDATED_BY_NAME,STRING);
        Mockito.when(mockMongoTemplate.getCollection(any())).thenReturn(mongoCollectionDocument);
        Mockito.when(mongoCollectionDocument.find(any(Bson.class))).thenReturn(mockDocuments);
        Mockito.when(mockDocuments.iterator()).thenReturn(mongoCursor);
        Mockito.when(mongoCursor.hasNext()).thenReturn(true).thenReturn(false);
        Mockito.when(mongoCursor.next()).thenReturn(document);
        Mockito.doReturn(aggregationResults).when(mockMongoTemplate).aggregate(Mockito.any(Aggregation.class), Mockito.eq(COLLECTION), Mockito.eq(Map.class));
        List response = mockFormDataServiceImpl.getFormDataByFormIdAndId(TEST_FORM_ID, TEST_FORM_ID,TEST_RELATIONS);
        List response1 = mockFormDataServiceImpl.getFormDataByFormIdAndId(TEST_FORM_ID, TEST_FORM_ID,null);
        assertThat(response).isInstanceOf(List.class);
        assertThat(response1).isInstanceOf(List.class);
     }

    @Test
    void deleteAllFormDataByFormIdTest() throws JsonProcessingException
    {
        ReflectionTestUtils.setField(mockFormDataServiceImpl, ELASTIC_ENABLE, true);
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
        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ +TEST_FORM_ID)).thenReturn(true);
        mockFormDataServiceImpl.deleteAllFormDataByFormId(TEST_FORM_ID);
        verify(mockWebClientWrapper,times(1)).webclientRequest(any(),any(),any(),any());
    }

    //@Test
    void deleteFormDataByFormIdAndIdTest() throws JsonProcessingException
    {
        ReflectionTestUtils.setField(mockFormDataServiceImpl, ELASTIC_ENABLE, true);
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
        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ +TEST_FORM_ID)).thenReturn(true);
        Mockito.when(mockMongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_ +TEST_FORM_ID)).thenReturn(mockMongoCollection);
        DeleteResult mockDeleteResult=Mockito.mock(DeleteResult.class);
        Mockito.when(mockMongoCollection.deleteMany(any())).thenReturn(mockDeleteResult);
        Mockito.when(mockDeleteResult.getDeletedCount()).thenReturn(Long.valueOf(1));
        mockFormDataServiceImpl.deleteFormDataByFormIdAndId(TEST_FORM_ID,TEST_ID);
        verify(mockWebClientWrapper,times(1)).webclientRequest(any(),any(),any(),any());
    }
}





