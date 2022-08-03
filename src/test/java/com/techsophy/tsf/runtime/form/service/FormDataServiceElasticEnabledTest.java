package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.techsophy.idgenerator.IdGeneratorImpl;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.service.impl.FormDataAuditServiceImpl;
import com.techsophy.tsf.runtime.form.service.impl.FormDataServiceImpl;
import com.techsophy.tsf.runtime.form.service.impl.ValidationCheckServiceImpl;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import com.techsophy.tsf.runtime.form.utils.UserDetails;
import com.techsophy.tsf.runtime.form.utils.WebClientWrapper;
import org.bson.Document;
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
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.CREATED_BY_ID;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.CREATED_BY_NAME;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.CREATED_ON;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.ELASTIC_ENABLE;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.ELASTIC_SOURCE;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.EMPTY_STRING;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.ID;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.NULL;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.UPDATED_BY_ID;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.UPDATED_BY_NAME;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.UPDATED_ON;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
    MongoCollection mockMongoCollection;
    @Mock
    InsertOneResult mockInsertOneResult;
    @Mock
    MongoCollection<Document> mockDocument;
    @Mock
    DeleteResult mockDeleteResult;
    @Mock
    AggregationOperation mockAggregationOperation;

    @InjectMocks
    FormDataServiceImpl mockFormDataServiceImpl;

    List<Map<String, Object>> userList = new ArrayList<>();
    Map<String, Object> map = new HashMap<>();

    @BeforeEach
    public void init()
    {
        ReflectionTestUtils.setField(mockFormDataServiceImpl, ELASTIC_ENABLE, true);
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

//    @Test
//    void saveFormDataUpdateIndexTest() throws JsonProcessingException
//    {
//        doReturn(userList).when(mockUserDetails).getUserDetails();
//        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        Map<String, Object> testFormData = new HashMap<>();
//        testFormData.put(NAME, NAME_VALUE);
//        testFormData.put(AGE,AGE_VALUE);
//        FormDataSchema formDataSchemaTest = new FormDataSchema(TEST_ID, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
//        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_FORM_ID, TEST_NAME, TEST_COMPONENTS,TEST_PROPERTIES, TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE,TEST_CREATED_BY_ID, TEST_CREATED_ON, TEST_CREATED_BY_NAME, TEST_UPDATED_BY_ID, TEST_UPDATED_ON, TEST_UPDATED_BY_NAME);
//        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
//        Map<String,Map<String,Object>> schemaMap=new HashMap<>();
//        Map<String,Object> fieldsMap=new HashMap<>();
//        fieldsMap.put(REQUIRED,false);
//        fieldsMap.put(UNIQUE,false);
//        schemaMap.put(NAME,fieldsMap);
//        try (MockedStatic<ValidateFormUtils> mockValidateFormUtils = Mockito.mockStatic(ValidateFormUtils.class))
//        {
//            mockValidateFormUtils.when(() -> ValidateFormUtils.getSchema(formResponseSchemaTest.getComponents()))
//                    .thenReturn(schemaMap);
//            Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_+formDataSchemaTest.getFormId())).thenReturn(true);
//            Mockito.when(mockMongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_+formDataSchemaTest.getFormId())).thenReturn(mockMongoCollection);
//            Mockito.when(mockIdGeneratorImpl.nextId()).thenReturn(BigInteger.valueOf(Long.parseLong(TEST_ID_VALUE)));
//            FormDataAuditSchema formDataAuditSchemaTest = new FormDataAuditSchema(TEST_ID_VALUE,TEST_FORM_DATA_ID,TEST_FORM_ID,TEST_VERSION, testFormData,testFormMetaData);
//            Mockito.when(mockFormDataAuditServiceImpl.saveFormData(formDataAuditSchemaTest)).thenReturn(new FormDataAuditResponse(TEST_ID_VALUE,TEST_VERSION));
//            String responseTest =RESPONSE_VALUE_15;
//            Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(GET),any())).thenReturn(responseTest);
//            Map<String,Object> responseMapTest=new HashMap<>();
//            LinkedHashMap<String,Object> dataMapTest =new LinkedHashMap<>();
//            dataMapTest.put(FORM_ID,TEST_FORM_ID);
//            dataMapTest.put(UNDERSCORE_ID,Long.parseLong(TEST_ID));
//            dataMapTest.put(ID,TEST_ID);
//            dataMapTest.put(VERSION,TEST_VERSION);
//            dataMapTest.put(FORM_DATA,testFormData);
//            dataMapTest.put(FORM_META_DATA,testFormMetaData);
//            dataMapTest.put(CREATED_BY_NAME,CREATED_BY_USER_NAME);
//            dataMapTest.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
//            dataMapTest.put(CREATED_BY_ID,BIGINTEGER_ID);
//            dataMapTest.put(UPDATED_BY_ID,BIGINTEGER_ID);
//            Date date=new Date();
//            dataMapTest.put(CREATED_ON,date);
//            dataMapTest.put(UPDATED_ON,date);
//            Document document = new Document(dataMapTest);
//            responseMapTest.put(DATA, dataMapTest);
//            responseMapTest.put(SUCCESS,true);
//            responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
//            FindIterable iterable = mock(FindIterable.class);
//            MongoCursor cursor = mock(MongoCursor.class);
//            Mockito.when(mockMongoCollection.find()).thenReturn(iterable);
//            Mockito.when(iterable.iterator()).thenReturn(cursor);
//            Mockito.when(cursor.hasNext()).thenReturn(true).thenReturn(false);
//            Mockito.when(cursor.next()).thenReturn(document);
//            Mockito.when(mockMongoCollection.findOneAndReplace((Bson) any(),any(), (FindOneAndReplaceOptions) any())).thenReturn(document);
//            Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
//            Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
//            Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(POST),any())).thenReturn(SUCCESS);
//            Mockito.when(mockValidationCheckService.allFieldsValidations(any(),any(),any(),any())).thenReturn(10);
//            final Map<String,Object> updatedElasticFormData=dataMapTest;
//            Mockito.when(mockObjectMapper.convertValue(any(), (Class<Object>) any())).thenReturn(updatedElasticFormData);
//            mockFormDataServiceImpl.saveFormData(formDataSchemaTest);
//            verify(mockWebClientWrapper,times(2)).webclientRequest(any(),any(),any(),any());
//            verify(mockFormDataAuditServiceImpl,times(1)).saveFormData(any());
//        }}

//    @Test
//    void getAllFormDataByFormIdEmptySortBySortOrderTest() throws JsonProcessingException
//    {
//        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        String responseTest =RESPONSE_VALUE_16;
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
//        responseMapTest.put(DATA, dataMapTest);
//        responseMapTest.put(SUCCESS,true);
//        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
//        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
//        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
//        Mockito.when(mockObjectMapper.convertValue(dataMapTest.get(CONTENT),ArrayList.class)).thenReturn(contentListTest);
//        Mockito.when(mockObjectMapper.convertValue(contentListTest.get(0), LinkedHashMap.class)).thenReturn(singleContentTest);
//        mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID, TEST_RELATIONS, TEST_FILTER, null, null);
//        verify(mockWebClientWrapper, times(1)).webclientRequest(any(), any(), any(), any());
//    }

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
//        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
//        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
//        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
//        Mockito.when(mockObjectMapper.convertValue(dataMapTest.get(CONTENT),ArrayList.class)).thenReturn(contentListTest);
//        Mockito.when(mockObjectMapper.convertValue(contentListTest.get(0), LinkedHashMap.class)).thenReturn(singleContentTest);
//        mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID, TEST_RELATIONS, TEST_FILTER, CREATED_ON, DESCENDING);
//        verify(mockWebClientWrapper, times(1)).webclientRequest(any(), any(), any(), any());
//    }

//    @Test
//    void getAllFormDataByFormIdPaginationEmptySortBySortOrderTest() throws JsonProcessingException
//    {
//        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        String responseTest =RESPONSE_VALUE_18;
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
//        dataMapTest.put(TOTAL_PAGES,ONE);
//        dataMapTest.put(TOTAL_ELEMENTS,ONE);
//        dataMapTest.put(PAGE,ZERO);
//        dataMapTest.put(SIZE,PAGE_SIZE);
//        dataMapTest.put(NUMBER_OF_ELEMENTS,ONE);
//        responseMapTest.put(DATA, dataMapTest);
//        responseMapTest.put(SUCCESS,true);
//        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
//        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
//        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
//        Mockito.when(mockObjectMapper.convertValue(dataMapTest.get(CONTENT),ArrayList.class)).thenReturn(contentListTest);
//        Mockito.when(mockObjectMapper.convertValue(contentListTest.get(0), LinkedHashMap.class)).thenReturn(singleContentTest);
//        mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID, TEST_RELATIONS, TEST_FILTER, null, null, PageRequest.of(0, 5));
//        verify(mockWebClientWrapper, times(1)).webclientRequest(any(), any(), any(), any());
//    }

//    @Test
//    void getAllFormDataByFormIdPaginationSortBySortOrderTest() throws JsonProcessingException
//    {
//        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        String responseTest =RESPONSE_VALUE_19;
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
//        dataMapTest.put(TOTAL_PAGES,ONE);
//        dataMapTest.put(TOTAL_ELEMENTS,ONE);
//        dataMapTest.put(PAGE,ZERO);
//        dataMapTest.put(SIZE,PAGE_SIZE);
//        dataMapTest.put(NUMBER_OF_ELEMENTS,ONE);
//        responseMapTest.put(DATA,dataMapTest);
//        responseMapTest.put(SUCCESS,true);
//        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
//        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
//        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
//        Mockito.when(mockObjectMapper.convertValue(dataMapTest.get(CONTENT),ArrayList.class)).thenReturn(contentListTest);
//        Mockito.when(mockObjectMapper.convertValue(contentListTest.get(0), LinkedHashMap.class)).thenReturn(singleContentTest);
//        mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID, TEST_RELATIONS, TEST_FILTER, CREATED_ON, DESCENDING, PageRequest.of(0, 5));
//        verify(mockWebClientWrapper, times(1)).webclientRequest(any(), any(), any(), any());
//    }

//    @Test
//    void getAllFormDataByFormIdAndQAllEmptySortQTest() throws JsonProcessingException
//    {
//        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        String responseTest =RESPONSE_VALUE_20;
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
//        dataMapTest.put(TOTAL_PAGES,ONE);
//        dataMapTest.put(TOTAL_ELEMENTS,ONE);
//        dataMapTest.put(PAGE,ZERO);
//        dataMapTest.put(SIZE,PAGE_SIZE);
//        dataMapTest.put(NUMBER_OF_ELEMENTS,ONE);
//        responseMapTest.put(DATA,dataMapTest);
//        responseMapTest.put(SUCCESS,true);
//        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
//        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
//        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
//        Mockito.when(mockObjectMapper.convertValue(dataMapTest.get(CONTENT),ArrayList.class)).thenReturn(contentListTest);
//        Mockito.when(mockObjectMapper.convertValue(contentListTest.get(0), LinkedHashMap.class)).thenReturn(singleContentTest);
//        mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID, TEST_RELATIONS, EMPTY_STRING, null, null);
//        verify(mockWebClientWrapper, times(1)).webclientRequest(any(), any(), any(), any());
//    }

//    @Test
//    void getAllFormDataByFormIdAndQEmptySortPresentTest() throws JsonProcessingException
//    {
//        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        String responseTest =RESPONSE_VALUE_21;
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
//        dataMapTest.put(TOTAL_PAGES,ONE);
//        dataMapTest.put(TOTAL_ELEMENTS,ONE);
//        dataMapTest.put(PAGE,ZERO);
//        dataMapTest.put(SIZE,PAGE_SIZE);
//        dataMapTest.put(NUMBER_OF_ELEMENTS,ONE);
//        responseMapTest.put(DATA, dataMapTest);
//        responseMapTest.put(SUCCESS,true);
//        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
//        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
//        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
//        Mockito.when(mockObjectMapper.convertValue(dataMapTest.get(CONTENT),ArrayList.class)).thenReturn(contentListTest);
//        Mockito.when(mockObjectMapper.convertValue(contentListTest.get(0), LinkedHashMap.class)).thenReturn(singleContentTest);
//        mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID, TEST_RELATIONS, EMPTY_STRING, CREATED_ON, DESCENDING);
//        verify(mockWebClientWrapper, times(1)).webclientRequest(any(), any(), any(), any());
//    }

//    @Test
//    void getAllFormDataByFormIdAndQEmptySortTest() throws JsonProcessingException
//    {
//        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        String responseTest =RESPONSE_VALUE_22;
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
//        dataMapTest.put(TOTAL_PAGES,ONE);
//        dataMapTest.put(TOTAL_ELEMENTS,ONE);
//        dataMapTest.put(PAGE,ZERO);
//        dataMapTest.put(SIZE,PAGE_SIZE);
//        dataMapTest.put(NUMBER_OF_ELEMENTS,ONE);
//        responseMapTest.put(DATA, dataMapTest);
//        responseMapTest.put(SUCCESS,true);
//        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
//        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
//        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
//        Mockito.when(mockObjectMapper.convertValue(dataMapTest.get(CONTENT),ArrayList.class)).thenReturn(contentListTest);
//        Mockito.when(mockObjectMapper.convertValue(contentListTest.get(0), LinkedHashMap.class)).thenReturn(singleContentTest);
//        mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID, TEST_RELATIONS, TEST_ID, null, null);
//        verify(mockWebClientWrapper, times(1)).webclientRequest(any(), any(), any(), any());
//    }

//    @Test
//    void getAllFormDataByFormIdAndQSortPresentTest() throws JsonProcessingException
//    {
//        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        String responseTest =RESPONSE_VALUE_23;
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
//        dataMapTest.put(TOTAL_PAGES,ONE);
//        dataMapTest.put(TOTAL_ELEMENTS,ONE);
//        dataMapTest.put(PAGE,ZERO);
//        dataMapTest.put(SIZE,PAGE_SIZE);
//        dataMapTest.put(NUMBER_OF_ELEMENTS,ONE);
//        responseMapTest.put(DATA, dataMapTest);
//        responseMapTest.put(SUCCESS,true);
//        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
//        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
//        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
//        Mockito.when(mockObjectMapper.convertValue(dataMapTest.get(CONTENT),ArrayList.class)).thenReturn(contentListTest);
//        Mockito.when(mockObjectMapper.convertValue(contentListTest.get(0), LinkedHashMap.class)).thenReturn(singleContentTest);
//        mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID, TEST_RELATIONS, TEST_ID, CREATED_ON, DESCENDING);
//        verify(mockWebClientWrapper, times(1)).webclientRequest(any(), any(), any(), any());
//    }

//    @Test
//    void getAllFormDataByFormIdAndQPaginationEmptyQSortTest() throws JsonProcessingException
//    {
//        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        String responseTest=RESPONSE_VALUE_24;
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
//        dataMapTest.put(TOTAL_PAGES,ONE);
//        dataMapTest.put(TOTAL_ELEMENTS,ONE);
//        dataMapTest.put(PAGE,ZERO);
//        dataMapTest.put(SIZE,PAGE_SIZE);
//        dataMapTest.put(NUMBER_OF_ELEMENTS,ONE);
//        responseMapTest.put(DATA, dataMapTest);
//        responseMapTest.put(SUCCESS,true);
//        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
//        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
//        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
//        Mockito.when(mockObjectMapper.convertValue(dataMapTest.get(CONTENT),ArrayList.class)).thenReturn(contentListTest);
//        Mockito.when(mockObjectMapper.convertValue(contentListTest.get(0), LinkedHashMap.class)).thenReturn(singleContentTest);
//        mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID, TEST_RELATIONS, EMPTY_STRING, null, null, PageRequest.of(0, 5));
//        verify(mockWebClientWrapper, times(1)).webclientRequest(any(), any(), any(), any());
//    }

//    @Test
//    void getAllFormDataByFormIdAndEmptyQWithSortPaginationTest() throws JsonProcessingException
//    {
//        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        String responseTest =RESPONSE_VALUE_25;
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
//        dataMapTest.put(TOTAL_PAGES,ONE);
//        dataMapTest.put(TOTAL_ELEMENTS,ONE);
//        dataMapTest.put(PAGE,ZERO);
//        dataMapTest.put(SIZE,PAGE_SIZE);
//        dataMapTest.put(NUMBER_OF_ELEMENTS,ONE);
//        responseMapTest.put(DATA, dataMapTest);
//        responseMapTest.put(SUCCESS,true);
//        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
//        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
//        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
//        Mockito.when(mockObjectMapper.convertValue(dataMapTest.get(CONTENT),ArrayList.class)).thenReturn(contentListTest);
//        Mockito.when(mockObjectMapper.convertValue(contentListTest.get(0), LinkedHashMap.class)).thenReturn(singleContentTest);
//        mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID, TEST_RELATIONS, EMPTY_STRING, CREATED_ON, DESCENDING, PageRequest.of(0, 5));
//        verify(mockWebClientWrapper, times(1)).webclientRequest(any(), any(), any(), any());
//    }

//    @Test
//    void getAllFormDataByFormIdAndQPaginationEmptySortTest() throws JsonProcessingException
//    {
//        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        String responseTest =RESPONSE_VALUE_26;
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
//        dataMapTest.put(TOTAL_PAGES,ONE);
//        dataMapTest.put(TOTAL_ELEMENTS,ONE);
//        dataMapTest.put(PAGE,ZERO);
//        dataMapTest.put(SIZE,PAGE_SIZE);
//        dataMapTest.put(NUMBER_OF_ELEMENTS,ONE);
//        responseMapTest.put(DATA, dataMapTest);
//        responseMapTest.put(SUCCESS,true);
//        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
//        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
//        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
//        Mockito.when(mockObjectMapper.convertValue(dataMapTest.get(CONTENT),ArrayList.class)).thenReturn(contentListTest);
//        Mockito.when(mockObjectMapper.convertValue(contentListTest.get(0), LinkedHashMap.class)).thenReturn(singleContentTest);
//        mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID, TEST_RELATIONS, TEST_ID, null, null, PageRequest.of(0, 5));
//        verify(mockWebClientWrapper, times(1)).webclientRequest(any(), any(), any(), any());
//    }

//    @Test
//    void getAllFormDataByFormIdAndQPaginationNotEmptyTest() throws JsonProcessingException
//    {
//        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        String responseTest =RESPONSE_VALUE_27;
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
//        dataMapTest.put(TOTAL_PAGES,ONE);
//        dataMapTest.put(TOTAL_ELEMENTS,ONE);
//        dataMapTest.put(PAGE,ZERO);
//        dataMapTest.put(SIZE,PAGE_SIZE);
//        dataMapTest.put(NUMBER_OF_ELEMENTS,ONE);
//        responseMapTest.put(DATA, dataMapTest);
//        responseMapTest.put(SUCCESS,true);
//        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
//        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
//        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
//        Mockito.when(mockObjectMapper.convertValue(dataMapTest.get(CONTENT),ArrayList.class)).thenReturn(contentListTest);
//        Mockito.when(mockObjectMapper.convertValue(contentListTest.get(0), LinkedHashMap.class)).thenReturn(singleContentTest);
//        mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID, TEST_RELATIONS, TEST_ID, CREATED_ON, DESCENDING, PageRequest.of(0, 5));
//        verify(mockWebClientWrapper, times(1)).webclientRequest(any(), any(), any(), any());
//    }

//    @Test
//    void getAllFormDataByFormIdTest() throws JsonProcessingException
//    {
//        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        String responseTest =RESPONSE_VALUE_28;
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
//        dataMapTest.put(TOTAL_PAGES,ONE);
//        dataMapTest.put(TOTAL_ELEMENTS,ONE);
//        dataMapTest.put(PAGE,ZERO);
//        dataMapTest.put(SIZE,PAGE_SIZE);
//        dataMapTest.put(NUMBER_OF_ELEMENTS,ONE);
//        responseMapTest.put(DATA, dataMapTest);
//        responseMapTest.put(SUCCESS,true);
//        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
//        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
//        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
//        Mockito.when(mockObjectMapper.convertValue(dataMapTest.get(CONTENT),ArrayList.class)).thenReturn(contentListTest);
//        Mockito.when(mockObjectMapper.convertValue(contentListTest.get(0), LinkedHashMap.class)).thenReturn(singleContentTest);
//        mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID, TEST_RELATIONS);
//        verify(mockWebClientWrapper, times(1)).webclientRequest(any(), any(), any(), any());
//    }

//    @Test
//    void getAllFormDataByFormIdAndIdTest() throws JsonProcessingException
//    {
//        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        String responseTest =RESPONSE_VALUE_29;
//        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(GET),any())).thenReturn(responseTest);
//        LinkedHashMap<String, Object> testFormMetaData = new LinkedHashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        LinkedHashMap<String, Object> testFormData = new LinkedHashMap<>();
//        testFormData.put(NAME, NAME_VALUE);
//        testFormData.put(AGE,AGE_VALUE);
//        LinkedHashMap<String,Object> responseMapTest =new LinkedHashMap<>();
//        LinkedHashMap<String,Object> dataMapTest =new LinkedHashMap<>();
//        dataMapTest.put(FORM_ID,TEST_FORM_ID);
//        dataMapTest.put(ID,TEST_ID);
//        dataMapTest.put(VERSION,TEST_VERSION);
//        dataMapTest.put(FORM_DATA,testFormData);
//        dataMapTest.put(FORM_META_DATA,testFormMetaData);
//        dataMapTest.put(CREATED_BY_NAME,CREATED_BY_USER_NAME);
//        dataMapTest.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
//        dataMapTest.put(CREATED_BY_ID,BIGINTEGER_ID);
//        dataMapTest.put(UPDATED_BY_ID,BIGINTEGER_ID);
//        dataMapTest.put(CREATED_ON,TEST_CREATED_ON);
//        dataMapTest.put(UPDATED_ON,TEST_UPDATED_ON);
//        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
//        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA), Map.class)).thenReturn(dataMapTest);
//        mockFormDataServiceImpl.getFormDataByFormIdAndId(TEST_FORM_ID, TEST_ID, TEST_RELATIONS);
//        verify(mockWebClientWrapper, times(1)).webclientRequest(any(), any(), any(), any());
//    }

    @Test
    void deleteAllFormDataByFormIdTest() throws JsonProcessingException
    {
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
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_+TEST_FORM_ID)).thenReturn(true);
        mockFormDataServiceImpl.deleteAllFormDataByFormId(TEST_FORM_ID);
        verify(mockWebClientWrapper,times(1)).webclientRequest(any(),any(),any(),any());
    }

