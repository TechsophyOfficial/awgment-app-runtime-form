package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.techsophy.idgenerator.IdGeneratorImpl;
import com.techsophy.tsf.commons.query.Filters;
import com.techsophy.tsf.commons.query.QueryBuilder;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.entity.FormDataDefinition;
import com.techsophy.tsf.runtime.form.exception.FormIdNotFoundException;
import com.techsophy.tsf.runtime.form.exception.InvalidInputException;
import com.techsophy.tsf.runtime.form.service.impl.FormDataAuditServiceImpl;
import com.techsophy.tsf.runtime.form.service.impl.FormDataServiceImpl;
import com.techsophy.tsf.runtime.form.service.impl.FormValidationServiceImpl;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import com.techsophy.tsf.runtime.form.utils.UserDetails;
import com.techsophy.tsf.runtime.form.utils.WebClientWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.web.reactive.function.client.WebClient;
import javax.swing.text.Document;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class FormDataServiceElasticDisabledExceptionTest
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
    FormDataDefinition mockFormDataDefinition;
    @Mock
    MongoCollection mockMongoCollection;
    @Mock
    WebClient mockWebClient;
    @Mock
    FormService mockFormService;
    @Mock
    MongoCollection<Document> mockDocument;
    @Mock
    DeleteResult mockDeleteResult;
    @Mock
    AggregationResults aggregationResults;
    @Mock
    FormValidationServiceImpl mockFormValidationServiceImpl;
    @Mock
    Filters filters;
    @Mock
    QueryBuilder queryBuilder;
    @InjectMocks
    FormDataServiceImpl mockFormDataServiceImpl;

    List<Map<String, Object>> userList = new ArrayList<>();
    List<Map<String,Object>> list=new ArrayList<>();

    @BeforeEach
    public void init()
    {
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
        Map<String,Object> mapData =new HashMap<>();
        mapData.put("create","true");
        list.add(mapData);
    }

    @Test
    void getAllFormDataByFormIdInvalidInputException()
    {
        String relations = "tp_runtime_form_data_994102731543871488:formData.orderId,tp_runtime_form_data_994122561634369536:formData.parcelId";
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData2 = new HashMap<>();
        testFormData2.put(NAME, NAME_VALUE);
        testFormData2.put(AGE, AGE_VALUE);
        Mockito.when(mockMongoTemplate.collectionExists(anyString())).thenReturn(false);
        Assertions.assertThrows(FormIdNotFoundException.class, () ->
                mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID, relations, null, null, null, null));
    }

    @Test
    void getAllFormDataByFormIdNotFoundExceptionTest() {
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData2 = new HashMap<>();
        testFormData2.put(NAME, NAME_VALUE);
        testFormData2.put(AGE, AGE_VALUE);
        Mockito.when(mockMongoTemplate.collectionExists(anyString())).thenReturn(false);
        Assertions.assertThrows(FormIdNotFoundException.class, () ->
                mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID, null, CREATED_ON, null, null, null));
    }

    @Test
    void getAllFormDataByFormIdNotFoundExceptionTest2()
    {
            Map<String, Object> testFormMetaData = new HashMap<>();
            testFormMetaData.put(FORM_VERSION, 1);
            Map<String, Object> testFormData2 = new HashMap<>();
            testFormData2.put(NAME, NAME_VALUE);
            testFormData2.put(AGE, AGE_VALUE);
            when(mockMongoTemplate.collectionExists(anyString())).thenReturn(false);
            Assertions.assertThrows(FormIdNotFoundException.class,()->mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID, TEST_RELATIONS, null, CREATED_ON, null, null));
    }

        @Test
    void getAllFormDataByFormIdAndQIllegalArgumentSortExceptionTest()
    {
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData2 = new HashMap<>();
        testFormData2.put(NAME, NAME_VALUE);
        testFormData2.put(AGE,AGE_VALUE);
        when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID,TEST_RELATIONS,SEARCH_STRING, CREATED_ON, null, null));
    }

    @Test
    void getAllFormDataByFormIdAndQFormIdNotFoundExceptionTest()
    {
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData2 = new HashMap<>();
        testFormData2.put(NAME, NAME_VALUE);
        testFormData2.put(AGE, AGE_VALUE);
        when(mockMongoTemplate.collectionExists(anyString())).thenReturn(false);
        Assertions.assertThrows(FormIdNotFoundException.class, () ->
                mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID, TEST_RELATIONS, EMPTY_STRING, null, null, null));
    }

    @Test
    void getAllFormDataByFormIdAndQFormIdNotFoundExceptionTest2() {
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData2 = new HashMap<>();
        testFormData2.put(NAME, NAME_VALUE);
        testFormData2.put(AGE, AGE_VALUE);
        when(mockMongoTemplate.collectionExists(anyString())).thenReturn(false);
        Assertions.assertThrows(FormIdNotFoundException.class, () ->
                mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID, TEST_RELATIONS, EMPTY_STRING, null, null, null));
    }

    @Test
    void getAllFormDataByFormIdAndQInvalidInputExceptionTest4()
    {
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData2 = new HashMap<>();
        testFormData2.put(NAME, NAME_VALUE);
        testFormData2.put(AGE,AGE_VALUE);
        when(mockMongoTemplate.collectionExists(anyString())).thenReturn(false);
        PageRequest pageRequest = PageRequest.of(1, 5);
        Assertions.assertThrows(RuntimeException.class, () ->
                mockFormDataServiceImpl.getAllFormDataByFormIdAndQ(TEST_FORM_ID, TEST_RELATIONS, EMPTY_STRING, CREATED_ON, null, pageRequest, null));
    }

    @Test
    void getAllFormDataByFormIdFormIdNotFoundExceptionTest()
    {
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData2 = new HashMap<>();
        testFormData2.put(NAME, NAME_VALUE);
        testFormData2.put(AGE, AGE_VALUE);
        when(mockMongoTemplate.collectionExists(anyString())).thenReturn(false);
        Assertions.assertThrows(FormIdNotFoundException.class, () ->
                mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID, TEST_RELATIONS, null));
    }

    @Test
    void deleteAllFormDataByFormIdNotFoundExceptionTest() {
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData2 = new HashMap<>();
        testFormData2.put(NAME, NAME_VALUE);
        testFormData2.put(AGE, AGE_VALUE);
        Assertions.assertThrows(InvalidInputException.class, () ->
                mockFormDataServiceImpl.deleteAllFormDataByFormId(TEST_FORM_ID));
    }

    @Test
    void deleteFormDataByFormIdAndIdFormIdNotFoundExceptionTest()
    {
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData2 = new HashMap<>();
        testFormData2.put(NAME, NAME_VALUE);
        testFormData2.put(AGE,AGE_VALUE);
        Assertions.assertThrows(FormIdNotFoundException.class, () ->
                mockFormDataServiceImpl.deleteFormDataByFormIdAndId(TEST_FORM_ID,TEST_FORM_DATA_ID, null, null));
    }

//    @Test
//    void getAllFormDataFilterJsonExceptionTest() throws IOException {
////        String FILTER_JSON="{\"operations\":{\"formData.orderId\":{\"lt\":23,\"lte\":20,\"gt\":20,\"gte\":24}";
//        Mockito.when(mockMongoTemplate.collectionExists(anyString())).thenReturn(true);
////        Mockito.when(mockObjectMapper.readValue(anyString(),ArgumentMatchers.eq(Filters.class))).thenReturn(filters);
//        Assertions.assertThrows(IllegalArgumentException.class,()->mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,TEST_RELATIONS,FILTER_INCORRECT_JSON,EMPTY_STRING, EMPTY_STRING, null));
//    }
}
