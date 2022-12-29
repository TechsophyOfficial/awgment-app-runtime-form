package com.techsophy.tsf.runtime.form.service;

import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.exception.InvalidInputException;
import com.techsophy.tsf.runtime.form.service.impl.ValidationCheckServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.FORM_DATA;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.MAX_LENGTH;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.MIN_LENGTH;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith({MockitoExtension.class})
class ValidationCheckServiceImplTest
{
    @Mock
    List<String> mockKeys;
    @Mock
    GlobalMessageSource globalMessageSource;
    @Mock
    MongoTemplate mockMongoTemplate;
    @InjectMocks
    ValidationCheckServiceImpl mockValidationCheckServiceImpl;

    @Test
    void isMandatoryFieldsMissingTest()
    {
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        LinkedHashMap<String,Object> dataMapTest =new LinkedHashMap<>();
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
        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
        fieldsMap.put(REQUIRED,false);
        fieldsMap.put(UNIQUE,false);
        schemaMap.put(NAME,fieldsMap);
        Assertions.assertNotNull(mockValidationCheckServiceImpl.allFieldsValidations(schemaMap,dataMapTest,TEST_FORM_ID,TEST_ID));
    }

    @Test
    void isFieldsMissingTrueTest()
    {
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        LinkedHashMap<String,Object> dataMapTest =new LinkedHashMap<>();
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
        dataMapTest.put(EMAIL,TEST_EMAIL_ADDRESS);
        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
        fieldsMap.put(REQUIRED,false);
        fieldsMap.put(UNIQUE,false);
        schemaMap.put(NAME,fieldsMap);
        Assertions.assertNotNull(mockValidationCheckServiceImpl.allFieldsValidations(schemaMap,dataMapTest,TEST_FORM_ID,TEST_ID));
    }

    @Test
    void isFieldsMissingFalseTest()
    {
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        LinkedHashMap<String,Object> dataMapTest =new LinkedHashMap<>();
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
        dataMapTest.put(EMAIL,TEST_EMAIL_ADDRESS);
        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
        fieldsMap.put(REQUIRED,false);
        fieldsMap.put(UNIQUE,false);
        schemaMap.put(NAME,fieldsMap);
        Assertions.assertNotNull(mockValidationCheckServiceImpl.allFieldsValidations(schemaMap,dataMapTest,TEST_FORM_ID,TEST_ID));
    }

    @Test
    void isFieldsMissingInvalidInputExceptionTest()
    {
        LinkedHashMap<String,Object> dataMapTest =new LinkedHashMap<>();
        dataMapTest.put(EMAIL, EMPTY_STRING);
        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
        fieldsMap.put(REQUIRED,false);
        fieldsMap.put(UNIQUE,false);
        schemaMap.put(NAME,fieldsMap);
        Assertions.assertThrows(InvalidInputException.class, () ->
                mockValidationCheckServiceImpl.allFieldsValidations(schemaMap,dataMapTest,TEST_FORM_ID,TEST_ID));
    }

    @Test
    void checkMissingUniqueLengthWordCountTest()
    {
        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
        fieldsMap.put(REQUIRED,true);
        schemaMap.put(NAME,fieldsMap);
        LinkedHashMap<String,Object> dataMapTest =new LinkedHashMap<>();
        List<String> result =mockValidationCheckServiceImpl.allFieldsValidations(schemaMap,dataMapTest,TEST_FORM_ID,TEST_ID);
        Assertions.assertEquals(0,Integer.parseInt(result.get(0)));
    }

    @Test
    void checkMinimumLengthTest()
    {
        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
        fieldsMap.put(MIN_LENGTH,9);
        schemaMap.put(NAME,fieldsMap);
        LinkedHashMap<String,Object> dataMapTest =new LinkedHashMap<>();
        dataMapTest.put(NAME,EMPTY_STRING);
        List<String> result =mockValidationCheckServiceImpl.allFieldsValidations(schemaMap,dataMapTest,TEST_FORM_ID,TEST_ID);
        Assertions.assertEquals(2,Integer.parseInt(result.get(0)));
    }

    @Test
    void checkMaxLengthTest()
    {
        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
        fieldsMap.put(MAX_LENGTH,3);
        schemaMap.put(NAME,fieldsMap);
        LinkedHashMap<String,Object> dataMapTest =new LinkedHashMap<>();
        dataMapTest.put(NAME,NAME_VALUE);
        List<String> result =mockValidationCheckServiceImpl.allFieldsValidations(schemaMap,dataMapTest,TEST_FORM_ID,TEST_ID);
        Assertions.assertEquals(3,Integer.parseInt(result.get(0)));
    }

