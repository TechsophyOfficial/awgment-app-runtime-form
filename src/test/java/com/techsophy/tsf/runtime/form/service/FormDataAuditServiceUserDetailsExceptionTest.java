package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.InsertOneResult;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.FormDataAuditSchema;
import com.techsophy.tsf.runtime.form.exception.UserDetailsIdNotFoundException;
import com.techsophy.tsf.runtime.form.service.impl.FormDataAuditServiceImpl;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import com.techsophy.tsf.runtime.form.utils.UserDetails;
import com.techsophy.tsf.runtime.form.utils.WebClientWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith({MockitoExtension.class})
class FormDataAuditServiceUserDetailsExceptionTest
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
        map.put(ID,EMPTY_STRING);
        map.put(USER_NAME, USER_FIRST_NAME);
        map.put(FIRST_NAME, USER_LAST_NAME);
        map.put(LAST_NAME, USER_FIRST_NAME);
        map.put(MOBILE_NUMBER, NUMBER);
        map.put(EMAIL_ID, MAIL_ID);
        map.put(DEPARTMENT, NULL);
        userList.add(map);
    }

    @Test
    void saveFormDataUserDetailsExceptionTest() throws IOException
    {
        Map<String,Object> testFormData=new HashMap<>();
        testFormData.put(NAME,NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        Map<String,Object> testFormMetaData=new HashMap<>();
        testFormMetaData.put(FORM_VERSION,1);
        FormDataAuditSchema formDataAuditSchemaTest = new FormDataAuditSchema(TEST_ID,TEST_FORM_DATA_ID,TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
        doReturn(userList).when(mockUserDetails).getUserDetails();
        Assertions.assertThrows(UserDetailsIdNotFoundException.class, () ->
                mockFormDataAuditServiceImpl.saveFormDataAudit(formDataAuditSchemaTest));
    }
}
