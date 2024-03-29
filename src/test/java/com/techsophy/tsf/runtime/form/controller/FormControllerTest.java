package com.techsophy.tsf.runtime.form.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techsophy.tsf.runtime.form.config.CustomFilter;
import com.techsophy.tsf.runtime.form.dto.FormResponseSchema;
import com.techsophy.tsf.runtime.form.dto.FormSchema;
import com.techsophy.tsf.runtime.form.service.FormService;
import com.techsophy.tsf.runtime.form.utils.RelationUtils;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.TOKEN;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles(TEST_ACTIVE_PROFILE)
@ExtendWith({MockitoExtension.class})
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@AutoConfigureMockMvc(addFilters = false)
class FormControllerTest
{
    private static  final SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtSaveOrUpdate = jwt().authorities(new SimpleGrantedAuthority(AWGMENT_RUNTIME_FORM_CREATE_OR_UPDATE));
    private static  final SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtRead = jwt().authorities(new SimpleGrantedAuthority(AWGMENT_RUNTIME_FORM_READ));
    private static  final SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtDelete = jwt().authorities(new SimpleGrantedAuthority(AWGMENT_RUNTIME_FORM_DELETE));

    @MockBean
    TokenUtils mockTokenUtils;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private RelationUtils mockRelationUtils;
    @MockBean
    FormService mockFormService;
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
    void saveRuntimeFormTest() throws Exception
    {
        InputStream inputStreamTest=new ClassPathResource(TEST_FORMS_DATA_1).getInputStream();
        ObjectMapper objectMapperTest=new ObjectMapper();
        FormSchema formSchemaTest=objectMapperTest.readValue(inputStreamTest,FormSchema.class);
        FormSchema formSchemaTest1= new FormSchema("1","1",null,null,null,"component",1,true);
        Mockito.when(mockTokenUtils.getIssuerFromToken(TOKEN)).thenReturn(TENANT);
        RequestBuilder requestBuilderTest = MockMvcRequestBuilders.post(BASE_URL + VERSION_V1 + FORMS_URL).header(ACCEPT_LANGUAGE,LOCALE_EN)
                .content(objectMapperTest.writeValueAsString(formSchemaTest))
                .with(jwtSaveOrUpdate)
                .contentType(MediaType.APPLICATION_JSON);
        RequestBuilder requestBuilderTest1 = MockMvcRequestBuilders.post(BASE_URL + VERSION_V1 + FORMS_URL).header(ACCEPT_LANGUAGE,LOCALE_EN)
                .content(objectMapperTest.writeValueAsString(formSchemaTest1))
                .with(jwtSaveOrUpdate)
                .contentType(MediaType.APPLICATION_JSON);
        MvcResult mvcResult = this.mockMvc.perform(requestBuilderTest).andExpect(status().isOk()).andReturn();
        MvcResult mvcResult1 = this.mockMvc.perform(requestBuilderTest1).andExpect(status().isOk()).andReturn();
        assertEquals(200,mvcResult.getResponse().getStatus());
        assertEquals(200,mvcResult1.getResponse().getStatus());
    }

    @Test
    void getRuntimeFormByIdTest() throws Exception
    {
        List<Map<String,Object>> list=new ArrayList<>();
        Map<String,Object> map=new HashMap<>();
        map.put("create","true");
        list.add(map);
        InputStream resource=new ClassPathResource(TEST_FORMS_DATA_1).getInputStream();
        ObjectMapper objectMapperTest=new ObjectMapper();
        FormSchema formSchemaTest=objectMapperTest.readValue(resource,FormSchema.class);
        FormSchema formSchemaTest1=new FormSchema("1",STRING,map,list,null,"component",1,true);
        FormResponseSchema formResponseSchema = new FormResponseSchema();
        FormResponseSchema formResponseSchema1 = new FormResponseSchema();
        Mockito.when(mockFormService.getRuntimeFormById(any())).thenReturn(formResponseSchema).thenReturn(formResponseSchema1);
        RequestBuilder requestBuilderTest = MockMvcRequestBuilders.get(BASE_URL_TEST+VERSION_V1_TEST+FORM_BY_ID_URL_TEST,1)
                .content(objectMapperTest.writeValueAsString(formSchemaTest))
                .with(jwtRead)
                .contentType(MediaType.APPLICATION_JSON);
        RequestBuilder requestBuilderTest1 = MockMvcRequestBuilders.get(BASE_URL_TEST+VERSION_V1_TEST+FORM_BY_ID_URL_TEST,1)
                .content(objectMapperTest.writeValueAsString(formSchemaTest1))
                .with(jwtRead)
                .contentType(MediaType.APPLICATION_JSON);
        MvcResult mvcResult = this.mockMvc.perform(requestBuilderTest).andExpect(status().isOk()).andReturn();
        MvcResult mvcResult1 = this.mockMvc.perform(requestBuilderTest1).andExpect(status().isOk()).andReturn();
        assertEquals(200,mvcResult.getResponse().getStatus());
        assertEquals(200,mvcResult1.getResponse().getStatus());
    }

