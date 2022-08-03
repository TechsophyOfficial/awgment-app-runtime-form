package com.techsophy.tsf.runtime.form.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techsophy.tsf.runtime.form.config.CustomFilter;
import com.techsophy.tsf.runtime.form.dto.FormDataResponse;
import com.techsophy.tsf.runtime.form.dto.FormDataSchema;
import com.techsophy.tsf.runtime.form.service.FormDataService;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.ID;
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

//    @Test
//    void getAllFormDataByFormIdFilterEmptyPaginationTest() throws Exception
//    {
//        InputStream inputStreamTest = new ClassPathResource(TEST_RUNTIME_FORM_DATA_1).getInputStream();
//        ObjectMapper objectMapperTest = new ObjectMapper();
//        FormDataResponseSchema formDataResponseSchemaListTest = objectMapperTest.readValue(inputStreamTest, FormDataResponseSchema.class);
//        Mockito.when(mockTokenUtils.getIssuerFromToken(TOKEN)).thenReturn(TENANT);
//        Mockito.when(mockFormService.getAllFormDataByFormId(TEST_FORM_ID,TEST_RELATIONS,TEST_FILTER,TEST_SORT_BY,TEST_SORT_ORDER))
//                .thenReturn(List.of(new FormDataResponseSchema(TEST_ID,TEST_VERSION,TEST_FORM_DATA,TEST_FORM_META_DATA,TEST_CREATED_BY_ID,TEST_CREATED_ON,TEST_CREATED_BY_NAME,TEST_UPDATED_BY_ID,TEST_UPDATED_ON,TEST_UPDATED_BY_NAME)
//                        ,new FormDataResponseSchema(TEST_ID,TEST_VERSION,TEST_FORM_DATA,TEST_FORM_META_DATA,TEST_CREATED_BY_ID,TEST_CREATED_ON,TEST_CREATED_BY_NAME,TEST_UPDATED_BY_ID,TEST_UPDATED_ON,TEST_UPDATED_BY_NAME)));
//        RequestBuilder requestBuilderTest = MockMvcRequestBuilders.get(BASE_URL + VERSION_V1+FORM_DATA_URL).param(FORM_ID,TEST_FORM_ID).param(SORT_BY,TEST_SORT_BY).param(SORT_ORDER,TEST_SORT_ORDER).param(FILTER,TEST_FILTER).param(Q, EMPTY_STRING).header(ACCEPT_LANGUAGE, LOCALE_EN)
//                .content(objectMapperTest.writeValueAsString(formDataResponseSchemaListTest))
//                .with(jwtRead)
//                .contentType(MediaType.APPLICATION_JSON);
//        MvcResult mvcResult = this.mockMvc.perform(requestBuilderTest).andExpect(status().isOk()).andReturn();
//        assertEquals(200, mvcResult.getResponse().getStatus());
//    }

//    @Test
//    void getAllFormDataByFormIdFilterPaginationTest() throws Exception
//    {
//        InputStream inputStreamTest = new ClassPathResource(TEST_RUNTIME_FORM_DATA_1).getInputStream();
//        ObjectMapper objectMapperTest = new ObjectMapper();
//        FormDataResponseSchema formDataResponseSchemaListTest = objectMapperTest.readValue(inputStreamTest, FormDataResponseSchema.class);
//        Mockito.when(mockTokenUtils.getIssuerFromToken(TOKEN)).thenReturn(TENANT);
//        PaginationResponsePayload paginationResponsePayload=new PaginationResponsePayload();
//        Mockito.when(mockFormService.getAllFormDataByFormId(TEST_FORM_ID,TEST_RELATIONS,TEST_FILTER,TEST_SORT_BY,TEST_SORT_ORDER, PageRequest.of(0,5)))
//                .thenReturn(paginationResponsePayload);
//        RequestBuilder requestBuilderTest = MockMvcRequestBuilders.get(BASE_URL + VERSION_V1+FORM_DATA_URL).param(FORM_ID,TEST_FORM_ID).param(PAGE,ZERO).param(SIZE,FIVE).param(SORT_BY,TEST_SORT_BY).param(SORT_ORDER,TEST_SORT_ORDER).param(FILTER,TEST_FILTER).param(Q,EMPTY_STRING).header(ACCEPT_LANGUAGE, LOCALE_EN)
//                .content(objectMapperTest.writeValueAsString(formDataResponseSchemaListTest))
//                .with(jwtRead)
//                .contentType(MediaType.APPLICATION_JSON);
//        MvcResult mvcResult = this.mockMvc.perform(requestBuilderTest).andExpect(status().isOk()).andReturn();
//        assertEquals(200, mvcResult.getResponse().getStatus());
//    }

