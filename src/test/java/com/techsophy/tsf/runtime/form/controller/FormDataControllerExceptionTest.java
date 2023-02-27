package com.techsophy.tsf.runtime.form.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.techsophy.tsf.commons.ACLDecision;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.controller.impl.FormDataControllerImpl;
import com.techsophy.tsf.runtime.form.dto.FormAclDto;
import com.techsophy.tsf.runtime.form.dto.FormDataResponse;
import com.techsophy.tsf.runtime.form.dto.FormDataSchema;
import com.techsophy.tsf.runtime.form.exception.ACLException;
import com.techsophy.tsf.runtime.form.exception.FormIdNotFoundException;
import com.techsophy.tsf.runtime.form.exception.InvalidInputException;
import com.techsophy.tsf.runtime.form.exception.UserDetailsIdNotFoundException;
import com.techsophy.tsf.runtime.form.service.FormAclService;
import com.techsophy.tsf.runtime.form.service.FormDataService;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import org.junit.Rule;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FormDataControllerExceptionTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(
            wireMockConfig()
                    .dynamicPort()
                    .dynamicHttpsPort());

    @Mock
    TokenUtils mockTokenUtils;
    @Mock
    FormDataService formDataService;
    @Mock
    GlobalMessageSource globalMessageSource;
    @Mock
    FormAclService mockFormACLService;
    FormDataController formDataController;
    WireMockServer wireMockServer;

    @BeforeAll
    public void beforeTest() {
        wireMockServer = new WireMockServer();
        wireMockServer.start();
        wireMockServer.resetAll();
        commonStubs();
    }

    @BeforeEach
    public void beforeEach() {
        formDataController = new FormDataControllerImpl(globalMessageSource, formDataService, mockFormACLService, mockTokenUtils, wireMockServer.baseUrl());
    }

    public void commonStubs() {
        stubFor(post("/accounts/v1/acl/1/evaluate").willReturn(okJson("{\n" +
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
                "}").withStatus(200)));
    }

    @AfterAll
    public void teardown() {
        wireMockServer.shutdownServer();
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
        Map<String, Object> formData = new HashMap<>();
        Map<String, Object> formMetaData = new HashMap<>();
        FormDataSchema formDataSchema = new FormDataSchema("101", "201", 1, formData, formMetaData);
        FormDataResponse formDataResponse = new FormDataResponse("101", 1);
        Mockito.when(formDataService.saveFormData(formDataSchema)).thenReturn(formDataResponse);
        formDataController.saveFormData(formDataSchema);
        verify(formDataService, times(1)).saveFormData(formDataSchema);
    }

    @Test
    void updateFormDataExceptionTest() {
        Map<String, Object> formData = new HashMap<>();
        Map<String, Object> formMetaData = new HashMap<>();
        FormDataSchema formDataSchema = new FormDataSchema("1", "1", 1, formData, formMetaData);
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setFormId("101");
        formAclDto.setAclId("1");
        Mockito.when(mockFormACLService.getFormAcl(any())).thenReturn(formAclDto);
        ACLDecision aclDecision = new ACLDecision();
        aclDecision.setDecision("deny");
        aclDecision.setAdditionalDetails(null);
        Assertions.assertThrows(ACLException.class, () -> formDataController.updateFormData(formDataSchema));
    }

    @Test
    void deleteAllFormDataByFormIdExceptionTest() {
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setAclId("1");
        formAclDto.setFormId("101");
        Mockito.when(mockFormACLService.getFormAcl(anyString())).thenReturn(formAclDto);
        Assertions.assertThrows(ACLException.class, () -> formDataController.deleteAllFormDataByFormId("101"));
    }

    @Test
    void deleteFormDataByFormIdAndIdExceptionTest() {
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setAclId("1");
        formAclDto.setFormId("101");
        Mockito.when(mockFormACLService.getFormAcl(anyString())).thenReturn(formAclDto);
        Assertions.assertThrows(ACLException.class, () -> formDataController.deleteFormDataByFormIdAndId("101", "201"));
    }

    @Test
    void getFormDataByFormIdAndIdExceptionTest() {
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setAclId("1");
        formAclDto.setFormId("101");
        Mockito.when(mockFormACLService.getFormAcl(anyString())).thenReturn(formAclDto);
        Assertions.assertThrows(ACLException.class, () -> formDataController.getFormDataByFormIdAndId("101", "201", "994102731543871488:orderId,994122561634369536:parcelId"));
    }

}
