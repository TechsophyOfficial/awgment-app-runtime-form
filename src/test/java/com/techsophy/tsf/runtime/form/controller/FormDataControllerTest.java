package com.techsophy.tsf.runtime.form.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techsophy.tsf.commons.acl.ACLDecision;
import com.techsophy.tsf.commons.acl.ACLEvaluatorImpl;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.controller.impl.FormDataControllerImpl;
import com.techsophy.tsf.runtime.form.dto.*;
import com.techsophy.tsf.runtime.form.entity.FormDataDefinition;
import com.techsophy.tsf.runtime.form.exception.ACLException;
import com.techsophy.tsf.runtime.form.exception.FormIdNotFoundException;
import com.techsophy.tsf.runtime.form.exception.InvalidInputException;
import com.techsophy.tsf.runtime.form.exception.UserDetailsIdNotFoundException;
import com.techsophy.tsf.runtime.form.model.ApiResponse;
import com.techsophy.tsf.runtime.form.service.FormDataService;
import com.techsophy.tsf.runtime.form.service.impl.FormAclServiceImpl;
import com.techsophy.tsf.runtime.form.utils.RelationUtils;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import java.io.InputStream;
import java.util.*;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class})
class FormDataControllerTest
{
    @Mock
    ACLEvaluatorImpl aclEvaluatorImpl;
    @Mock
    FormDataService formDataService ;
    @Mock
    GlobalMessageSource globalMessageSource;
    @Mock
    FormAclServiceImpl formAclServiceImpl;
    @Mock
    TokenUtils tokenUtils;
    @Mock
    RelationUtils relationUtils;
    @InjectMocks
    FormDataControllerImpl formDataController;

    @Test
    void userDetailsNotFoundExceptionTest() throws Exception {
        InputStream inputStreamTest = new ClassPathResource(TEST_RUNTIME_FORM_DATA_1).getInputStream();
        ObjectMapper objectMapperTest = new ObjectMapper();
        FormDataSchema formDataSchemaTest = objectMapperTest.readValue(inputStreamTest, FormDataSchema.class);
        Mockito.when(formDataService.saveFormData(any(),anyString(),anyString(),anyList())).thenThrow(new UserDetailsIdNotFoundException(errorCode, USER_DETAILS_NOT_FOUND_WITH_GIVEN_ID));
        Assertions.assertThrows(UserDetailsIdNotFoundException.class, () -> formDataController.saveFormData(formDataSchemaTest,"formData.name:akhil"));
    }

    @Test
    void FormIdNotFoundExceptionTest() throws Exception {
        InputStream inputStreamTest = new ClassPathResource(TEST_RUNTIME_FORM_DATA_1).getInputStream();
        ObjectMapper objectMapperTest = new ObjectMapper();
        FormDataSchema formDataSchemaTest = objectMapperTest.readValue(inputStreamTest, FormDataSchema.class);
        Mockito.when(formDataService.saveFormData(any(),anyString(),anyString(),anyList())).thenThrow(new FormIdNotFoundException(errorCode, USER_DETAILS_NOT_FOUND_WITH_GIVEN_ID));
        Assertions.assertThrows(FormIdNotFoundException.class, () -> formDataController.saveFormData(formDataSchemaTest,"formData.name:akhil"));
    }

    @Test
    void InvalidInputExceptionTest() throws Exception {
        InputStream inputStreamTest = new ClassPathResource(TEST_RUNTIME_FORM_DATA_1).getInputStream();
        ObjectMapper objectMapperTest = new ObjectMapper();
        FormDataSchema formDataSchemaTest = objectMapperTest.readValue(inputStreamTest, FormDataSchema.class);
        Mockito.when(formDataService.saveFormData(any(),anyString(),anyString(),anyList())).thenThrow(new InvalidInputException(errorCode, USER_DETAILS_NOT_FOUND_WITH_GIVEN_ID));
        Assertions.assertThrows(InvalidInputException.class, () -> formDataController.saveFormData(formDataSchemaTest,"formData.name:akhil"));
    }