//    @Test
//    void getAllFormDataByFormIdAndQEmptyPaginationTest() throws Exception
//    {
//        InputStream inputStreamTest = new ClassPathResource(TEST_RUNTIME_FORM_DATA_1).getInputStream();
//        ObjectMapper objectMapperTest = new ObjectMapper();
//        FormDataResponseSchema formDataResponseSchemaListTest = objectMapperTest.readValue(inputStreamTest, FormDataResponseSchema.class);
//        Mockito.when(mockTokenUtils.getIssuerFromToken(TOKEN)).thenReturn(TENANT);
//        Mockito.when(mockFormService.getAllFormDataByFormIdAndQ(TEST_FORM_ID,TEST_RELATIONS,Q,TEST_SORT_BY,TEST_SORT_ORDER))
//                .thenReturn(List.of(new FormDataResponseSchema(TEST_ID,TEST_VERSION,TEST_FORM_DATA,TEST_FORM_META_DATA,TEST_CREATED_BY_ID,TEST_CREATED_ON,TEST_CREATED_BY_NAME,TEST_UPDATED_BY_ID,TEST_UPDATED_ON,TEST_UPDATED_BY_NAME)
//                        ,new FormDataResponseSchema(TEST_ID,TEST_VERSION,TEST_FORM_DATA,TEST_FORM_META_DATA,TEST_CREATED_BY_ID,TEST_CREATED_ON,TEST_CREATED_BY_NAME,TEST_UPDATED_BY_ID,TEST_UPDATED_ON,TEST_UPDATED_BY_NAME)));
//        RequestBuilder requestBuilderTest = MockMvcRequestBuilders.get(BASE_URL + VERSION_V1+FORM_DATA_URL).param(FORM_ID,TEST_FORM_ID).param(SORT_BY,TEST_SORT_BY).param(SORT_ORDER,TEST_SORT_ORDER).param(Q,EMPTY_STRING).header(ACCEPT_LANGUAGE, LOCALE_EN)
//                .content(objectMapperTest.writeValueAsString(formDataResponseSchemaListTest))
//                .with(jwtRead)
//                .contentType(MediaType.APPLICATION_JSON);
//        MvcResult mvcResult = this.mockMvc.perform(requestBuilderTest).andExpect(status().isOk()).andReturn();
//        assertEquals(200, mvcResult.getResponse().getStatus());
//    }

//    @Test
//    void getAllFormDataByFormIdAndQPaginationTest() throws Exception
//    {
//        InputStream inputStreamTest = new ClassPathResource(TEST_RUNTIME_FORM_DATA_1).getInputStream();
//        ObjectMapper objectMapperTest = new ObjectMapper();
//        FormDataResponseSchema formDataResponseSchemaListTest = objectMapperTest.readValue(inputStreamTest, FormDataResponseSchema.class);
//        Mockito.when(mockTokenUtils.getIssuerFromToken(TOKEN)).thenReturn(TENANT);
//        PaginationResponsePayload paginationResponsePayload=new PaginationResponsePayload();
//        Mockito.when(mockFormService.getAllFormDataByFormIdAndQ(TEST_FORM_ID,TEST_RELATIONS,Q,TEST_SORT_BY,TEST_SORT_ORDER, PageRequest.of(0,5)))
//                .thenReturn(paginationResponsePayload);
//        RequestBuilder requestBuilderTest = MockMvcRequestBuilders.get(BASE_URL + VERSION_V1+FORM_DATA_URL).param(FORM_ID,TEST_FORM_ID).param(PAGE,ZERO).param(SIZE,FIVE).param(SORT_BY,TEST_SORT_BY).param(SORT_ORDER,TEST_SORT_ORDER).param(Q,EMPTY_STRING).header(ACCEPT_LANGUAGE, LOCALE_EN)
//                .content(objectMapperTest.writeValueAsString(formDataResponseSchemaListTest))
//                .with(jwtRead)
//                .contentType(MediaType.APPLICATION_JSON);
//        MvcResult mvcResult = this.mockMvc.perform(requestBuilderTest).andExpect(status().isOk()).andReturn();
//        assertEquals(200, mvcResult.getResponse().getStatus());
//    }

