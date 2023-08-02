package com.techsophy.tsf.runtime.form.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techsophy.idgenerator.IdGeneratorImpl;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.FormDataAuditResponse;
import com.techsophy.tsf.runtime.form.dto.FormDataSchema;
import com.techsophy.tsf.runtime.form.entity.FormDataDefinition;
import com.techsophy.tsf.runtime.form.exception.InvalidInputException;
import com.techsophy.tsf.runtime.form.service.FormService;
import com.techsophy.tsf.runtime.form.service.MongoQueryBuilder;
import com.techsophy.tsf.runtime.form.service.impl.FormDataServiceImpl;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import com.techsophy.tsf.runtime.form.utils.UserDetails;
import com.techsophy.tsf.runtime.form.utils.WebClientWrapper;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import java.math.BigInteger;
import java.util.*;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class FormDataServiceImplTest {
    @Mock
    UserDetails userDetails;
    @Mock
    TokenUtils tokenUtils;
    @Mock
    WebClientWrapper webClientWrapper;
    @Mock
    FormService formService;
    @Mock
    IdGeneratorImpl idGenerator;
    @Mock
    GlobalMessageSource globalMessageSource;
    @Mock
    MongoTemplate mongoTemplate;
    @Mock
    Document document;
    @Mock
    ObjectMapper objectMapper;
    @Mock
    FormDataAuditServiceImpl formDataAuditService;
    @Mock
    FormValidationServiceImpl formValidationServiceImpl;
    @Mock
    MongoQueryBuilder queryBuilder;
    @InjectMocks
    FormDataServiceImpl formDataService;
    List<Map<String, Object>> userList = new ArrayList<>();
    Map<String, Object> map = new HashMap<>();

    @BeforeEach
    public void init() {
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
    void updateFormDataTestWhileIdIsEmpty() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put(STRING, STRING);
        FormDataSchema formDataSchema = new FormDataSchema(TEST_ID, TEST_FORM_ID, TEST_VERSION, TEST_FORM_DATA, TEST_FORM_META_DATA);
        Assertions.assertThrows(InvalidInputException.class, () -> formDataService.updateFormData(formDataSchema, "formData.name:akhil", null, null));
    }

    @Test
    void updateFormDataTestWhileFormIdIsEmpty() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put(STRING, STRING);
        FormDataSchema formDataSchema = new FormDataSchema(TEST_ID, TEST_FORM_ID, TEST_VERSION, TEST_FORM_DATA, TEST_FORM_META_DATA);
        Assertions.assertThrows(InvalidInputException.class, () -> formDataService.updateFormData(formDataSchema, "formData.name:akhil", null, null));
    }

    @Test
    void updateFormDataTestToSaveToAuditException() throws JsonProcessingException {
        // Create a FormDataServiceImpl instance with the necessary dependencies
        FormDataServiceImpl formDataService = new FormDataServiceImpl(
                0, "gateway", false, mongoTemplate, globalMessageSource, idGenerator,
                webClientWrapper, tokenUtils, objectMapper, formDataAuditService,
                formService, formValidationServiceImpl, userDetails, queryBuilder
        );
        // Create test data for form metadata and form data
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put("formVersion", 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put("name", "John Doe");
        testFormData.put("age", 30);
        // Create FormDataSchema instance with the test data
        FormDataSchema formDataSchema = new FormDataSchema(
                "formDataTestId", "formId", 1, testFormData, testFormMetaData
        );
        // Create a sample FormDataDefinition to be returned by the mongoTemplate.findOne()
        FormDataDefinition formDataDefinition = new FormDataDefinition();
        formDataDefinition.setId("formDataTestId");
        formDataDefinition.setFormId("formId");
        formDataDefinition.setVersion(1);
        formDataDefinition.setFormData(testFormData);
        formDataDefinition.setFormMetaData(testFormMetaData);
        // Set up the behavior of mocked components
        when(mongoTemplate.findOne(any(), any(), any())).thenReturn(formDataDefinition);
        when(formDataAuditService.saveFormDataAudit(any())).thenThrow(JsonProcessingException.class);
        // Assertions
        Assertions.assertThrows(
                InvalidInputException.class,
                () -> formDataService.updateFormData(formDataSchema, "formData.name:akhil", null, null),
                "Expected updateFormData() to throw an InvalidInputException when formDataAuditService.saveFormDataAudit() throws JsonProcessingException."
        );
    }

    @Test
    void updateFormDataTestToSaveToAudit() throws JsonProcessingException {
        FormDataSchema formDataSchema = new FormDataSchema(TEST_ID, TEST_FORM_ID, TEST_VERSION, map, TEST_FORM_META_DATA);
        FormDataDefinition formDataDefinition = new FormDataDefinition();
        formDataDefinition.setId(TEST_ID);
        formDataDefinition.setFormId(TEST_FORM_ID);
        formDataDefinition.setVersion(TEST_VERSION);
        formDataDefinition.setFormData(map);
        formDataDefinition.setFormMetaData(TEST_FORM_META_DATA);
        // Mock the dependencies and method calls
        when(mongoTemplate.findOne(any(), any(), any())).thenReturn(formDataDefinition);
        doReturn(BigInteger.ONE).when(idGenerator).nextId();
        FormDataAuditResponse formDataAuditResponse = new FormDataAuditResponse(TEST_ID, TEST_VERSION);
        doReturn(formDataAuditResponse).when(formDataAuditService).saveFormDataAudit(any());
        // Call the method to test
        FormDataDefinition updatedFormData = formDataService.updateFormData(formDataSchema, "formData.name:akhil", null, null);
        // Assertions
        assertNotNull(updatedFormData);
        assertEquals(TEST_VERSION + 1, updatedFormData.getVersion());
        // Verify method invocations
        verify(mongoTemplate, times(1)).findOne(any(), any(), any());
        verify(formDataAuditService, times(1)).saveFormDataAudit(any());
    }

    @Test
    void updateFormDataTestToSaveToAuditFailure() throws JsonProcessingException {
        // Create a FormDataServiceImpl instance with the necessary dependencies
        FormDataServiceImpl formDataService = new FormDataServiceImpl(
                0, "gateway", false, mongoTemplate, globalMessageSource, idGenerator,
                webClientWrapper, tokenUtils, objectMapper, formDataAuditService,
                formService, formValidationServiceImpl, userDetails, queryBuilder
        );
        // Create test data for form metadata and form data
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put("formVersion", 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put("name", "John Doe");
        testFormData.put("age", 30);
        FormDataSchema formDataSchema = new FormDataSchema(
                "formDataTestId", "formId", 1, testFormData, testFormMetaData
        );
        // Create a sample FormDataDefinition to be returned by the mongoTemplate.findOne()
        FormDataDefinition formDataDefinition = new FormDataDefinition();
        formDataDefinition.setId("formDataTestId");
        formDataDefinition.setFormId("formId");
        formDataDefinition.setVersion(1);
        formDataDefinition.setFormData(testFormData);
        formDataDefinition.setFormMetaData(testFormMetaData);
        // Set up the behavior of mocked components
        when(mongoTemplate.findOne(any(), any(), any())).thenReturn(formDataDefinition);
        // Create a FormDataAuditResponse instance with a null version
        FormDataAuditResponse formDataAuditResponse = new FormDataAuditResponse(formDataDefinition.getId(), null);
        when(formDataAuditService.saveFormDataAudit(any())).thenReturn(formDataAuditResponse);
        // Assertions
        Assertions.assertThrows(
                InvalidInputException.class,
                () -> formDataService.updateFormData(formDataSchema, "formData.name:akhil", null, null),
                "Expected updateFormData() to throw an InvalidInputException when FormDataAuditResponse version is null."
        );
    }
}