//    @Test
//    void deleteFormDataByFormIdAndIdTest() throws JsonProcessingException
//    {
//        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
//        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
//        String responseTest =RESPONSE_VALUE_31;
//        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),any(),any())).thenReturn(responseTest);
//        Map<String, Object> testFormMetaData = new HashMap<>();
//        testFormMetaData.put(FORM_VERSION, 1);
//        Map<String, Object> testFormData = new HashMap<>();
//        testFormData.put(NAME, NAME_VALUE);
//        testFormData.put(AGE,AGE_VALUE);
//        Map<String,Object> responseMapTest =new HashMap<>();
//        Map<String,Object> dataMapTest =new HashMap<>();
//        dataMapTest.put(FORM_ID,TEST_FORM_ID);
//        dataMapTest.put(ID,TEST_ID);
//        dataMapTest.put(VERSION,TEST_VERSION);
//        dataMapTest.put(FORM_DATA,testFormData);
//        dataMapTest.put(FORM_META_DATA,testFormMetaData);
//        dataMapTest.put(CREATED_BY_NAME,CREATED_BY_USER_NAME);
//        dataMapTest.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
//        dataMapTest.put(CREATED_BY_ID,BIGINTEGER_ID);
//        dataMapTest.put(UPDATED_BY_ID,BIGINTEGER_ID);
//        dataMapTest.put(CREATED_ON,TEST_CREATED_ON);
//        dataMapTest.put(UPDATED_ON,TEST_UPDATED_ON);
//        responseMapTest.put(DATA, dataMapTest);
//        responseMapTest.put(SUCCESS,true);
//        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
//        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
//        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_+TEST_FORM_ID)).thenReturn(true);
//        Mockito.when(mockMongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_+TEST_FORM_ID)).thenReturn(mockMongoCollection);
//        DeleteResult mockDeleteResult=Mockito.mock(DeleteResult.class);
//        Mockito.when(mockMongoCollection.deleteMany(any())).thenReturn(mockDeleteResult);
//        Mockito.when(mockDeleteResult.getDeletedCount()).thenReturn(Long.valueOf(1));
//        mockFormDataServiceImpl.deleteFormDataByFormIdAndId(TEST_FORM_ID,TEST_ID);
//        verify(mockWebClientWrapper,times(1)).webclientRequest(any(),any(),any(),any());
//    }

    @Test
    void aggregateByFormIdAndFilterGroupByTest()
    {
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ + TEST_FORM_ID)).thenReturn(true);
        List<AggregationOperation> aggregationOperationsList = new ArrayList<>();
        aggregationOperationsList.add(mockAggregationOperation);
        Map testAggregationMap=new HashMap<>();
        testAggregationMap.put(UNDERSCORE_ID,TEST_ID);
        testAggregationMap.put(TEST_OPERATION,TEST_COUNT_VALUE);
        List<Map> testAggregationList=new ArrayList<>();
        testAggregationList.add(testAggregationMap);
        List<Map> mappedResults=new ArrayList();
        mappedResults.add(testAggregationMap);
        Document document=new Document();
        document.append("result",mappedResults);
        AggregationResults<Map> aggregationResults=new AggregationResults<>(testAggregationList,document);
        Mockito.when(mockMongoTemplate.aggregate(any(Aggregation.class), (String) any(),eq(Map.class))).thenReturn(aggregationResults);
        mockFormDataServiceImpl.aggregateByFormIdFilterGroupBy(TEST_FORM_ID,TEST_FILTER,TEST_GROUP_BY,TEST_OPERATION);
        verify(mockMongoTemplate,times(1)).aggregate(any(Aggregation.class), (String) any(),eq(Map.class));
    }

    @Test
    void aggregateByFormIdAndWithoutFilterGroupByTest()
    {
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ + TEST_FORM_ID)).thenReturn(true);
        List<AggregationOperation> aggregationOperationsList = new ArrayList<>();
        aggregationOperationsList.add(mockAggregationOperation);
        Map testAggregationMap=new HashMap<>();
        testAggregationMap.put(UNDERSCORE_ID,TEST_ID);
        testAggregationMap.put(TEST_OPERATION,TEST_COUNT_VALUE);
        List<Map> testAggregationList=new ArrayList<>();
        testAggregationList.add(testAggregationMap);
        List<Map> mappedResults=new ArrayList();
        mappedResults.add(testAggregationMap);
        Document document=new Document();
        document.append("result",mappedResults);
        AggregationResults<Map> aggregationResults=new AggregationResults<>(testAggregationList,document);
        Mockito.when(mockMongoTemplate.aggregate(any(Aggregation.class), (String) any(),eq(Map.class))).thenReturn(aggregationResults);
        mockFormDataServiceImpl.aggregateByFormIdFilterGroupBy(TEST_FORM_ID, EMPTY_STRING,TEST_GROUP_BY,TEST_OPERATION);
        verify(mockMongoTemplate,times(1)).aggregate(any(Aggregation.class), (String) any(),eq(Map.class));
    }
}





