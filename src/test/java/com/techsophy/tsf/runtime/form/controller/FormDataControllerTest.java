package com.techsophy.tsf.runtime.form.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.techsophy.tsf.commons.ACLDecision;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.controller.impl.FormDataControllerImpl;
import com.techsophy.tsf.runtime.form.dto.*;
import com.techsophy.tsf.runtime.form.model.ApiResponse;
import com.techsophy.tsf.runtime.form.service.FormAclService;
import com.techsophy.tsf.runtime.form.service.FormDataService;
import com.techsophy.tsf.runtime.form.utils.RelationUtils;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import org.junit.Rule;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FormDataControllerTest
{
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(
            wireMockConfig()
                    .dynamicPort()
                    .dynamicHttpsPort());
    @Mock
    TokenUtils tokenUtils;
    @Mock
    FormDataService formDataService ;
    @Mock
    GlobalMessageSource globalMessageSource;
    @Mock
    FormAclService mockFormACLService;
    @Mock
    RelationUtils mockRelationUtils;
    FormDataControllerImpl formDataController;

    WireMockServer wireMockServer ;

    @BeforeAll
    public void beforeTest()
    {
        wireMockServer = new WireMockServer();
        wireMockServer.start();
        wireMockServer.resetAll();
        commonStubs();
    }

    @BeforeEach
    public void beforeEach() {
        formDataController = new FormDataControllerImpl(globalMessageSource, formDataService, mockFormACLService, tokenUtils,mockRelationUtils, wireMockServer.baseUrl());
    }

    public void commonStubs()
    {
        stubFor(post("/accounts/v1/acl/2/evaluate").willReturn(okJson("{\n" +
                "    \"data\": {\n" +
                "        \"name\": \"aclRule\",\n" +
                "        \"read\": {\n" +
                "            \"decision\": \"deny\",\n" +
                "            \"additionalDetails\": null\n" +
                "        },\n" +
                "        \"update\": {\n" +
                "            \"decision\": \"allow\",\n" +
                "            \"additionalDetails\": null\n" +
                "        },\n" +
                "        \"delete\": {\n" +
                "            \"decision\": \"allow\",\n" +
                "            \"additionalDetails\": null\n" +
                "        }\n" +
                "    },\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"ACL evaluated successfully\"\n" +
                "}").withStatus(200)));
        stubFor(post("/accounts/v1/acl/1/evaluate").willReturn(okJson("{\n" +
                "    \"data\": {\n" +
                "        \"name\": \"aclRule\",\n" +
                "        \"read\": {\n" +
                "            \"decision\": \"allow\",\n" +
                "            \"additionalDetails\": null\n" +
                "        },\n" +
                "        \"update\": {\n" +
                "            \"decision\": \"allow\",\n" +
                "            \"additionalDetails\": null\n" +
                "        },\n" +
                "        \"delete\": {\n" +
                "            \"decision\": \"allow\",\n" +
                "            \"additionalDetails\": null\n" +
                "        }\n" +
                "    },\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"ACL evaluated successfully\"\n" +
                "}").withStatus(200)));
    }

    @AfterAll
    public void teardown() {
        wireMockServer.shutdownServer();
    }

    @Test
    void saveFormDataTest() throws Exception
    {
        Map<String,Object> formData=new HashMap<>();
        Map<String,Object> formMetaData=new HashMap<>();
        FormDataSchema formDataSchema = new FormDataSchema("101","201",1,formData,formMetaData);
        FormDataResponse formDataResponse=new FormDataResponse("101",1);
        Mockito.when(formDataService.saveFormData(formDataSchema)).thenReturn(formDataResponse);
        formDataController.saveFormData(formDataSchema);
        verify(formDataService,times(1)).saveFormData(formDataSchema);
    }

    @Test
    void updateFormData() throws Exception
    {
        Map<String,Object> formData=new HashMap<>();
        Map<String,Object> formMetaData=new HashMap<>();
        FormDataSchema formDataSchema = new FormDataSchema("1","1",1,formData,formMetaData);
        FormAclDto formAclDto=new FormAclDto();
        formAclDto.setFormId("101");
        formAclDto.setAclId("1");
        Mockito.when(mockFormACLService.getFormAcl(any())).thenReturn(formAclDto);
        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("abc");
        ACLDecision aclDecision=new ACLDecision();
        aclDecision.setDecision("deny");
        aclDecision.setAdditionalDetails(null);
        FormDataResponse formDataResponse=new FormDataResponse("1",1);
        ApiResponse apiResponse=new ApiResponse(formDataResponse,true,"Form data updated successfully");
        Mockito.when(formDataService.updateFormData(formDataSchema)).thenReturn(formDataResponse);
        Mockito.when(globalMessageSource.get(anyString())).thenReturn("Form data updated successfully");
        Assertions.assertEquals(apiResponse,formDataController.updateFormData(formDataSchema));
    }

    @Test
    void getAllFormDataByFormIdFilterTest()
    {
        List<FormDataResponseSchema> formDataResponseSchemaList=new ArrayList<>();
        Mockito.when(formDataService.getAllFormDataByFormId(anyString(),anyString(),any(),any(),any())).thenReturn(formDataResponseSchemaList);
        ApiResponse apiResponse=new ApiResponse(new ArrayList<>(),true,"Form data retrieved successfully");
        Mockito.when(globalMessageSource.get(anyString())).thenReturn("Form data retrieved successfully");
        Assertions.assertEquals(apiResponse,formDataController.getAllFormDataByFormId("101","994102731543871488:orderId,994122561634369536:parcelId",null,null,null,null,"formData.name:akhil",null));
    }

    @Test
    void getAllFormDataByFormIdFilterPaginationTest()
    {
        PaginationResponsePayload paginationResponsePayload=new PaginationResponsePayload();
        Mockito.when(formDataService.getAllFormDataByFormId(anyString(),anyString(),any(),any(),any(),any())).thenReturn(paginationResponsePayload);
        ApiResponse apiResponse=new ApiResponse(paginationResponsePayload,true,"Form data retrieved successfully");
        Mockito.when(globalMessageSource.get(anyString())).thenReturn("Form data retrieved successfully");
        Assertions.assertEquals(apiResponse,formDataController.getAllFormDataByFormId("101","994102731543871488:orderId,994122561634369536:parcelId",0,5,null,null,"formData.name:akhil",null));
    }

    @Test
    void getAllFormDataByFormIdFilterSortTest()
    {
        PaginationResponsePayload paginationResponsePayload=new PaginationResponsePayload();
        Mockito.when(formDataService.getAllFormDataByFormId(anyString(),anyString(),any(),any(),any(),any())).thenReturn(paginationResponsePayload);
        ApiResponse apiResponse=new ApiResponse(paginationResponsePayload,true,"Form data retrieved successfully");
        Mockito.when(globalMessageSource.get(anyString())).thenReturn("Form data retrieved successfully");
        Assertions.assertEquals(apiResponse,formDataController.getAllFormDataByFormId("101","994102731543871488:orderId,994122561634369536:parcelId",0,5,null,null,"formData.name:akhil",null));
    }

    @Test
    void getAllFormDataByFormIdSortTest()
    {
        List<FormDataResponseSchema> formDataResponseSchemaList=new ArrayList<>();
        Mockito.when(formDataService.getAllFormDataByFormIdAndQ("101","994102731543871488:orderId,994122561634369536:parcelId",null,CREATED_ON,DESCENDING)).thenReturn(formDataResponseSchemaList);
        ApiResponse apiResponse=new ApiResponse(formDataResponseSchemaList,true,"Form data retrieved successfully");
        Mockito.when(globalMessageSource.get(anyString())).thenReturn("Form data retrieved successfully");
        Assertions.assertEquals(apiResponse,formDataController.getAllFormDataByFormId("101","994102731543871488:orderId,994122561634369536:parcelId",null,null,CREATED_ON,DESCENDING,EMPTY_STRING,null));
    }

    @Test
    void getAllFormDataByFormIdSortPaginationTest()
    {
        PaginationResponsePayload paginationResponsePayload=new PaginationResponsePayload();
        Mockito.when(formDataService.getAllFormDataByFormIdAndQ("101","994102731543871488:orderId,994122561634369536:parcelId",null,CREATED_ON,DESCENDING, PageRequest.of(0,5))).thenReturn(paginationResponsePayload);
        ApiResponse apiResponse=new ApiResponse(paginationResponsePayload,true,"Form data retrieved successfully");
        Mockito.when(globalMessageSource.get(anyString())).thenReturn("Form data retrieved successfully");
        Assertions.assertEquals(apiResponse,formDataController.getAllFormDataByFormId("101","994102731543871488:orderId,994122561634369536:parcelId",0,5,CREATED_ON,DESCENDING,EMPTY_STRING,null));
    }

    @Test
    void getAllFormDataByFormIdRelationsTest()
    {
        PaginationResponsePayload paginationResponsePayload=new PaginationResponsePayload();
        Mockito.when(formDataService.getAllFormDataByFormId("101","994102731543871488:orderId,994122561634369536:parcelId")).thenReturn(paginationResponsePayload);
        ApiResponse apiResponse=new ApiResponse(paginationResponsePayload,true,"Form data retrieved successfully");
        Mockito.when(globalMessageSource.get(anyString())).thenReturn("Form data retrieved successfully");
        Assertions.assertEquals(apiResponse,formDataController.getAllFormDataByFormId("101","994102731543871488:orderId,994122561634369536:parcelId",null,null,null,null,EMPTY_STRING,null));
    }

    @Test
    void deleteAllFormDataByFormId()
    {
        FormAclDto formAclDto=new FormAclDto();
        formAclDto.setAclId("1");
        formAclDto.setFormId("101");
        Mockito.when(mockFormACLService.getFormAcl(anyString())).thenReturn(formAclDto);
        ApiResponse apiResponse=new ApiResponse(null,true,"Form data deleted successfully");
        Mockito.when(globalMessageSource.get(anyString())).thenReturn("Form data deleted successfully");
        Assertions.assertEquals(apiResponse,formDataController.deleteAllFormDataByFormId("101"));
    }

    @Test
    void deleteFormDataByFormIdAndId()
    {
        FormAclDto formAclDto=new FormAclDto();
        formAclDto.setAclId("1");
        formAclDto.setFormId("101");
        Mockito.when(mockFormACLService.getFormAcl(anyString())).thenReturn(formAclDto);
        ApiResponse apiResponse=new ApiResponse(null,true,"Form data deleted successfully");
        Mockito.when(globalMessageSource.get(anyString())).thenReturn("Form data deleted successfully");
        Assertions.assertEquals(apiResponse,formDataController.deleteFormDataByFormIdAndId("101","201"));
    }

    @Test
    void getFormDataByFormIdAndIdTest()
    {
        FormAclDto formAclDto=new FormAclDto();
        formAclDto.setAclId("1");
        formAclDto.setFormId("101");
        Mockito.when(mockFormACLService.getFormAcl(anyString())).thenReturn(formAclDto);
        List<FormDataResponseSchema> formDataResponseSchemaList=new ArrayList<>();
        ApiResponse apiResponse=new ApiResponse(formDataResponseSchemaList,true,"Form data retrieved successfully");
        Mockito.when(globalMessageSource.get(anyString())).thenReturn("Form data retrieved successfully");
        Assertions.assertEquals(apiResponse,formDataController.getFormDataByFormIdAndId("101","201","994102731543871488:orderId,994122561634369536:parcelId"));
    }

    @Test
    void aggregateByFormIdFilterGroupByTest()
    {
        Mockito.when(globalMessageSource.get(anyString())).thenReturn("Form data retrieved successfully");
        AggregationResponse aggregationResponse=new AggregationResponse(new ArrayList<>());
        Mockito.when(formDataService.aggregateByFormIdFilterGroupBy(any(),anyString(),any(),anyString())).thenReturn(aggregationResponse);
        ApiResponse apiResponse=new ApiResponse(aggregationResponse,true,"Form data retrieved successfully");
        Assertions.assertEquals(apiResponse,formDataController.aggregateByFormIdFilterGroupBy("101","formData.name:akhil","formData.name","groupBy"));
    }
}
