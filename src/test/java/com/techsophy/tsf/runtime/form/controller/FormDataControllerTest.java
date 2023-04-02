package com.techsophy.tsf.runtime.form.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.controller.impl.FormDataControllerImpl;
import com.techsophy.tsf.runtime.form.dto.*;
import com.techsophy.tsf.runtime.form.entity.FormDataDefinition;
import com.techsophy.tsf.runtime.form.exception.ACLException;
import com.techsophy.tsf.runtime.form.exception.FormIdNotFoundException;
import com.techsophy.tsf.runtime.form.exception.InvalidInputException;
import com.techsophy.tsf.runtime.form.exception.UserDetailsIdNotFoundException;
import com.techsophy.tsf.runtime.form.model.ApiResponse;
import com.techsophy.tsf.runtime.form.service.FormAclService;
import com.techsophy.tsf.runtime.form.service.FormDataService;
import com.techsophy.tsf.runtime.form.utils.RelationUtils;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FormDataControllerTest
{
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

        WireMock.configureFor(9090);
        wireMockServer = new WireMockServer(9090);


        wireMockServer.start();
        wireMockServer.resetAll();
        commonStubs();
    }

    @BeforeEach
    public void beforeEach() {

        String baseURL = wireMockServer.baseUrl();
        formDataController = new FormDataControllerImpl(globalMessageSource, formDataService, mockFormACLService, tokenUtils,mockRelationUtils, baseURL);
    }

    public void commonStubs()
    {
        stubFor(post("/accounts/v1/acl/2/evaluate").willReturn(ok(
                "{\"data\":{\"name\":\"aclRule\",\"read\":{\"decision\":\"deny\",\"additionalDetails\":null},\"update\":{\"decision\":\"allow\",\"additionalDetails\":null},\"delete\":{\"decision\":\"allow\",\"additionalDetails\":null}},\"success\":true,\"message\":\"ACL evaluated successfully\"}"
        ).withStatus(200)));
        stubFor(post("/accounts/v1/acl/1/evaluate").willReturn(okJson(
                "{\n" +
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
                        "}"
        ).withStatus(200)));
        stubFor(post("/accounts/v1/acl/101/evaluate").willReturn(okJson(
                "{\n" +
                        "    \"data\": {\n" +
                        "        \"name\": \"aclRule\",\n" +
                        "        \"read\": {\n" +
                        "            \"decision\": \"deny\",\n" +
                        "            \"additionalDetails\": null\n" +
                        "        },\n" +
                        "        \"update\": {\n" +
                        "            \"decision\": \"deny\",\n" +
                        "            \"additionalDetails\": null\n" +
                        "        },\n" +
                        "        \"delete\": {\n" +
                        "            \"decision\": \"deny\",\n" +
                        "            \"additionalDetails\": null\n" +
                        "        }\n" +
                        "    },\n" +
                        "    \"success\": true,\n" +
                        "    \"message\": \"ACL evaluated successfully\"\n" +
                        "}"
        ).withStatus(200)));
    }

    @AfterAll
    public void teardown() {
        wireMockServer.shutdown();
    }

    @Test
    void userDetailsNotFoundExceptionTest() throws Exception {
        InputStream inputStreamTest = new ClassPathResource(TEST_RUNTIME_FORM_DATA_1).getInputStream();
        ObjectMapper objectMapperTest = new ObjectMapper();
        FormDataSchema formDataSchemaTest = objectMapperTest.readValue(inputStreamTest, FormDataSchema.class);
        Mockito.when(formDataService.saveFormData(formDataSchemaTest)).thenThrow(new UserDetailsIdNotFoundException(errorCode, USER_DETAILS_NOT_FOUND_WITH_GIVEN_ID));
        Assertions.assertThrows(UserDetailsIdNotFoundException.class, () -> formDataController.saveFormData(formDataSchemaTest));
    }

    @Test
    void FormIdNotFoundExceptionTest() throws Exception {
        InputStream inputStreamTest = new ClassPathResource(TEST_RUNTIME_FORM_DATA_1).getInputStream();
        ObjectMapper objectMapperTest = new ObjectMapper();
        FormDataSchema formDataSchemaTest = objectMapperTest.readValue(inputStreamTest, FormDataSchema.class);
        Mockito.when(formDataService.saveFormData(formDataSchemaTest)).thenThrow(new FormIdNotFoundException(errorCode, USER_DETAILS_NOT_FOUND_WITH_GIVEN_ID));
        Assertions.assertThrows(FormIdNotFoundException.class, () -> formDataController.saveFormData(formDataSchemaTest));
    }

    @Test
    void InvalidInputExceptionTest() throws Exception {
        InputStream inputStreamTest = new ClassPathResource(TEST_RUNTIME_FORM_DATA_1).getInputStream();
        ObjectMapper objectMapperTest = new ObjectMapper();
        FormDataSchema formDataSchemaTest = objectMapperTest.readValue(inputStreamTest, FormDataSchema.class);
        Mockito.when(formDataService.saveFormData(formDataSchemaTest)).thenThrow(new InvalidInputException(errorCode, USER_DETAILS_NOT_FOUND_WITH_GIVEN_ID));
        Assertions.assertThrows(InvalidInputException.class, () -> formDataController.saveFormData(formDataSchemaTest));
    }

    @Test
    void saveFormDataExceptionTest() throws Exception {
        FormDataSchema formDataSchema=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,TEST_FORM_DATA,TEST_FORM_META_DATA);
        Mockito.when(formDataService.saveFormData(formDataSchema)).thenReturn(new FormDataDefinition());
        formDataController.saveFormData(formDataSchema);
        verify(formDataService, times(1)).saveFormData(formDataSchema);
    }

    @Test
    void updateFormDataExceptionTest() {
        FormDataSchema formDataSchema=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,TEST_FORM_DATA,TEST_FORM_META_DATA);
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setFormId("101");
        formAclDto.setAclId("101");
        Mockito.when(mockFormACLService.getFormAcl(any())).thenReturn(formAclDto);
        Assertions.assertThrows(ACLException.class, () -> formDataController.updateFormData(formDataSchema));
    }

    @Test
    void deleteAllFormDataByFormIdExceptionTest() {
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setAclId("101");
        formAclDto.setFormId("101");
        Mockito.when(mockFormACLService.getFormAcl(anyString())).thenReturn(formAclDto);
        Assertions.assertThrows(ACLException.class, () -> formDataController.deleteAllFormDataByFormId("101"));
    }

    @Test
    void deleteFormDataByFormIdAndIdExceptionTest() {
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setAclId("101");
        formAclDto.setFormId("101");
        Mockito.when(mockFormACLService.getFormAcl(anyString())).thenReturn(formAclDto);
        Assertions.assertThrows(ACLException.class, () -> formDataController.deleteFormDataByFormIdAndId("101", "201"));
    }

    @Test
    void getFormDataByFormIdAndIdExceptionTest() {
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setAclId("101");
        formAclDto.setFormId("101");
        Mockito.when(mockFormACLService.getFormAcl(anyString())).thenReturn(formAclDto);
        Assertions.assertThrows(ACLException.class, () -> formDataController.getFormDataByFormIdAndId("101", "201", "994102731543871488:orderId,994122561634369536:parcelId"));
    }
    @Test
    void saveFormDataTest() throws Exception
    {
        FormDataSchema formDataSchema=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,TEST_FORM_DATA,TEST_FORM_META_DATA);
        Mockito.when(formDataService.saveFormData(formDataSchema)).thenReturn(new FormDataDefinition());
        formDataController.saveFormData(formDataSchema);
        verify(formDataService,times(1)).saveFormData(formDataSchema);
    }

    @Test
    void updateFormDataTest() throws Exception
    {
        FormDataSchema formDataSchema=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,TEST_FORM_DATA,TEST_FORM_META_DATA);
        Mockito.when(formDataService.updateFormData(formDataSchema)).thenReturn(new FormDataDefinition());
        formDataController.updateFormData(formDataSchema);
        verify(formDataService,times(1)).updateFormData(formDataSchema);
    }

    @Test
    void getAllFormDataByFormIdFilterTest() throws JsonProcessingException {
        List<FormDataResponseSchema> formDataResponseSchemaList=new ArrayList<>();
        Mockito.when(formDataService.getAllFormDataByFormId(anyString(),anyString(),any(),any(),any())).thenReturn(formDataResponseSchemaList);
        ApiResponse apiResponse=new ApiResponse(new ArrayList<>(),true,"Form data retrieved successfully");
        Mockito.when(globalMessageSource.get(anyString())).thenReturn("Form data retrieved successfully");
        Assertions.assertEquals(apiResponse,formDataController.getAllFormDataByFormId("101","994102731543871488:orderId,994122561634369536:parcelId",null,null,null,null,"formData.name:akhil",null));
    }

    @Test
    void getAllFormDataByFormIdFilterPaginationTest() throws JsonProcessingException {
        PaginationResponsePayload paginationResponsePayload=new PaginationResponsePayload();
        Mockito.when(formDataService.getAllFormDataByFormId(anyString(),anyString(),any(),any(),any(),any())).thenReturn(paginationResponsePayload);
        ApiResponse apiResponse=new ApiResponse(paginationResponsePayload,true,"Form data retrieved successfully");
        Mockito.when(globalMessageSource.get(anyString())).thenReturn("Form data retrieved successfully");
        Assertions.assertEquals(apiResponse,formDataController.getAllFormDataByFormId("101","994102731543871488:orderId,994122561634369536:parcelId",0,5,null,null,"formData.name:akhil",null));
    }

    @Test
    void getAllFormDataByFormIdFilterSortTest() throws JsonProcessingException {
        PaginationResponsePayload paginationResponsePayload=new PaginationResponsePayload();
        Mockito.when(formDataService.getAllFormDataByFormId(anyString(),anyString(),any(),any(),any(),any())).thenReturn(paginationResponsePayload);
        ApiResponse apiResponse=new ApiResponse(paginationResponsePayload,true,"Form data retrieved successfully");
        Mockito.when(globalMessageSource.get(anyString())).thenReturn("Form data retrieved successfully");
        Assertions.assertEquals(apiResponse,formDataController.getAllFormDataByFormId("101","994102731543871488:orderId,994122561634369536:parcelId",0,5,null,null,"formData.name:akhil",null));
    }

    @Test
    void getAllFormDataByFormIdSortTest() throws JsonProcessingException {
        List<FormDataResponseSchema> formDataResponseSchemaList=new ArrayList<>();
        Mockito.when(formDataService.getAllFormDataByFormIdAndQ("101","994102731543871488:orderId,994122561634369536:parcelId",null,CREATED_ON,DESCENDING)).thenReturn(formDataResponseSchemaList);
        ApiResponse apiResponse=new ApiResponse(formDataResponseSchemaList,true,"Form data retrieved successfully");
        Mockito.when(globalMessageSource.get(anyString())).thenReturn("Form data retrieved successfully");
        Assertions.assertEquals(apiResponse,formDataController.getAllFormDataByFormId("101","994102731543871488:orderId,994122561634369536:parcelId",null,null,CREATED_ON,DESCENDING,EMPTY_STRING,null));
    }

    @Test
    void getAllFormDataByFormIdSortPaginationTest() throws JsonProcessingException {
        PaginationResponsePayload paginationResponsePayload=new PaginationResponsePayload();
        Mockito.when(formDataService.getAllFormDataByFormIdAndQ("101","994102731543871488:orderId,994122561634369536:parcelId",null,CREATED_ON,DESCENDING, PageRequest.of(0,5))).thenReturn(paginationResponsePayload);
        ApiResponse apiResponse=new ApiResponse(paginationResponsePayload,true,"Form data retrieved successfully");
        Mockito.when(globalMessageSource.get(anyString())).thenReturn("Form data retrieved successfully");
        Assertions.assertEquals(apiResponse,formDataController.getAllFormDataByFormId("101","994102731543871488:orderId,994122561634369536:parcelId",0,5,CREATED_ON,DESCENDING,EMPTY_STRING,null));
    }

    @Test
    void getAllFormDataByFormIdRelationsTest() throws JsonProcessingException {
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
    void aggregateByFormIdFilterGroupByTest() throws JsonProcessingException {
        Mockito.when(globalMessageSource.get(anyString())).thenReturn("Form data retrieved successfully");
        AggregationResponse aggregationResponse=new AggregationResponse(new ArrayList<>());
        Mockito.when(formDataService.aggregateByFormIdFilterGroupBy(any(),anyString(),any(),anyString())).thenReturn(aggregationResponse);
        ApiResponse apiResponse=new ApiResponse(aggregationResponse,true,"Form data retrieved successfully");
        Assertions.assertEquals(apiResponse,formDataController.aggregateByFormIdFilterGroupBy("101","formData.name:akhil","formData.name","groupBy"));
    }
}
