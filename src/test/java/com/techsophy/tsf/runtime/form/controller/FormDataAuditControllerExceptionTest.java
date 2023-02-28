package com.techsophy.tsf.runtime.form.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techsophy.tsf.runtime.form.config.CustomFilter;
import com.techsophy.tsf.runtime.form.dto.FormDataAuditSchema;
import com.techsophy.tsf.runtime.form.exception.ExternalServiceErrorException;
import com.techsophy.tsf.runtime.form.exception.FormIdNotFoundException;
import com.techsophy.tsf.runtime.form.exception.GlobalExceptionHandler;
import com.techsophy.tsf.runtime.form.exception.UserDetailsIdNotFoundException;
import com.techsophy.tsf.runtime.form.utils.RelationUtils;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
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
class FormDataAuditControllerExceptionTest
{
    @MockBean
    private RelationUtils mockRelationUtils;
    @MockBean
    TokenUtils mockTokenUtils;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    CustomFilter customFilter;
    @Mock
    private FormDataAuditController mockFormDataAuditController;

    @BeforeEach
    public void setUp()
    {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new GlobalExceptionHandler(), mockFormDataAuditController)
                .addFilters(customFilter).build();
    }

    @Test
    void FormIdNotFoundExceptionTest() throws Exception
    {
        Mockito.when(mockTokenUtils.getIssuerFromToken(TOKEN)).thenReturn(TENANT);
        Mockito.when(mockFormDataAuditController.getAllFormDatAuditByFormIdAndDocumentId(TEST_FORM_ID,TEST_FORM_DATA_ID)).thenThrow(new FormIdNotFoundException(errorCode,FORM_NOT_FOUND_EXCEPTION));
        RequestBuilder requestBuilderTest = MockMvcRequestBuilders.get(BASE_URL + VERSION_V1 + HISTORY+FORM_DATA_DOCUMENT_ID_URL).param(FORM_ID,TEST_FORM_ID).param(FORM_DATA_ID,TEST_FORM_DATA_ID).header(ACCEPT_LANGUAGE, LOCALE_EN)
                .contentType(MediaType.APPLICATION_JSON);
        MvcResult mvcResult = this.mockMvc.perform(requestBuilderTest).andExpect(status().isInternalServerError()).andReturn();
        assertEquals(500,mvcResult.getResponse().getStatus());
    }

    @Test
    void userDetailsNotFoundExceptionTest() throws Exception
    {
        ObjectMapper objectMapperTest = new ObjectMapper();
        FormDataAuditSchema formDataAuditSchemaTest=new FormDataAuditSchema(null,null,null,null,null,null);
        Mockito.when(mockTokenUtils.getIssuerFromToken(TOKEN)).thenReturn(TENANT);
        Mockito.when(mockFormDataAuditController.saveFormDataAudit(formDataAuditSchemaTest)).thenThrow(new UserDetailsIdNotFoundException(errorCode,USER_DETAILS_NOT_FOUND_WITH_GIVEN_ID));
        RequestBuilder requestBuilderTest = MockMvcRequestBuilders.post(BASE_URL + VERSION_V1 + HISTORY+FORM_DATA_URL).header(ACCEPT_LANGUAGE, LOCALE_EN)
                .content(objectMapperTest.writeValueAsString(formDataAuditSchemaTest))
                .contentType(MediaType.APPLICATION_JSON);
        MvcResult mvcResult = this.mockMvc.perform(requestBuilderTest).andExpect(status().isInternalServerError()).andReturn();
        assertEquals(500,mvcResult.getResponse().getStatus());
    }

    @Test
    void ExternalServiceErrorExceptionTest() throws Exception
    {
        Mockito.when(mockTokenUtils.getIssuerFromToken(TOKEN)).thenReturn(TENANT);
        Mockito.when(mockFormDataAuditController.getAllFormDatAuditByFormIdAndDocumentId(TEST_FORM_ID,TEST_FORM_DATA_ID)).thenThrow(new ExternalServiceErrorException(errorCode,FORM_NOT_FOUND_EXCEPTION));
        RequestBuilder requestBuilderTest = MockMvcRequestBuilders.get(BASE_URL + VERSION_V1 + HISTORY+FORM_DATA_DOCUMENT_ID_URL).param(FORM_ID,TEST_FORM_ID).param(FORM_DATA_ID,TEST_FORM_DATA_ID).header(ACCEPT_LANGUAGE, LOCALE_EN)
                .contentType(MediaType.APPLICATION_JSON);
        MvcResult mvcResult = this.mockMvc.perform(requestBuilderTest).andExpect(status().isInternalServerError()).andReturn();
        assertEquals(500,mvcResult.getResponse().getStatus());
    }
}
