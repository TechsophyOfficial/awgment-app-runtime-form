package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.assertions.Assertions;
import com.techsophy.tsf.runtime.form.dto.Component;
import com.techsophy.tsf.runtime.form.dto.FormDataSchema;
import com.techsophy.tsf.runtime.form.dto.FormResponseSchema;
import com.techsophy.tsf.runtime.form.dto.Validate;
import com.techsophy.tsf.runtime.form.service.impl.FormValidationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;

@ExtendWith({MockitoExtension.class})
class FormValidationServiceTest
{
    @Mock
    ObjectMapper mockObjectMapper;
    @InjectMocks
    FormValidationServiceImpl formValidationService;
    List<Map<String,Object>> list=new ArrayList<>();

    @Test
    void validateDataTest()
    {
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String,Object> components=new HashMap<>();
        LinkedHashMap<String,Object> component=new LinkedHashMap<>();
        component.put("label","Text Field");
        component.put("key","textField");
        List<LinkedHashMap> componentList=new ArrayList<>();
        componentList.add(component);
        components.put("display","form");
        components.put("components",componentList);
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_FORM_ID, TEST_NAME,components, list,TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID,
                TEST_CREATED_ON, TEST_UPDATED_BY_ID, TEST_UPDATED_ON);
        FormDataSchema formDataSchemaTest=new FormDataSchema(EMPTY_STRING,TEST_FORM_ID,
                TEST_VERSION,testFormData,testFormMetaData);
        Assertions.assertNotNull(formValidationService.validateData(formResponseSchemaTest,formDataSchemaTest,TEST_FORM_ID));
    }

    @Test
    void validateComponentNullTest()
    {
        Component component=new Component();
        Map<String,Object> dataMap=new HashMap<>();
        Assertions.assertNotNull(formValidationService.validateComponent(component,dataMap,TEST_FORM_ID));
    }

    @Test
    void validateComponentTest()
    {
        Component component=new Component();
        component.setType("textField");
        component.setValidate(new Validate(true,"a-z",
        null,null,null,null,1.0,10.0,1,100,
                null,null,null,2,100,null,null));
        Map<String,Object> dataMap=new HashMap<>();
        Assertions.assertNotNull(formValidationService.validateComponent(component,dataMap,TEST_FORM_ID));
    }
}