package com.techsophy.tsf.runtime.form.service.impl;

import com.techsophy.idgenerator.IdGeneratorImpl;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.FormDataSchema;
import com.techsophy.tsf.runtime.form.exception.InvalidInputException;
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

import java.util.LinkedHashMap;

import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;

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
}