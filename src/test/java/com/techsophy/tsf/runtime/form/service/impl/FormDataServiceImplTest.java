package com.techsophy.tsf.runtime.form.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techsophy.idgenerator.IdGeneratorImpl;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.FormDataAuditResponse;
import com.techsophy.tsf.runtime.form.dto.FormDataAuditSchema;
import com.techsophy.tsf.runtime.form.dto.FormDataSchema;
import com.techsophy.tsf.runtime.form.entity.FormDataDefinition;
import com.techsophy.tsf.runtime.form.exception.InvalidInputException;
import com.techsophy.tsf.runtime.form.service.FormDataAuditService;
import com.techsophy.tsf.runtime.form.service.FormService;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import com.techsophy.tsf.runtime.form.utils.UserDetails;
import com.techsophy.tsf.runtime.form.utils.WebClientWrapper;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.techsophy.tsf.runtime.form.constants.ErrorConstants.FORMDATA_AUDIT_FAILED;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.EMPTY_STRING;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.NULL;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

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
  FormDataAuditService formDataAuditService;
  @InjectMocks
  FormDataServiceImpl formDataService;

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
  void updateFormDataTestToSaveToAudit() throws JsonProcessingException {
    Map<String, Object> testFormMetaData = new HashMap<>();
    testFormMetaData.put(FORM_VERSION, 1);
    Map<String, Object> testFormData = new HashMap<>();
    testFormData.put(NAME, NAME_VALUE);
    testFormData.put(AGE,AGE_VALUE);
    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
    map.put(STRING, STRING);
    FormDataAuditSchema formDataAuditSchema = new FormDataAuditSchema(TEST_ID,NULL,EMPTY_STRING, TEST_VERSION, testFormData, testFormMetaData);
    FormDataSchema formDataSchema = new FormDataSchema(TEST_ID, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
    FormDataDefinition formDataDefinition= new FormDataDefinition();
    formDataDefinition.setId(TEST_ID);
    formDataDefinition.setFormId(TEST_FORM_ID);
    formDataDefinition.setVersion(TEST_VERSION);
    formDataDefinition.setFormData(testFormData);
    formDataDefinition.setFormMetaData(testFormMetaData);
    when(mongoTemplate.findOne(any(),any(),any())).thenReturn(formDataDefinition);
    when(formDataAuditService.saveFormDataAudit(any())).thenThrow(JsonProcessingException.class);
    Assertions.assertThrows(InvalidInputException.class,()->formDataService.updateFormData(formDataSchema, "formData.name:akhil", null, null));
  }

}
