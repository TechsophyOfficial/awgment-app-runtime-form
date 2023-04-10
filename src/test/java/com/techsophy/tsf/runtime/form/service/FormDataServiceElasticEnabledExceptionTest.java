package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.techsophy.idgenerator.IdGeneratorImpl;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.FormResponseSchema;
import com.techsophy.tsf.runtime.form.exception.InvalidInputException;
import com.techsophy.tsf.runtime.form.service.impl.FormDataAuditServiceImpl;
import com.techsophy.tsf.runtime.form.service.impl.FormDataServiceImpl;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import com.techsophy.tsf.runtime.form.utils.UserDetails;
import com.techsophy.tsf.runtime.form.utils.WebClientWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.DATA;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.ELASTIC_SOURCE;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.FORM_DATA;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
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
    FormService mockFormService;
    @Mock
    MongoTemplate mockMongoTemplate;
    @Mock
    MongoCollection mockMongoCollection;
    @Mock
    InsertOneResult insertOneResult;
    @InjectMocks
    FormDataServiceImpl mockFormDataServiceImpl;

    List<Map<String, Object>> userList = new ArrayList<>();

    @BeforeEach
    public void init()
    {
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
        when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
        Assertions.assertThrows(InvalidInputException.class,()->mockFormDataServiceImpl.deleteAllFormDataByFormId(TEST_FORM_ID));
    }

    @Test
    void deleteFormDataByFormIdAndIdTest()
    {
        when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
        when(mockMongoTemplate.getCollection(TP_RUNTIME_FORM_DATA +TEST_FORM_ID)).thenReturn(mockMongoCollection);
        DeleteResult mockDeleteResult=Mockito.mock(DeleteResult.class);
        when(mockMongoCollection.deleteMany(any())).thenReturn(mockDeleteResult);
        when(mockDeleteResult.getDeletedCount()).thenReturn(Long.valueOf(1));
        Assertions.assertThrows(InvalidInputException.class,()->mockFormDataServiceImpl.deleteFormDataByFormIdAndId(TEST_FORM_ID,TEST_ID));
    }

    @Test
    void deleteFormDataByFormIdAndIdInvalidInputExceptionTest()
    {
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
        when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
        when(mockMongoTemplate.getCollection(TP_RUNTIME_FORM_DATA +TEST_FORM_ID)).thenReturn(mockMongoCollection);
        DeleteResult mockDeleteResult=Mockito.mock(DeleteResult.class);
        when(mockMongoCollection.deleteMany(any())).thenReturn(mockDeleteResult);
        when(mockDeleteResult.getDeletedCount()).thenReturn(Long.valueOf(1));
        Assertions.assertThrows(InvalidInputException.class,()->mockFormDataServiceImpl.deleteFormDataByFormIdAndId(TEST_FORM_ID,TEST_ID));
    }

    @Test
    void deleteFormDataByFormIdAndIdTokenInvalidInputExceptionTest()
    {
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
        when(mockMongoTemplate.getCollection(TP_RUNTIME_FORM_DATA +TEST_FORM_ID)).thenReturn(mockMongoCollection);
        DeleteResult mockDeleteResult= Mockito.mock(DeleteResult.class);
        when(mockMongoCollection.deleteMany(any())).thenReturn(mockDeleteResult);
        when(mockDeleteResult.getDeletedCount()).thenReturn(Long.valueOf(1));
        Assertions.assertThrows(InvalidInputException.class,()->mockFormDataServiceImpl.deleteFormDataByFormIdAndId(TEST_FORM_ID,TEST_ID));
    }
}