    @Test
    void getAllFormsTest() throws Exception
    {
        List<Map<String,Object>> list=new ArrayList<>();
        Map<String,Object> map=new HashMap<>();
        map.put("create","true");
        list.add(map);
        InputStream inputStreamTest =new ClassPathResource(TEST_FORMS_DATA_1).getInputStream();
        ObjectMapper objectMapperTest=new ObjectMapper();
        FormSchema formSchemaTest=objectMapperTest.readValue(inputStreamTest,FormSchema.class);
        Mockito.when(mockTokenUtils.getIssuerFromToken(TOKEN)).thenReturn(TENANT);
        Mockito.when(mockFormService.getAllRuntimeForms(true, TYPE_FORM)).thenReturn(Stream.of(
                new FormResponseSchema(),
                new FormResponseSchema()));
        RequestBuilder requestBuilderTest = MockMvcRequestBuilders.get(BASE_URL + VERSION_V1_TEST + FORMS_URL).param(INCLUDE_CONTENT, String.valueOf(true)).param(TYPE,FORM)
                .content(objectMapperTest.writeValueAsString(formSchemaTest))
                .with(jwtRead)
                .contentType(MediaType.APPLICATION_JSON);
        RequestBuilder requestBuilderTest1 = MockMvcRequestBuilders.get(BASE_URL + VERSION_V1_TEST + FORMS_URL).param(INCLUDE_CONTENT, String.valueOf(true)).param(TYPE,"component")
                .content(objectMapperTest.writeValueAsString(formSchemaTest))
                .with(jwtRead)
                .contentType(MediaType.APPLICATION_JSON);
        MvcResult mvcResult = this.mockMvc.perform(requestBuilderTest).andExpect(status().isOk()).andReturn();
        MvcResult mvcResult1 = this.mockMvc.perform(requestBuilderTest1).andExpect(status().isOk()).andReturn();
        assertEquals(200,mvcResult.getResponse().getStatus());
        assertEquals(200,mvcResult1.getResponse().getStatus());
    }

    @Test
    void deleteFormTest() throws Exception
    {
        Mockito.when(mockTokenUtils.getIssuerFromToken(TOKEN)).thenReturn(TENANT);
        RequestBuilder requestBuilderTest=MockMvcRequestBuilders
                .delete(BASE_URL + VERSION_V1 + FORM_BY_ID_URL,1)
                .with(jwtDelete);
        MvcResult mvcResult = this.mockMvc.perform(requestBuilderTest).andExpect(status().isOk()).andReturn();
        assertEquals(200,mvcResult.getResponse().getStatus());
    }

    @Test
    void deleteRuntimeFormById() throws Exception {
        Mockito.when(mockTokenUtils.getIssuerFromToken(TOKEN)).thenReturn(TENANT);
        Mockito.when(mockFormService.deleteRuntimeFormById("1")).thenReturn(true);
        RequestBuilder requestBuilderTest=MockMvcRequestBuilders
                .delete(BASE_URL + VERSION_V1 + FORM_BY_ID_URL,1)
                .with(jwtDelete);
        MvcResult mvcResult = this.mockMvc.perform(requestBuilderTest).andExpect(status().isOk()).andReturn();
        assertEquals(200,mvcResult.getResponse().getStatus());
    }

    @Test
    void searchFormByIdOrNameLike() throws Exception
    {
        List<Map<String,Object>> list=new ArrayList<>();
        Map<String,Object> map=new HashMap<>();
        map.put("create","true");
        list.add(map);
        Mockito.when(mockTokenUtils.getIssuerFromToken(TOKEN)).thenReturn(TENANT);
        InputStream inputStreamTest =new ClassPathResource(TEST_FORMS_DATA_1).getInputStream();
        ObjectMapper objectMapperTest=new ObjectMapper();
        FormSchema formSchemaTest=objectMapperTest.readValue(inputStreamTest,FormSchema.class);
        Mockito.when(mockFormService.searchRuntimeFormByIdOrNameLike(TEST_ID, TYPE_FORM)).thenReturn(Stream.of(
                new FormResponseSchema(),
                new FormResponseSchema()));
        RequestBuilder requestBuilderTest=MockMvcRequestBuilders.get(BASE_URL + VERSION_V1 + SEARCH_FORM_URL).param(ID_OR_NAME_LIKE, String.valueOf(1)).param(TYPE,FORM)
                .content(objectMapperTest.writeValueAsString(formSchemaTest))
                .with(jwtRead)
                .contentType(MediaType.APPLICATION_JSON);
        RequestBuilder requestBuilderTest1=MockMvcRequestBuilders.get(BASE_URL + VERSION_V1 + SEARCH_FORM_URL).param(ID_OR_NAME_LIKE, String.valueOf(1)).param(TYPE,"component")
                .content(objectMapperTest.writeValueAsString(formSchemaTest))
                .with(jwtRead)
                .contentType(MediaType.APPLICATION_JSON);
        MvcResult mvcResult = this.mockMvc.perform(requestBuilderTest).andExpect(status().isOk()).andReturn();
        MvcResult mvcResult1 = this.mockMvc.perform(requestBuilderTest1).andExpect(status().isOk()).andReturn();
        assertEquals(200,mvcResult.getResponse().getStatus());
        assertEquals(200,mvcResult1.getResponse().getStatus());
    }
}