//    @Test
//    void getAllFormDataByFormIdOnlyTest() throws Exception
//    {
//        InputStream inputStreamTest = new ClassPathResource(TEST_RUNTIME_FORM_DATA_1).getInputStream();
//        ObjectMapper objectMapperTest = new ObjectMapper();
//        FormDataResponseSchema formDataResponseSchemaListTest = objectMapperTest.readValue(inputStreamTest, FormDataResponseSchema.class);
//        Mockito.when(mockTokenUtils.getIssuerFromToken(TOKEN)).thenReturn(TENANT);
//        PaginationResponsePayload paginationResponsePayload=new PaginationResponsePayload();
//        Mockito.when(mockFormService.getAllFormDataByFormId(TEST_FORM_ID,TEST_RELATIONS)).thenReturn(paginationResponsePayload);
//                       RequestBuilder requestBuilderTest = MockMvcRequestBuilders.get(BASE_URL + VERSION_V1 + FORM_DATA_URL).param(FORM_ID,TEST_FORM_ID).header(ACCEPT_LANGUAGE, LOCALE_EN)
//                .content(objectMapperTest.writeValueAsString(formDataResponseSchemaListTest))
//                .with(jwtRead)
//                .contentType(MediaType.APPLICATION_JSON);
//        MvcResult mvcResult = this.mockMvc.perform(requestBuilderTest).andExpect(status().isOk()).andReturn();
//        assertEquals(200, mvcResult.getResponse().getStatus());
//    }

//    @Test
//    void getFormDataByFormIdAndId() throws Exception
//    {
//        InputStream inputStreamTest = new ClassPathResource(TEST_RUNTIME_FORM_DATA_1).getInputStream();
//        ObjectMapper objectMapperTest = new ObjectMapper();
//        FormDataResponseSchema formDataResponseSchemaListTest = objectMapperTest.readValue(inputStreamTest, FormDataResponseSchema.class);
//        Mockito.when(mockTokenUtils.getIssuerFromToken(TOKEN)).thenReturn(TENANT);
//        Mockito.when(mockFormService.getFormDataByFormIdAndId(TEST_FORM_ID,TEST_ID,TEST_RELATIONS)).thenReturn
//                (Collections.singletonList(new FormDataResponseSchema(TEST_ID, TEST_VERSION, TEST_FORM_DATA, TEST_FORM_META_DATA, TEST_CREATED_BY_ID, TEST_CREATED_ON, TEST_CREATED_BY_NAME, TEST_UPDATED_BY_ID, TEST_UPDATED_ON, TEST_UPDATED_BY_NAME)));
//        RequestBuilder requestBuilderTest = MockMvcRequestBuilders.get(BASE_URL + VERSION_V1 + FORM_DATA_ID_URL,TEST_FORM_ID).param(ID,TEST_ID).header(ACCEPT_LANGUAGE, LOCALE_EN)
//                .content(objectMapperTest.writeValueAsString(formDataResponseSchemaListTest))
//                .with(jwtRead)
//                .contentType(MediaType.APPLICATION_JSON);
//        MvcResult mvcResult = this.mockMvc.perform(requestBuilderTest).andExpect(status().isOk()).andReturn();
//        assertEquals(200, mvcResult.getResponse().getStatus());
//    }

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
}
