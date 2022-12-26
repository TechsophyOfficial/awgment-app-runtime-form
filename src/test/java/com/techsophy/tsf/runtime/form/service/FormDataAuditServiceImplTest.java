package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.FormDataAuditSchema;
import com.techsophy.tsf.runtime.form.exception.InvalidInputException;
import com.techsophy.tsf.runtime.form.exception.UserDetailsIdNotFoundException;
import com.techsophy.tsf.runtime.form.service.impl.FormDataAuditServiceImpl;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.*;

import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.FORM_DATA;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles(TEST_ACTIVE_PROFILE)
@ExtendWith({SpringExtension.class})
class FormDataAuditServiceImplTest
{
    @Mock
    UserDetails mockUserDetails;
    @Mock
    TokenUtils mockTokenUtils;
    @Mock
    MessageSource mockMessageSource;
    @Mock
    GlobalMessageSource mockGlobalMessageSource;
    @Mock
    ObjectMapper mockObjectMapper;
    @Mock
    WebClientWrapper mockWebClientWrapper;
    @Mock
    WebClient mockWebClient;
    @Mock
    MongoTemplate mockMongoTemplate;
    @Mock
    InsertOneResult insertOneResult;
    @Mock
    MongoCollection mockMongoCollection;
    @InjectMocks
    FormDataAuditServiceImpl mockFormDataAuditServiceImpl;

    List<Map<String, Object>> userList = new ArrayList<>();
    Map<String, Object> map = new HashMap<>();

