package com.techsophy.tsf.runtime.form.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techsophy.tsf.runtime.form.config.CustomFilter;
import com.techsophy.tsf.runtime.form.dto.FormDataSchema;
import com.techsophy.tsf.runtime.form.exception.FormIdNotFoundException;
import com.techsophy.tsf.runtime.form.exception.GlobalExceptionHandler;
import com.techsophy.tsf.runtime.form.exception.InvalidInputException;
import com.techsophy.tsf.runtime.form.exception.UserDetailsIdNotFoundException;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.io.InputStream;
import static com.techsophy.tsf.runtime.form.constants.ErrorConstants.FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC;
import static com.techsophy.tsf.runtime.form.constants.ErrorConstants.FORM_NOT_FOUND_EXCEPTION;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.TOKEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles(TEST_ACTIVE_PROFILE)
@ExtendWith({MockitoExtension.class})
@AutoConfigureMockMvc(addFilters = false)
class FormDataControllerExceptionTest
{
    @MockBean
    TokenUtils mockTokenUtils;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    CustomFilter customFilter;
    @Mock
    private FormDataController mockFormDataController;

    @BeforeEach
    public void setUp()
    {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new GlobalExceptionHandler(), mockFormDataController)
                .addFilters(customFilter).build();
    }

    @Test
    void userDetailsNotFoundExceptionTest() throws Exception
    {
        InputStream inputStreamTest = new ClassPathResource(TEST_RUNTIME_FORM_DATA_1).getInputStream();
        ObjectMapper objectMapperTest = new ObjectMapper();
        FormDataSchema formDataSchemaTest = objectMapperTest.readValue(inputStreamTest, FormDataSchema.class);
        Mockito.when(mockTokenUtils.getIssuerFromToken(TOKEN)).thenReturn(TENANT);
        Mockito.when(mockFormDataController.saveFormData(formDataSchemaTest)).thenThrow(new UserDetailsIdNotFoundException(errorCode,USER_DETAILS_NOT_FOUND_WITH_GIVEN_ID));
        RequestBuilder requestBuilderTest = MockMvcRequestBuilders.post(BASE_URL + VERSION_V1 + FORM_DATA_URL).header(ACCEPT_LANGUAGE, "en")
                .content(objectMapperTest.writeValueAsString(formDataSchemaTest))
                .contentType(MediaType.APPLICATION_JSON);
        MvcResult mvcResult = this.mockMvc.perform(requestBuilderTest).andExpect(status().isInternalServerError()).andReturn();
        assertEquals(500,mvcResult.getResponse().getStatus());
    }

    @Test
    void FormIdNotFoundExceptionTest() throws Exception
    {
        InputStream inputStreamTest = new ClassPathResource(TEST_RUNTIME_FORM_DATA_1).getInputStream();
        ObjectMapper objectMapperTest = new ObjectMapper();
        FormDataSchema formDataSchemaTest = objectMapperTest.readValue(inputStreamTest, FormDataSchema.class);
        Mockito.when(mockTokenUtils.getIssuerFromToken(TOKEN)).thenReturn(TENANT);
        Mockito.when(mockFormDataController.saveFormData(formDataSchemaTest)).thenThrow(new FormIdNotFoundException(errorCode,FORM_NOT_FOUND_EXCEPTION));
        RequestBuilder requestBuilderTest = MockMvcRequestBuilders.post(BASE_URL + VERSION_V1 + FORM_DATA_URL).header(ACCEPT_LANGUAGE, "en")
                .content(objectMapperTest.writeValueAsString(formDataSchemaTest))
                .contentType(MediaType.APPLICATION_JSON);
        MvcResult mvcResult = this.mockMvc.perform(requestBuilderTest).andExpect(status().isInternalServerError()).andReturn();
        assertEquals(500,mvcResult.getResponse().getStatus());
    }

    @Test
    void InvalidInputExceptionTest() throws Exception
    {
        InputStream inputStreamTest = new ClassPathResource(TEST_RUNTIME_FORM_DATA_1).getInputStream();
        ObjectMapper objectMapperTest = new ObjectMapper();
        FormDataSchema formDataSchemaTest = objectMapperTest.readValue(inputStreamTest, FormDataSchema.class);
        Mockito.when(mockTokenUtils.getIssuerFromToken(TOKEN)).thenReturn(TENANT);
        Mockito.when(mockFormDataController.saveFormData(formDataSchemaTest)).thenThrow(new InvalidInputException(errorCode, FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC));
        RequestBuilder requestBuilderTest = MockMvcRequestBuilders.post(BASE_URL + VERSION_V1 + FORM_DATA_URL).header(ACCEPT_LANGUAGE, "en")
                .content(objectMapperTest.writeValueAsString(formDataSchemaTest))
                .contentType(MediaType.APPLICATION_JSON);
        MvcResult mvcResult = this.mockMvc.perform(requestBuilderTest).andExpect(status().isInternalServerError()).andReturn();
        assertEquals(500,mvcResult.getResponse().getStatus());
    }
}
