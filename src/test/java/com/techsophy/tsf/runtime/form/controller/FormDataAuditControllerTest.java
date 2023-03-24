package com.techsophy.tsf.runtime.form.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techsophy.tsf.runtime.form.config.CustomFilter;
import com.techsophy.tsf.runtime.form.dto.FormDataAuditResponseSchema;
import com.techsophy.tsf.runtime.form.service.FormDataAuditService;
import com.techsophy.tsf.runtime.form.utils.RelationUtils;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.InputStream;
import java.util.List;

import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.TOKEN;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles(TEST_ACTIVE_PROFILE)
@ExtendWith({MockitoExtension.class})
@AutoConfigureMockMvc(addFilters = false)
class FormDataAuditControllerTest
{
    private static final SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtSaveOrUpdate = jwt().authorities(new SimpleGrantedAuthority(AWGMENT_RUNTIME_FORM_CREATE_OR_UPDATE));
    private static final SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtRead = jwt().authorities(new SimpleGrantedAuthority(AWGMENT_RUNTIME_FORM_READ));

    @MockBean
    private RelationUtils mockRelationUtils;
    @MockBean
    TokenUtils mockTokenUtils;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    FormDataAuditService mockFormDataAuditServiceImpl;
    @Autowired
    WebApplicationContext webApplicationContext;
    @Autowired
    CustomFilter customFilter;

    @BeforeEach
    public void setUp()
    {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(customFilter)
                .apply(springSecurity())
                .build();
    }

    @Test
    void getAllFormDataByFormIdDocumentId() throws Exception
    {
        InputStream inputStreamTest = new ClassPathResource(TEST_RUNTIME_FORM_DATA_AUDIT_1).getInputStream();
        ObjectMapper objectMapperTest = new ObjectMapper();
        FormDataAuditResponseSchema formDataAuditResponseSchema = objectMapperTest.readValue(inputStreamTest, FormDataAuditResponseSchema.class);
        Mockito.when(mockTokenUtils.getIssuerFromToken(TOKEN)).thenReturn(TENANT);
        Mockito.when(mockFormDataAuditServiceImpl.getAllFormDataAuditByFormIdAndDocumentId(TEST_FORM_ID,TEST_FORM_DATA_ID)).thenReturn
                (List.of(new FormDataAuditResponseSchema(TEST_ID,TEST_FORM_DATA_ID,TEST_FORM_ID,TEST_VERSION,TEST_FORM_DATA,TEST_FORM_META_DATA,TEST_CREATED_BY_ID,String.valueOf(TEST_CREATED_ON))
                        ,new FormDataAuditResponseSchema(TEST_ID,TEST_FORM_DATA_ID,TEST_FORM_ID,TEST_VERSION,TEST_FORM_DATA,TEST_FORM_META_DATA,TEST_CREATED_BY_ID,String.valueOf(TEST_CREATED_ON))));
        RequestBuilder requestBuilderTest = MockMvcRequestBuilders.get(BASE_URL + VERSION_V1 + HISTORY+FORM_DATA_DOCUMENT_ID_URL).param(FORM_ID,TEST_FORM_ID).param(FORM_DATA_ID,TEST_FORM_DATA_ID).header(ACCEPT_LANGUAGE, LOCALE_EN)
                .content(objectMapperTest.writeValueAsString(formDataAuditResponseSchema))
                .with(jwtRead)
                .contentType(MediaType.APPLICATION_JSON);
        MvcResult mvcResult = this.mockMvc.perform(requestBuilderTest).andExpect(status().isOk()).andReturn();
        assertEquals(200, mvcResult.getResponse().getStatus());
    }
}
