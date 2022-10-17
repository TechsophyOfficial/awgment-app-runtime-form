package com.techsophy.tsf.runtime.form.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techsophy.tsf.runtime.form.config.CustomFilter;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.constants.FormModelerConstants;
import com.techsophy.tsf.runtime.form.controller.impl.FormDataControllerImpl;
import com.techsophy.tsf.runtime.form.dto.FormDataResponse;
import com.techsophy.tsf.runtime.form.dto.FormDataResponseSchema;
import com.techsophy.tsf.runtime.form.dto.FormDataSchema;
import com.techsophy.tsf.runtime.form.dto.PaginationResponsePayload;
import com.techsophy.tsf.runtime.form.model.ApiResponse;
import com.techsophy.tsf.runtime.form.service.FormDataService;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

import java.awt.print.Pageable;
import java.io.InputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;

import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.Q;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.TOKEN;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles(TEST_ACTIVE_PROFILE)
@ExtendWith({MockitoExtension.class})
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FormDataControllerTest
{
    private static final SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtSaveOrUpdate = jwt().authorities(new SimpleGrantedAuthority(AWGMENT_RUNTIME_FORM_CREATE_OR_UPDATE));
    private static final SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtRead = jwt().authorities(new SimpleGrantedAuthority(AWGMENT_RUNTIME_FORM_READ));
    private static final SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtDelete = jwt().authorities(new SimpleGrantedAuthority(AWGMENT_RUNTIME_FORM_DELETE));

    @MockBean
    TokenUtils mockTokenUtils;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    FormDataService mockFormService;
    @Autowired
    WebApplicationContext webApplicationContext;
    @Autowired
    CustomFilter customFilter;
    @Mock
    FormDataService formDataService ;
    @Mock
    GlobalMessageSource globalMessageSource;
    @InjectMocks
    FormDataControllerImpl formDataController;
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
    void saveFormDataTest() throws Exception
    {
        InputStream inputStreamTest = new ClassPathResource(TEST_RUNTIME_FORM_DATA_1).getInputStream();
        ObjectMapper objectMapperTest = new ObjectMapper();
        FormDataSchema formDataSchemaTest = objectMapperTest.readValue(inputStreamTest, FormDataSchema.class);
        Mockito.when(mockTokenUtils.getIssuerFromToken(TOKEN)).thenReturn(TENANT);
        Mockito.when(mockFormService.saveFormData(formDataSchemaTest)).thenReturn(new FormDataResponse(TEST_ID, TEST_VERSION));
        RequestBuilder requestBuilderTest = MockMvcRequestBuilders.post(BASE_URL + VERSION_V1 + FORM_DATA_URL).header(ACCEPT_LANGUAGE, LOCALE_EN)
                .content(objectMapperTest.writeValueAsString(formDataSchemaTest))
                .with(jwtSaveOrUpdate)
                .contentType(MediaType.APPLICATION_JSON);
        MvcResult mvcResult = this.mockMvc.perform(requestBuilderTest).andExpect(status().isOk()).andReturn();
        assertEquals(200, mvcResult.getResponse().getStatus());
    }
    @Test
    void updateFormData() throws Exception
    {
        ObjectMapper objectMapperTest = new ObjectMapper();
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        map.put(STRING,STRING);
        FormDataSchema formDataSchema = new FormDataSchema("1","1",1,map,map);
        formDataController.updateFormData(formDataSchema);
        verify(formDataService,times(0)).updateFormData(any());
    }

    @Test
    void getAllFormDataByFormIdFilterEmptyPaginationTest() throws Exception
    {
        Map<String,Object> map = new HashMap<>();
        map.put(STRING,STRING);
        PaginationResponsePayload paginationResponsePayload = new PaginationResponsePayload();
        FormDataResponseSchema formDataResponseSchema = new FormDataResponseSchema("1",map,map, String.valueOf(1),STRING, Instant.now(),STRING,"1",Instant.now(),STRING);
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(BASE_URL+VERSION_V1+FORM_DATA_URL).param(FORM_ID,"1").with(jwtRead).contentType(MediaType.APPLICATION_JSON);
        RequestBuilder requestBuilder1 = MockMvcRequestBuilders.get(BASE_URL+VERSION_V1+FORM_DATA_URL).param(FORM_ID,"1").param(FILTER,STRING).with(jwtRead).contentType(MediaType.APPLICATION_JSON);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.get(BASE_URL+VERSION_V1+FORM_DATA_URL).param(FORM_ID,"1").param(SORT_BY,STRING).param(Q,STRING).with(jwtRead).contentType(MediaType.APPLICATION_JSON);
        RequestBuilder requestBuilder4 = MockMvcRequestBuilders.get(BASE_URL+VERSION_V1+FORM_DATA_URL).param(FORM_ID,"1").param(SORT_BY,STRING).param(Q,STRING).param(PAGE,"1").param(SIZE,PAGE_SIZE).with(jwtRead).contentType(MediaType.APPLICATION_JSON);
        RequestBuilder requestBuilder2 = MockMvcRequestBuilders.get(BASE_URL+VERSION_V1+FORM_DATA_URL).param(FORM_ID,"1").param(FILTER,STRING).param(PAGE,"1").param(SIZE,PAGE_SIZE).with(jwtRead).contentType(MediaType.APPLICATION_JSON);
        MvcResult mvcResult = this.mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();
        MvcResult mvcResult1 = this.mockMvc.perform(requestBuilder1).andExpect(status().isOk()).andReturn();
        MvcResult mvcResult2 = this.mockMvc.perform(requestBuilder2).andExpect(status().isOk()).andReturn();
        MvcResult mvcResult3 = this.mockMvc.perform(requestBuilder3).andExpect(status().isOk()).andReturn();
        MvcResult mvcResult4 = this.mockMvc.perform(requestBuilder4).andExpect(status().isOk()).andReturn();
        assertEquals(200, mvcResult.getResponse().getStatus());
        assertEquals(200, mvcResult1.getResponse().getStatus());
        assertEquals(200, mvcResult2.getResponse().getStatus());
        assertEquals(200, mvcResult3.getResponse().getStatus());
        assertEquals(200, mvcResult4.getResponse().getStatus());
    }

    @Test
    void deleteAllFormDataByFormId() throws Exception {
        Mockito.when(mockTokenUtils.getIssuerFromToken(TOKEN)).thenReturn(TENANT);
        RequestBuilder requestBuilderTest = MockMvcRequestBuilders
                .delete(BASE_URL + VERSION_V1 + FORM_DATA_URL).param(FORM_ID, TEST_FORM_ID)
                .with(jwtDelete);
        MvcResult mvcResult = this.mockMvc.perform(requestBuilderTest).andExpect(status().isOk()).andReturn();
        assertEquals(200, mvcResult.getResponse().getStatus());
    }

    @Test
    void deleteFormDataByFormIdAndId() throws Exception
    {
        Mockito.when(mockTokenUtils.getIssuerFromToken(TOKEN)).thenReturn(TENANT);
        RequestBuilder requestBuilderTest=MockMvcRequestBuilders
                .delete(BASE_URL + VERSION_V1 + FORM_DATA_ID_URL,TEST_FORM_ID).param(ID,TEST_ID)
                .with(jwtDelete);
        MvcResult mvcResult = this.mockMvc.perform(requestBuilderTest).andExpect(status().isOk()).andReturn();
        assertEquals(200,mvcResult.getResponse().getStatus());
    }

    @Test
    void validateFormDataByFormIdTest() throws Exception
    {
        InputStream inputStreamTest = new ClassPathResource(TEST_RUNTIME_FORM_DATA_1).getInputStream();
        ObjectMapper objectMapperTest = new ObjectMapper();
        FormDataSchema formDataSchemaTest = objectMapperTest.readValue(inputStreamTest, FormDataSchema.class);
        Mockito.when(mockTokenUtils.getIssuerFromToken(TOKEN)).thenReturn(TENANT);
        Mockito.when(mockFormService.validateFormDataByFormId(formDataSchemaTest)).thenReturn(FORM_DATA_VALIDATED_SUCCESSFULLY);
        RequestBuilder requestBuilderTest = MockMvcRequestBuilders.post(BASE_URL + VERSION_V1 + FORM_DATA_VALIDATE_URL).header(ACCEPT_LANGUAGE, LOCALE_EN)
                .content(objectMapperTest.writeValueAsString(formDataSchemaTest))
                .with(jwtSaveOrUpdate)
                .contentType(MediaType.APPLICATION_JSON);
        MvcResult mvcResult = this.mockMvc.perform(requestBuilderTest).andExpect(status().isOk()).andReturn();
        assertEquals(200, mvcResult.getResponse().getStatus());
    }

    @Test
    void getFormDataByFormIdAndIdTest() throws Exception {
        Mockito.when(mockTokenUtils.getIssuerFromToken(TOKEN)).thenReturn(TENANT);
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(BASE_URL+VERSION_V1+FORM_DATA_ID_URL,"1").param(FORM_ID,"1").param(ID,TEST_ID_VALUE).param(RELATIONS,STRING).with(jwtRead).contentType(MediaType.APPLICATION_JSON);
        MvcResult mvcResult = this.mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();
        assertEquals(200, mvcResult.getResponse().getStatus());

    }
}