    @BeforeEach
    public void init()
    {
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
    void saveFormDataCollectionExistsTest() throws JsonProcessingException
    {
        doReturn(userList).when(mockUserDetails).getUserDetails();
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        FormDataAuditSchema formDataAuditSchemaTest = new FormDataAuditSchema(TEST_ID,TEST_FORM_DATA_ID,TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
        Mockito.when(mockMongoTemplate.collectionExists( TP_RUNTIME_FORM_DATA_+formDataAuditSchemaTest.getFormId()+AUDIT)).thenReturn(true);
        Mockito.when(mockMongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_ +formDataAuditSchemaTest.getFormId()+AUDIT)).thenReturn(mockMongoCollection);
        Map<String, Object> formDataMap = new HashMap<>();
        formDataMap.put(UNDERSCORE_ID,Long.parseLong(UNDERSCORE_ID_VALUE));
        formDataMap.put(FORM_DATA_ID,TEST_FORM_DATA_ID);
        formDataMap.put(FORM_ID,TEST_FORM_ID);
        formDataMap.put(VERSION, String.valueOf(1));
        formDataMap.put(FORM_META_DATA, formDataAuditSchemaTest.getFormMetadata());
        formDataMap.put(FORM_DATA, formDataAuditSchemaTest.getFormData());
        formDataMap.put(CREATED_ON, Instant.now());
        formDataMap.put(CREATED_BY_ID,CREATED_BY_USER_ID);
        formDataMap.put(CREATED_BY_NAME, CREATED_BY_USER_NAME);
        formDataMap.put(UPDATED_ON,TEST_UPDATED_ON);
        formDataMap.put(UPDATED_BY_ID,UPDATED_BY_USER_ID);
        formDataMap.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        Document document = new Document(formDataMap);
        Mockito.when(mockMongoTemplate.save(any(),any())).thenReturn(document);
        mockFormDataAuditServiceImpl.saveFormDataAudit(formDataAuditSchemaTest);
        verify(mockMongoTemplate,times(1)).save(any(),any());
    }

    @Test
    void saveFormDataCollectionExistsTestWhileUserIdIsEmpty() throws JsonProcessingException
    {
        doReturn(userList).when(mockUserDetails).getUserDetails();
        Map<String, Object> map = new HashMap<>();
        map.put("id","");
        Mockito.when(mockUserDetails.getUserDetails()).thenReturn(List.of(map));
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        FormDataAuditSchema formDataAuditSchemaTest = new FormDataAuditSchema(TEST_ID,TEST_FORM_DATA_ID,TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
        Mockito.when(mockMongoTemplate.collectionExists( TP_RUNTIME_FORM_DATA_+formDataAuditSchemaTest.getFormId()+AUDIT)).thenReturn(true);
        Assertions.assertThrows(UserDetailsIdNotFoundException.class, () -> mockFormDataAuditServiceImpl.saveFormDataAudit(formDataAuditSchemaTest));
    }

    @Test
    void saveFormDataCollectionExistsTestWhileFormIdIsEmpty() throws JsonProcessingException
    {
        doReturn(userList).when(mockUserDetails).getUserDetails();
        Mockito.when(mockUserDetails.getUserDetails()).thenReturn(List.of(map));
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        FormDataAuditSchema formDataAuditSchemaTest = new FormDataAuditSchema(TEST_ID,TEST_FORM_DATA_ID,"", TEST_VERSION, testFormData, testFormMetaData);
        Mockito.when(mockMongoTemplate.collectionExists( TP_RUNTIME_FORM_DATA_+formDataAuditSchemaTest.getFormId()+AUDIT)).thenReturn(true);
        Assertions.assertThrows(InvalidInputException.class, () -> mockFormDataAuditServiceImpl.saveFormDataAudit(formDataAuditSchemaTest));
    }

    @Test
    void saveFormDataCollectionUpdateTest() throws JsonProcessingException
    {
        doReturn(userList).when(mockUserDetails).getUserDetails();
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        FormDataAuditSchema formDataAuditSchemaTest = new FormDataAuditSchema(TEST_ID,TEST_FORM_DATA_ID,TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ +formDataAuditSchemaTest.getFormId()+AUDIT)).thenReturn(false);
        Map<String, Object> formDataMap = new HashMap<>();
        formDataMap.put(UNDERSCORE_ID,Long.parseLong(UNDERSCORE_ID_VALUE));
        formDataMap.put(FORM_DATA_ID,TEST_FORM_DATA_ID);
        formDataMap.put(FORM_ID,TEST_FORM_ID);
        formDataMap.put(VERSION, String.valueOf(1));
        formDataMap.put(FORM_META_DATA, formDataAuditSchemaTest.getFormMetadata());
        formDataMap.put(FORM_DATA, formDataAuditSchemaTest.getFormData());
        formDataMap.put(CREATED_ON, Instant.now());
        formDataMap.put(CREATED_BY_ID,CREATED_BY_USER_ID);
        formDataMap.put(CREATED_BY_NAME, CREATED_BY_USER_NAME);
        formDataMap.put(UPDATED_ON,TEST_UPDATED_ON);
        formDataMap.put(UPDATED_BY_ID,UPDATED_BY_USER_ID);
        formDataMap.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        Document document = new Document(formDataMap);
        Mockito.when(mockMongoTemplate.save(any(),any())).thenReturn(document);
        mockFormDataAuditServiceImpl.saveFormDataAudit(formDataAuditSchemaTest);
        verify(mockMongoTemplate,times(1)).save(any(),any());
    }

    @Test
    void getAllFormDataByFormIdDocumentIdTest()
    {
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        FormDataAuditSchema formDataAuditSchemaTest = new FormDataAuditSchema(TEST_ID,TEST_FORM_DATA_ID,TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
        Map<String, Object> formDataMap = new HashMap<>();
        formDataMap.put(UNDERSCORE_ID,Long.parseLong(UNDERSCORE_ID_VALUE));
        formDataMap.put(FORM_DATA_ID,TEST_FORM_DATA_ID);
        formDataMap.put(FORM_ID,TEST_FORM_ID);
        formDataMap.put(VERSION, String.valueOf(1));
        formDataMap.put(FORM_META_DATA, formDataAuditSchemaTest.getFormMetadata());
        formDataMap.put(FORM_DATA, formDataAuditSchemaTest.getFormData());
        Date date=new Date();
        formDataMap.put(CREATED_ON, date);
        formDataMap.put(CREATED_BY_ID,CREATED_BY_USER_ID);
        formDataMap.put(CREATED_BY_NAME, CREATED_BY_USER_NAME);
        formDataMap.put(UPDATED_ON,date);
        formDataMap.put(UPDATED_BY_ID,UPDATED_BY_USER_ID);
        formDataMap.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        Document document = new Document(formDataMap);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ +formDataAuditSchemaTest.getFormId()+AUDIT)).thenReturn(true);
        Mockito.when(mockMongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_+formDataAuditSchemaTest.getFormId()+AUDIT)).thenReturn(mockMongoCollection);
        FindIterable iterable = mock(FindIterable.class);
        MongoCursor cursor = mock(MongoCursor.class);
        Bson filter= Filters.eq(FORM_DATA_ID,Long.valueOf(TEST_FORM_DATA_ID_VALUE));
        Mockito.when(mockMongoCollection.find(filter)).thenReturn(iterable);
        Mockito.when(iterable.iterator()).thenReturn(cursor);
        Mockito.when(cursor.hasNext()).thenReturn(true).thenReturn(false);
        Mockito.when(cursor.next()).thenReturn(document);
        Mockito.when(mockObjectMapper.convertValue(document.get(FORM_DATA),Map.class)).thenReturn(testFormData);
        Mockito.when(mockObjectMapper.convertValue(document.get(FORM_META_DATA),Map.class)).thenReturn(testFormMetaData);
        mockFormDataAuditServiceImpl.getAllFormDataAuditByFormIdAndDocumentId(TEST_FORM_ID,TEST_ID);
        verify(mockMongoCollection,times(1)).find(any(Bson.class));
    }
}