    @Test
    void saveFormDataExceptionTest()
    {
        FormDataSchema formDataSchema=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,TEST_FORM_DATA,TEST_FORM_META_DATA);
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setFormId("101");
        formAclDto.setAclId("101");
        Mockito.when(formAclServiceImpl.getFormAcl(anyString())).thenReturn(formAclDto);
        ACLDecision aclDecision=new ACLDecision("deny",null);
        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("test-token");
        Mockito.when(aclEvaluatorImpl.getUpdate(anyString(),anyString(),any())).thenReturn(aclDecision);
        Assertions.assertThrows(ACLException.class, () -> formDataController.saveFormData(formDataSchema,"formData.name:akhil"));
    }

    @Test
    void updateFormDataExceptionTest()
    {
        FormDataSchema formDataSchema=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,TEST_FORM_DATA,TEST_FORM_META_DATA);
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setFormId("101");
        formAclDto.setAclId("101");
        Mockito.when(formAclServiceImpl.getFormAcl(any())).thenReturn(formAclDto);
        ACLDecision aclDecision=new ACLDecision("deny",null);
        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("test-token");
        Mockito.when(aclEvaluatorImpl.getUpdate(anyString(),anyString(),any())).thenReturn(aclDecision);
        Assertions.assertThrows(ACLException.class, () -> formDataController.updateFormData(formDataSchema,"formData.name:akhil"));
    }

    @Test
    void deleteAllFormDataByFormIdExceptionTest()
    {
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setAclId("101");
        formAclDto.setFormId("101");
        Mockito.when(formAclServiceImpl.getFormAcl(anyString())).thenReturn(formAclDto);
        ACLDecision aclDecision=new ACLDecision("deny",null);
        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("test-token");
        Mockito.when(aclEvaluatorImpl.getDelete(anyString(),anyString(),any())).thenReturn(aclDecision);
        Assertions.assertThrows(ACLException.class, () -> formDataController.deleteAllFormDataByFormId("101"));
    }

    @Test
    void deleteFormDataByFormIdAndIdExceptionTest()
    {
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setAclId("101");
        formAclDto.setFormId("101");
        Mockito.when(formAclServiceImpl.getFormAcl(anyString())).thenReturn(formAclDto);
        ACLDecision aclDecision=new ACLDecision("deny",null);
        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("test-token");
        Mockito.when(aclEvaluatorImpl.getDelete(anyString(),anyString(),any())).thenReturn(aclDecision);
        Assertions.assertThrows(ACLException.class, () -> formDataController.deleteFormDataByFormIdAndId("101", "201","formData.name:akhil"));
    }

    @Test
    void getFormDataByFormIdAndIdExceptionTest()
    {
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setAclId("101");
        formAclDto.setFormId("101");
        Mockito.when(formAclServiceImpl.getFormAcl(anyString())).thenReturn(formAclDto);
        ACLDecision aclDecision=new ACLDecision("deny",null);
        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("test-token");
        Mockito.when(aclEvaluatorImpl.getRead(anyString(),anyString(),any())).thenReturn(aclDecision);
        Assertions.assertThrows(ACLException.class, () -> formDataController.getFormDataByFormIdAndId("101", "201", "994102731543871488:orderId,994122561634369536:parcelId"));
    }

    @Test
    void saveFormDataWithoutACLTest() throws Exception
    {
        FormDataSchema formDataSchema=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,TEST_FORM_DATA,TEST_FORM_META_DATA);
        Mockito.when(formDataService.saveFormData(any(),anyString(),anyString(),anyList())).thenReturn(new FormDataDefinition());
        formDataController.saveFormData(formDataSchema,"formData.name:akhil");
        verify(formDataService,times(1)).saveFormData(formDataSchema,"formData.name:akhil", EMPTY_STRING,List.of());
    }

    @Test
    void saveFormDataWithACLTest() throws Exception
    {
        FormDataSchema formDataSchema=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,TEST_FORM_DATA,TEST_FORM_META_DATA);
        Mockito.when(formDataService.saveFormData(any(),anyString(),anyString(),anyList())).thenReturn(new FormDataDefinition());
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setId("1");
        formAclDto.setAclId("101");
        formAclDto.setFormId("101");
        Mockito.when(formAclServiceImpl.getFormAcl(anyString())).thenReturn(formAclDto);
        Map<String,Object> additionalDetails=new HashMap<>();
        Map<String,String> filtersMap=new HashMap<>();
        filtersMap.put("filters","{\\\"formData.orderId\\\":{\\\"equals\\\" : 994192119303684096},\\\"formData.customerName\\\":{\\\"like\\\":\\\"customer\\\"}}");
        additionalDetails.put("runtime-form-app",filtersMap);
        ACLDecision aclDecision=new ACLDecision("allow",additionalDetails);
        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("test-token");
        Mockito.when(aclEvaluatorImpl.getUpdate(anyString(),anyString(),any())).thenReturn(aclDecision);
        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("test-token");
        formDataController.saveFormData(formDataSchema,"formData.name:akhil");
        verify(formDataService,times(1)).saveFormData(formDataSchema,"formData.name:akhil","{\\\"formData.orderId\\\":{\\\"equals\\\" : 994192119303684096},\\\"formData.customerName\\\":{\\\"like\\\":\\\"customer\\\"}}",List.of());
    }

    @Test
    void saveFormDataWithInCorrectACLFormatTest()
    {
        FormDataSchema formDataSchema=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,TEST_FORM_DATA,TEST_FORM_META_DATA);
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setId("1");
        formAclDto.setAclId("101");
        formAclDto.setFormId("101");
        Mockito.when(formAclServiceImpl.getFormAcl(anyString())).thenReturn(formAclDto);
        Map<String,Object> additionalDetails=new HashMap<>();
        Map<String,String> filtersMap=new HashMap<>();
        additionalDetails.put("runtime-form-app",filtersMap);
        ACLDecision aclDecision=new ACLDecision("allow",additionalDetails);
        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("test-token");
        Mockito.when(aclEvaluatorImpl.getUpdate(anyString(),anyString(),any())).thenReturn(aclDecision);
        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("test-token");
        Assertions.assertThrows(NoSuchElementException.class,()->formDataController.saveFormData(formDataSchema,"formData.name:akhil"));
    }


    @Test
    void updateFormDataTest() throws Exception
    {
        FormDataSchema formDataSchema=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,TEST_FORM_DATA,TEST_FORM_META_DATA);
        Mockito.when(formDataService.updateFormData(any(),anyString(),anyString(),anyList())).thenReturn(new FormDataDefinition());
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setId("1");
        formAclDto.setAclId("101");
        formAclDto.setFormId("101");
        Mockito.when(formAclServiceImpl.getFormAcl(anyString())).thenReturn(formAclDto);
        Map<String,Object> additionalDetails=new HashMap<>();
        Map<String,String> filtersMap=new HashMap<>();
        filtersMap.put("filters","{\\\"formData.orderId\\\":{\\\"equals\\\" : 994192119303684096},\\\"formData.customerName\\\":{\\\"like\\\":\\\"customer\\\"}}");
        additionalDetails.put("runtime-form-app",filtersMap);
        ACLDecision aclDecision=new ACLDecision("allow",additionalDetails);
        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("test-token");
        Mockito.when(aclEvaluatorImpl.getUpdate(anyString(),anyString(),any())).thenReturn(aclDecision);
        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("test-token");
        formDataController.updateFormData(formDataSchema,"formData.name:akhil");
        verify(formDataService,times(1)).updateFormData(formDataSchema,"formData.name:akhil", "{\\\"formData.orderId\\\":{\\\"equals\\\" : 994192119303684096},\\\"formData.customerName\\\":{\\\"like\\\":\\\"customer\\\"}}",List.of());
    }

    @Test
    void getAllFormDataByFormIdFilterTest() throws JsonProcessingException
    {
        List<FormDataResponseSchema> formDataResponseSchemaList=new ArrayList<>();
        Mockito.when(formDataService.getAllFormDataByFormId(anyString(),anyString(),any(),any(),any(), anyString(),anyList())).thenReturn(formDataResponseSchemaList);
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setId("1");
        formAclDto.setAclId("101");
        formAclDto.setFormId("101");
        Mockito.when(formAclServiceImpl.getFormAcl(anyString())).thenReturn(formAclDto);
        Map<String,Object> additionalDetails=new HashMap<>();
        Map<String,String> filtersMap=new HashMap<>();
        filtersMap.put("filters","{\\\"formData.orderId\\\":{\\\"equals\\\" : 994192119303684096},\\\"formData.customerName\\\":{\\\"like\\\":\\\"customer\\\"}}");
        additionalDetails.put("runtime-form-app",filtersMap);
        ACLDecision aclDecision=new ACLDecision("allow",additionalDetails);
        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("test-token");
        Mockito.when(aclEvaluatorImpl.getRead(anyString(),anyString(),any())).thenReturn(aclDecision);
        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("test-token");
        ApiResponse apiResponse=new ApiResponse(new ArrayList<>(),true,"Form data retrieved successfully");
        Mockito.when(globalMessageSource.get(anyString())).thenReturn("Form data retrieved successfully");
        Assertions.assertEquals(apiResponse,formDataController.getAllFormDataByFormId("101","994102731543871488:orderId,994122561634369536:parcelId",null,null,null,null,"formData.name:akhil",null));
    }

    @Test
    void getAllFormDataByFormIdOrFilterTest() throws JsonProcessingException
    {
        List<FormDataResponseSchema> formDataResponseSchemaList=new ArrayList<>();
        Mockito.when(formDataService.getAllFormDataByFormId(anyString(),anyString(),any(),any(),any(), anyString(),anyList())).thenReturn(formDataResponseSchemaList);
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setId("1");
        formAclDto.setAclId("101");
        formAclDto.setFormId("101");
        Mockito.when(formAclServiceImpl.getFormAcl(anyString())).thenReturn(formAclDto);
        Map<String,Object> additionalDetails=new HashMap<>();
        Map<String,Object> filtersMap=new HashMap<>();
        filtersMap.put("orFilters",List.of("{\\\"formData.orderId\\\":{\\\"equals\\\" : 994192119303684096},\\\"formData.customerName\\\":{\\\"like\\\":\\\"customer\\\"}}"));
        additionalDetails.put("runtime-form-app",filtersMap);
        ACLDecision aclDecision=new ACLDecision("allow",additionalDetails);
        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("test-token");
        Mockito.when(aclEvaluatorImpl.getRead(anyString(),anyString(),any())).thenReturn(aclDecision);
        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("test-token");
        ApiResponse apiResponse=new ApiResponse(new ArrayList<>(),true,"Form data retrieved successfully");
        Mockito.when(globalMessageSource.get(anyString())).thenReturn("Form data retrieved successfully");

        Assertions.assertEquals(apiResponse,formDataController.getAllFormDataByFormId("101","994102731543871488:orderId,994122561634369536:parcelId",null,null,null,null,"formData.name:akhil",null));
    }
    @Test
    void getAllFormDataByFormIdFilterPaginationTest() throws JsonProcessingException
    {
        PaginationResponsePayload paginationResponsePayload=new PaginationResponsePayload();
        Mockito.when(formDataService.getAllFormDataByFormId(anyString(),anyString(),any(),any(),any(),any(),anyString(),anyList())).thenReturn(paginationResponsePayload);
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setId("1");
        formAclDto.setAclId("101");
        formAclDto.setFormId("101");
        Mockito.when(formAclServiceImpl.getFormAcl(anyString())).thenReturn(formAclDto);
        Map<String,Object> additionalDetails=new HashMap<>();
        Map<String,String> filtersMap=new HashMap<>();
        filtersMap.put("filters","{\\\"formData.orderId\\\":{\\\"equals\\\" : 994192119303684096},\\\"formData.customerName\\\":{\\\"like\\\":\\\"customer\\\"}}");
        additionalDetails.put("runtime-form-app",filtersMap);
        ACLDecision aclDecision=new ACLDecision("allow",additionalDetails);
        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("test-token");
        Mockito.when(aclEvaluatorImpl.getRead(anyString(),anyString(),any())).thenReturn(aclDecision);
        ApiResponse apiResponse=new ApiResponse(paginationResponsePayload,true,"Form data retrieved successfully");
        Mockito.when(globalMessageSource.get(anyString())).thenReturn("Form data retrieved successfully");
        Assertions.assertEquals(apiResponse,formDataController.getAllFormDataByFormId("101","994102731543871488:orderId,994122561634369536:parcelId",0,5,null,null,"formData.name:akhil",null));
    }

    @Test
    void getAllFormDataByFormIdFilterSortTest() throws JsonProcessingException
    {
        PaginationResponsePayload paginationResponsePayload=new PaginationResponsePayload();
        Mockito.when(formDataService.getAllFormDataByFormId(anyString(),anyString(),any(),any(),any(),any(),anyString(),anyList())).thenReturn(paginationResponsePayload);
        ApiResponse apiResponse=new ApiResponse(paginationResponsePayload,true,"Form data retrieved successfully");
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setId("1");
        formAclDto.setAclId("101");
        formAclDto.setFormId("101");
        Mockito.when(formAclServiceImpl.getFormAcl(anyString())).thenReturn(formAclDto);
        Map<String,Object> additionalDetails=new HashMap<>();
        Map<String,String> filtersMap=new HashMap<>();
        filtersMap.put("filters","{\\\"formData.orderId\\\":{\\\"equals\\\" : 994192119303684096},\\\"formData.customerName\\\":{\\\"like\\\":\\\"customer\\\"}}");
        additionalDetails.put("runtime-form-app",filtersMap);
        ACLDecision aclDecision=new ACLDecision("allow",additionalDetails);
        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("test-token");
        Mockito.when(aclEvaluatorImpl.getRead(anyString(),anyString(),any())).thenReturn(aclDecision);
        Mockito.when(globalMessageSource.get(anyString())).thenReturn("Form data retrieved successfully");
        Assertions.assertEquals(apiResponse,formDataController.getAllFormDataByFormId("101","994102731543871488:orderId,994122561634369536:parcelId",0,5,null,null,"formData.name:akhil",null));
    }

    @Test
    void getAllFormDataByFormIdSortWithoutACLTest() throws JsonProcessingException
    {
        List<FormDataResponseSchema> formDataResponseSchemaList=new ArrayList<>();
        Mockito.when(formDataService.getAllFormDataByFormIdAndQ(anyString(),anyString(),any(),anyString(),anyString(), any(),anyList())).thenReturn(formDataResponseSchemaList);
        ApiResponse apiResponse=new ApiResponse(formDataResponseSchemaList,true,"Form data retrieved successfully");
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setId("1");
        formAclDto.setAclId("101");
        formAclDto.setFormId("101");
        Mockito.when(formAclServiceImpl.getFormAcl(anyString())).thenReturn(formAclDto);
        Map<String,Object> additionalDetails=new HashMap<>();
        ACLDecision aclDecision=new ACLDecision("allow",additionalDetails);
        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("test-token");
        Mockito.when(aclEvaluatorImpl.getRead(anyString(),anyString(),any())).thenReturn(aclDecision);
        Mockito.when(globalMessageSource.get(anyString())).thenReturn("Form data retrieved successfully");
        Assertions.assertEquals(apiResponse,formDataController.getAllFormDataByFormId("101","994102731543871488:orderId,994122561634369536:parcelId",null,null,CREATED_ON,DESCENDING,EMPTY_STRING,null));
    }

    @Test
    void getAllFormDataByFormIdSortPaginationTest() throws JsonProcessingException
    {
        PaginationResponsePayload paginationResponsePayload=new PaginationResponsePayload();
        Mockito.when(formDataService.getAllFormDataByFormIdAndQ(anyString(),anyString(),any(),anyString(),anyString(),any(),anyString(),anyList())).thenReturn(paginationResponsePayload);
        ApiResponse apiResponse=new ApiResponse(paginationResponsePayload,true,"Form data retrieved successfully");
        Mockito.when(globalMessageSource.get(anyString())).thenReturn("Form data retrieved successfully");
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setId("1");
        formAclDto.setAclId("102");
        formAclDto.setFormId("101");
        Mockito.when(formAclServiceImpl.getFormAcl(anyString())).thenReturn(formAclDto);
        Map<String,Object> additionalDetails=new HashMap<>();
        Map<String,String> filtersMap=new HashMap<>();
        filtersMap.put("filters","{\\\"formData.orderId\\\":{\\\"equals\\\" : 994192119303684096},\\\"formData.customerName\\\":{\\\"like\\\":\\\"customer\\\"}}");
        additionalDetails.put("runtime-form-app",filtersMap);
        ACLDecision aclDecision=new ACLDecision("allow",additionalDetails);
        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("test-token");
        Mockito.when(aclEvaluatorImpl.getRead(anyString(),anyString(),any())).thenReturn(aclDecision);
        Assertions.assertEquals(apiResponse,formDataController.getAllFormDataByFormId("101","994102731543871488:orderId,994122561634369536:parcelId",0,5,CREATED_ON,DESCENDING,EMPTY_STRING,null));
    }

    @Test
    void getAllFormDataByFormIdRelationsTest() throws JsonProcessingException {
        PaginationResponsePayload paginationResponsePayload=new PaginationResponsePayload();
        Mockito.when(formDataService.getAllFormDataByFormId(anyString(),anyString(),anyString(),anyList())).thenReturn(paginationResponsePayload);
        ApiResponse apiResponse=new ApiResponse(paginationResponsePayload,true,"Form data retrieved successfully");
        Mockito.when(globalMessageSource.get(anyString())).thenReturn("Form data retrieved successfully");
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setId("1");
        formAclDto.setAclId("102");
        formAclDto.setFormId("101");
        Mockito.when(formAclServiceImpl.getFormAcl(anyString())).thenReturn(formAclDto);
        Map<String,Object> additionalDetails=new HashMap<>();
        Map<String,String> filtersMap=new HashMap<>();
        filtersMap.put("filters","{\\\"formData.orderId\\\":{\\\"equals\\\" : 994192119303684096},\\\"formData.customerName\\\":{\\\"like\\\":\\\"customer\\\"}}");
        additionalDetails.put("runtime-form-app",filtersMap);
        ACLDecision aclDecision=new ACLDecision("allow",additionalDetails);
        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("test-token");
        Mockito.when(aclEvaluatorImpl.getRead(anyString(),anyString(),any())).thenReturn(aclDecision);
        Assertions.assertEquals(apiResponse,formDataController.getAllFormDataByFormId("101","994102731543871488:orderId,994122561634369536:parcelId",null,null,null,null,EMPTY_STRING,null));
    }

    @Test
    void deleteAllFormDataByFormId()
    {
        FormAclDto formAclDto=new FormAclDto();
        formAclDto.setAclId("1");
        formAclDto.setFormId("101");
        Mockito.when(formAclServiceImpl.getFormAcl(anyString())).thenReturn(formAclDto);
        ApiResponse apiResponse=new ApiResponse(null,true,"Form data deleted successfully");
        Mockito.when(globalMessageSource.get(anyString())).thenReturn("Form data deleted successfully");
        Mockito.when(formAclServiceImpl.getFormAcl(anyString())).thenReturn(formAclDto);
        Map<String,Object> additionalDetails=new HashMap<>();
        Map<String,String> filtersMap=new HashMap<>();
        filtersMap.put("filters","{\\\"formData.orderId\\\":{\\\"equals\\\" : 994192119303684096},\\\"formData.customerName\\\":{\\\"like\\\":\\\"customer\\\"}}");
        additionalDetails.put("runtime-form-app",filtersMap);
        ACLDecision aclDecision=new ACLDecision("allow",additionalDetails);
        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("test-token");
        Mockito.when(aclEvaluatorImpl.getDelete(anyString(),anyString(),any())).thenReturn(aclDecision);
        Assertions.assertEquals(apiResponse,formDataController.deleteAllFormDataByFormId("101"));
    }

    @Test
    void deleteFormDataByFormIdAndId()
    {
        FormAclDto formAclDto=new FormAclDto();
        formAclDto.setAclId("1");
        formAclDto.setFormId("101");
        Mockito.when(formAclServiceImpl.getFormAcl(anyString())).thenReturn(formAclDto);
        ApiResponse apiResponse=new ApiResponse(null,true,"Form data deleted successfully");
        Mockito.when(globalMessageSource.get(anyString())).thenReturn("Form data deleted successfully");
        Map<String,Object> additionalDetails=new HashMap<>();
        Map<String,String> filtersMap=new HashMap<>();
        filtersMap.put("filters","{\\\"formData.orderId\\\":{\\\"equals\\\" : 994192119303684096},\\\"formData.customerName\\\":{\\\"like\\\":\\\"customer\\\"}}");
        additionalDetails.put("runtime-form-app",filtersMap);
        ACLDecision aclDecision=new ACLDecision("allow",additionalDetails);
        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("test-token");
        Mockito.when(aclEvaluatorImpl.getDelete(anyString(),anyString(),any())).thenReturn(aclDecision);
        Assertions.assertEquals(apiResponse,formDataController.deleteFormDataByFormIdAndId("101","201",null ));
    }

    @Test
    void getFormDataByFormIdAndIdTest()
    {
        FormAclDto formAclDto=new FormAclDto();
        formAclDto.setAclId("1");
        formAclDto.setFormId("101");
        Mockito.when(formAclServiceImpl.getFormAcl(anyString())).thenReturn(formAclDto);
        List<FormDataResponseSchema> formDataResponseSchemaList=new ArrayList<>();
        ApiResponse apiResponse=new ApiResponse(formDataResponseSchemaList,true,"Form data retrieved successfully");
        Mockito.when(globalMessageSource.get(anyString())).thenReturn("Form data retrieved successfully");
        Map<String,Object> additionalDetails=new HashMap<>();
        Map<String,String> filtersMap=new HashMap<>();
        filtersMap.put("filters","{\\\"formData.orderId\\\":{\\\"equals\\\" : 994192119303684096},\\\"formData.customerName\\\":{\\\"like\\\":\\\"customer\\\"}}");
        additionalDetails.put("runtime-form-app",filtersMap);
        ACLDecision aclDecision=new ACLDecision("allow",additionalDetails);
        Mockito.when(aclEvaluatorImpl.getRead(anyString(),anyString(),any())).thenReturn(aclDecision);
        Mockito.when(tokenUtils.getTokenFromContext()).thenReturn("test-token");
        Assertions.assertEquals(apiResponse,formDataController.getFormDataByFormIdAndId("101","201","994102731543871488:orderId,994122561634369536:parcelId"));
    }

    @Test
    void aggregateByFormIdFilterGroupByTest() throws JsonProcessingException
    {
        Mockito.when(globalMessageSource.get(anyString())).thenReturn("Form data retrieved successfully");
        AggregationResponse aggregationResponse=new AggregationResponse(new ArrayList<>());
        Mockito.when(formDataService.aggregateByFormIdFilterGroupBy(any(),anyString(),any(),anyString())).thenReturn(aggregationResponse);
        ApiResponse apiResponse=new ApiResponse(aggregationResponse,true,"Form data retrieved successfully");
        Assertions.assertEquals(apiResponse,formDataController.aggregateByFormIdFilterGroupBy("101","formData.name:akhil","formData.name","groupBy"));
    }
}