    @Test
    void checkMinIntegerValueTest()
    {
        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
        fieldsMap.put(MIN,25);
        schemaMap.put(AGE,fieldsMap);
        LinkedHashMap<String,Object> dataMapTest =new LinkedHashMap<>();
        dataMapTest.put(AGE,AGE_VALUE);
        List<String> result =mockValidationCheckServiceImpl.allFieldsValidations(schemaMap,dataMapTest,TEST_FORM_ID,TEST_ID);
        Assertions.assertEquals(5,Integer.parseInt(result.get(0)));
    }

    @Test
    void checkMaxIntegerValueTest()
    {
        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
        fieldsMap.put(MAX,60);
        schemaMap.put(AGE,fieldsMap);
        LinkedHashMap<String,Object> dataMapTest =new LinkedHashMap<>();
        dataMapTest.put(AGE,PERSON_AGE);
        List<String> result =mockValidationCheckServiceImpl.allFieldsValidations(schemaMap,dataMapTest,TEST_FORM_ID,TEST_ID);
        Assertions.assertEquals(7, Integer.parseInt(result.get(0)));
    }

    @Test
    void checkMinWordsTest()
    {
        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
        fieldsMap.put(MIN_WORDS,2);
        schemaMap.put(NAME,fieldsMap);
        LinkedHashMap<String,Object> dataMapTest =new LinkedHashMap<>();
        dataMapTest.put(NAME,EMPTY_STRING);
        List<String> result =mockValidationCheckServiceImpl.allFieldsValidations(schemaMap,dataMapTest,TEST_FORM_ID,TEST_ID);
        Assertions.assertEquals(8,Integer.parseInt(result.get(0)));
    }

    @Test
    void checkMaxWordsTest()
    {
        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
        fieldsMap.put(MAX_WORDS,1);
        schemaMap.put(NAME,fieldsMap);
        LinkedHashMap<String,Object> dataMapTest =new LinkedHashMap<>();
        dataMapTest.put(NAME,NAME_VALUE);
        List<String> result =mockValidationCheckServiceImpl.allFieldsValidations(schemaMap,dataMapTest,TEST_FORM_ID,TEST_ID);
        Assertions.assertEquals(9,Integer.parseInt(result.get(0)));
    }

    @Test
    void checkIntegerNoAlphabetTest()
    {
        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
        fieldsMap.put(MIN,25);
        schemaMap.put(AGE,fieldsMap);
        LinkedHashMap<String,Object> dataMapTest =new LinkedHashMap<>();
        dataMapTest.put(AGE,NAME_VALUE);
        List<String> result =mockValidationCheckServiceImpl.allFieldsValidations(schemaMap,dataMapTest,TEST_FORM_ID,TEST_ID);
        Assertions.assertEquals(4,Integer.parseInt(result.get(0)));
    }

    @Test
    void checkIntegerNoAlphabetTest2()
    {
        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
        fieldsMap.put(MAX,60);
        schemaMap.put(AGE,fieldsMap);
        LinkedHashMap<String,Object> dataMapTest =new LinkedHashMap<>();
        dataMapTest.put(AGE,NAME_VALUE);
        List<String> result =mockValidationCheckServiceImpl.allFieldsValidations(schemaMap,dataMapTest,TEST_FORM_ID,TEST_ID);
        Assertions.assertEquals(6,Integer.parseInt(result.get(0)));
    }

    @Test
    void criteriaListTest()
    {
        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
        fieldsMap.put(UNIQUE,true);
        schemaMap.put(NAME,fieldsMap);
        LinkedHashMap<String,Object> dataMapTest =new LinkedHashMap<>();
        dataMapTest.put(NAME,NAME_VALUE);
        Mockito.when(mockMongoTemplate.exists(any(), (String) any())).thenReturn(true);
        List<String> result =mockValidationCheckServiceImpl.allFieldsValidations(schemaMap,dataMapTest,TEST_FORM_ID,EMPTY_STRING);
        Assertions.assertEquals(1,Integer.parseInt(result.get(0)));
    }

    @Test
    void defaultTest()
    {
        LinkedHashMap<String,LinkedHashMap<String,Object>> schemaMap=new LinkedHashMap<>();
        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
        fieldsMap.put(UNIQUE,true);
        schemaMap.put(NAME,fieldsMap);
        LinkedHashMap<String,Object> dataMapTest =new LinkedHashMap<>();
        dataMapTest.put(NAME,NAME_VALUE);
        List<String> result =mockValidationCheckServiceImpl.allFieldsValidations(schemaMap,dataMapTest,TEST_FORM_ID,EMPTY_STRING);
        Assertions.assertEquals(-1,Integer.parseInt(result.get(0)));
    }
}
