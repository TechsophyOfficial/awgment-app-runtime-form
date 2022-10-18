package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.techsophy.idgenerator.IdGeneratorImpl;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.FormDataSchema;
import com.techsophy.tsf.runtime.form.entity.FormDataDefinition;
import com.techsophy.tsf.runtime.form.exception.UserDetailsIdNotFoundException;
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
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.io.IOException;
import java.util.*;


import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static org.mockito.Mockito.doReturn;

@ActiveProfiles(TEST_ACTIVE_PROFILE)
@SpringBootTest
@ExtendWith({SpringExtension.class})
class FormDataServiceUserDetailsExceptionTest
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
    @InjectMocks
    FormDataServiceImpl mockFormDataServiceImpl;

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
        LinkedHashMap<String,Object> testFormData=new LinkedHashMap<>();
        testFormData.put(NAME,NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        Map<String,Object> testFormMetaData=new HashMap<>();
        testFormMetaData.put(FORM_VERSION,1);
        FormDataSchema formDataSchemaTest=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,testFormData,testFormMetaData);
        doReturn(userList).when(mockUserDetails).getUserDetails();
        Assertions.assertThrows(UserDetailsIdNotFoundException.class, () ->
                mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
    }
}
