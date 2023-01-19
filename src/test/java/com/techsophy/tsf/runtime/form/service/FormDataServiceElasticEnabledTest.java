package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.assertions.Assertions;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.techsophy.idgenerator.IdGeneratorImpl;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.FormDataSchema;
import com.techsophy.tsf.runtime.form.dto.FormResponseSchema;
import com.techsophy.tsf.runtime.form.dto.ValidationResult;
import com.techsophy.tsf.runtime.form.service.impl.FormDataAuditServiceImpl;
import com.techsophy.tsf.runtime.form.service.impl.FormDataServiceImpl;
//import com.techsophy.tsf.runtime.form.service.impl.ValidationCheckServiceImpl;
import com.techsophy.tsf.runtime.form.service.impl.FormValidationServiceImpl;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import com.techsophy.tsf.runtime.form.utils.UserDetails;
import com.techsophy.tsf.runtime.form.utils.WebClientWrapper;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.*;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.CONTENT;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.DATA;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.ELASTIC_ENABLE;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.ELASTIC_SOURCE;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.FORM_DATA;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.ONE;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.SUCCESS;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class FormDataServiceElasticEnabledTest
{
    @Mock
    UserDetails mockUserDetails;
    @Mock
    TokenUtils mockTokenUtils;
    @Mock
    MessageSource mockMessageSource;
    @Mock
    ObjectMapper mockObjectMapper;
    @Mock
    GlobalMessageSource mockGlobalMessageSource;
    @Mock
    FormDataAuditServiceImpl mockFormDataAuditServiceImpl;
    @Mock
    WebClientWrapper mockWebClientWrapper;
    @Mock
    Logger mockLogger;
    @Mock
    FormService mockFormService;
    @Mock
    IdGeneratorImpl mockIdGeneratorImpl;
    @Mock
    WebClient mockWebClient;
    @Mock
    MongoTemplate mockMongoTemplate;
    @Mock
    AggregationResults aggregationResults;
    @Mock
    MongoCollection mockMongoCollection;
    @Mock
    InsertOneResult mockInsertOneResult;
    @Mock
    MongoCursor mongoCursor;
    @Mock
    MongoCollection<Document> mongoCollectionDocument;
    @Mock
    FindIterable<Document> mockDocuments;
    @Mock
    DeleteResult mockDeleteResult;
    @Mock
    FormValidationServiceImpl mockFormValidationServiceImpl;
    @InjectMocks
    FormDataServiceImpl mockFormDataServiceImpl;

    List<Map<String, Object>> userList = new ArrayList<>();
    Map<String, Object> map = new HashMap<>();
    List<Map<String,Object>> list=new ArrayList<>();

    @BeforeEach
    public void init()
    {
        ReflectionTestUtils.setField(mockFormDataServiceImpl,ELASTIC_SOURCE,true);
        ReflectionTestUtils.setField(mockFormDataServiceImpl,GATEWAY_API,GATEWAY_API_VALUE);
        ReflectionTestUtils.setField(mockFormDataServiceImpl,"defaultPageLimit",20);
        Map<String, Object> map = new HashMap<>();
        map.put(CREATED_BY_ID, NULL);
        map.put(CREATED_BY_NAME, NULL);
        map.put(CREATED_ON, NULL);
        map.put(UPDATED_BY_ID, NULL);
        map.put(UPDATED_BY_NAME, NULL);
        map.put(UPDATED_ON, NULL);
        map.put(ID, BIGINTEGER_ID);
        map.put(USER_NAME, USER_FIRST_NAME);
        map.put(FIRST_NAME, USER_LAST_NAME);
        map.put(LAST_NAME, USER_FIRST_NAME);
        map.put(MOBILE_NUMBER, NUMBER);
        map.put(EMAIL_ID, MAIL_ID);
        map.put(DEPARTMENT, NULL);
        userList.add(map);
    }

    @Test
    void saveFormDataEmptyUniqueDocumentIdTest() throws IOException
    {
        ReflectionTestUtils.setField(mockFormDataServiceImpl, ELASTIC_ENABLE, true);
        doReturn(userList).when(mockUserDetails).getUserDetails();
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockIdGeneratorImpl.nextId()).thenReturn(BigInteger.valueOf(Long.parseLong(TEST_FORM_ID)));
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        FormDataSchema formDataSchemaTest=new FormDataSchema(EMPTY_STRING,TEST_FORM_ID,
                TEST_VERSION,testFormData,testFormMetaData);
        ValidationResult validationResult=new ValidationResult("name");
        List<ValidationResult> validationResultList=new ArrayList<>();
        validationResultList.add(validationResult);
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_FORM_ID, TEST_NAME, TEST_COMPONENTS, list,TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID,
                TEST_CREATED_ON, TEST_UPDATED_BY_ID, TEST_UPDATED_ON);
        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
        Mockito.when(mockFormValidationServiceImpl.validateData(any(),any(),any())).thenReturn(validationResultList);
        String responseTest="{\n" +
                "    \"data\": {\n" +
                "        \"id\": \"963403130239434752\",\n" +
                "        \"version\": 1\n" +
                "    },\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"Form data saved successfully\"\n" +
                "}";
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(POST),any())).thenReturn(responseTest);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ +formDataSchemaTest.getFormId())).thenReturn(true);
        Map<String, Object> formDataMap = new HashMap<>();
        formDataMap.put(UNDERSCORE_ID,Long.parseLong(UNDERSCORE_ID_VALUE));
        formDataMap.put(FORM_ID,TEST_FORM_ID);
        formDataMap.put(VERSION, String.valueOf(1));
        formDataMap.put(FORM_META_DATA, formDataSchemaTest.getFormMetadata());
        formDataMap.put(FORM_DATA, formDataSchemaTest.getFormData());
        formDataMap.put(CREATED_ON, Date.from(Instant.now()));
        formDataMap.put(CREATED_BY_ID,CREATED_BY_USER_ID);
        formDataMap.put(CREATED_BY_NAME, CREATED_BY_USER_NAME);
        formDataMap.put(UPDATED_ON,TEST_UPDATED_ON);
        formDataMap.put(UPDATED_BY_ID,UPDATED_BY_USER_ID);
        formDataMap.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        Mockito.when(mockMongoTemplate.save(any(),any())).thenReturn(formDataMap);
        Assertions.assertNotNull(mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
    }

    @Test
    void saveFormDataUniqueDocumentIdTest() throws IOException
    {
        ReflectionTestUtils.setField(mockFormDataServiceImpl, ELASTIC_ENABLE, true);
        doReturn(userList).when(mockUserDetails).getUserDetails();
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockIdGeneratorImpl.nextId()).thenReturn(BigInteger.valueOf(Long.parseLong(TEST_FORM_ID)));
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        FormDataSchema formDataSchemaTest=new FormDataSchema(TEST_ID_VALUE,TEST_FORM_ID,
                TEST_VERSION,testFormData,testFormMetaData);
        ValidationResult validationResult=new ValidationResult("name");
        List<ValidationResult> validationResultList=new ArrayList<>();
        validationResultList.add(validationResult);
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema(TEST_FORM_ID, TEST_NAME, TEST_COMPONENTS, list,TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE, TEST_CREATED_BY_ID,
                TEST_CREATED_ON, TEST_UPDATED_BY_ID, TEST_UPDATED_ON);
        Mockito.when(mockFormService.getRuntimeFormById(formDataSchemaTest.getFormId())).thenReturn(formResponseSchemaTest);
        Mockito.when(mockFormValidationServiceImpl.validateData(any(),any(),any())).thenReturn(validationResultList);
        String postResponse="{\n" +
                "    \"data\": {\n" +
                "        \"id\": \"963403130239434752\",\n" +
                "        \"version\": 1\n" +
                "    },\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"Form data saved successfully\"\n" +
                "}";
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(POST),any())).thenReturn(postResponse);
        String getResponse="{\n" +
                "    \"data\": [\n" +
                "        {\n" +
                "            \"id\": \"945292224435081216\",\n" +
                "            \"formId\": \"928232634435125248\",\n" +
                "            \"version\": 2,\n" +
                "            \"formData\": {\n" +
                "                \"name\": \"akhil\",\n" +
                "                \"id\": \"945292224435081216\",\n" +
                "                \"age\": \"202\"\n" +
                "            },\n" +
                "            \"formMetadata\": {\n" +
                "                \"formVersion\": \"101\"\n" +
                "            },\n" +
                "            \"createdById\": \"910797699334508544\",\n" +
                "            \"createdOn\": \"2022-02-21T12:13:48.985338Z\",\n" +
                "            \"createdByName\": \"tejaswini kaza\",\n" +
                "            \"updatedById\": \"910797699334508544\",\n" +
                "            \"updatedOn\": \"2022-02-21T12:14:01.117149Z\",\n" +
                "            \"updatedByName\": \"tejaswini kaza\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"Form data retrieved successfully\"\n" +
                "}";
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(GET),any())).thenReturn(getResponse);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ +formDataSchemaTest.getFormId())).thenReturn(true);
        Map<String, Object> formDataMap = new HashMap<>();
        formDataMap.put(UNDERSCORE_ID,Long.parseLong(UNDERSCORE_ID_VALUE));
        formDataMap.put(FORM_ID,TEST_FORM_ID);
        formDataMap.put(VERSION, String.valueOf(1));
        formDataMap.put(FORM_META_DATA, formDataSchemaTest.getFormMetadata());
        formDataMap.put(FORM_DATA, formDataSchemaTest.getFormData());
        formDataMap.put(CREATED_ON, Date.from(Instant.now()));
        formDataMap.put(CREATED_BY_ID,CREATED_BY_USER_ID);
        formDataMap.put(CREATED_BY_NAME, CREATED_BY_USER_NAME);
        formDataMap.put(UPDATED_ON,TEST_UPDATED_ON);
        formDataMap.put(UPDATED_BY_ID,UPDATED_BY_USER_ID);
        formDataMap.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        Mockito.when(mockMongoTemplate.getCollection(anyString())).thenReturn(mockMongoCollection);
        Document document = new Document(formDataMap);
        document.put(UNDERSCORE_ID,TEST_ID_VALUE);
        FindIterable<Document> iterable = mock(FindIterable.class);
        MongoCursor cursor = mock(MongoCursor.class);
        Mockito.when(mockMongoCollection.find()).thenReturn(iterable);
        Mockito.when(iterable.iterator()).thenReturn(cursor);
        Mockito.when(cursor.hasNext()).thenReturn(true).thenReturn(false);
        Mockito.when(cursor.next()).thenReturn(document);
        Mockito.when(mockMongoCollection.findOneAndReplace((Bson) any(),any(),any())).thenReturn(formDataMap);
        Map<String,Object> responseMap=new HashMap<>();
        LinkedHashMap<String,Object> dataMap=new LinkedHashMap<>();
        dataMap.put("id","963403130239434752");
        dataMap.put("version",1);
        responseMap.put("success",true);
        responseMap.put("message","FormData saved successfully");
        responseMap.put("data",dataMap);
        Mockito.when(mockObjectMapper.readValue(getResponse,Map.class)).thenReturn(responseMap);
        Mockito.when(mockObjectMapper.convertValue(responseMap.get(DATA),LinkedHashMap.class)).thenReturn(dataMap);
        Assertions.assertNotNull(mockFormDataServiceImpl.saveFormData(formDataSchemaTest));
    }

    @Test
    void getAllFormDataByFormIdEmptySortBySortEmptyRelationsTest() throws JsonProcessingException
    {
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        ReflectionTestUtils.setField(mockFormDataServiceImpl, ELASTIC_ENABLE, true);
        String getResponse="{\n" +
                "    \"data\": {\n" +
                "        \"content\": [\n" +
                "            {\n" +
                "                \"_id\": 994192119303684096,\n" +
                "                \"formData\": {\n" +
                "                    \"orderName\": \"order1\"\n" +
                "                },\n" +
                "                \"formMetadata\": null,\n" +
                "                \"version\": \"2\",\n" +
                "                \"createdOn\": \"2022-07-06T10:44:32.438+00:00\",\n" +
                "                \"createdById\": \"910797699334508544\",\n" +
                "                \"createdByName\": \"tejaswini kaza\",\n" +
                "                \"updatedOn\": \"2022-07-06T10:45:32.665+00:00\",\n" +
                "                \"updatedById\": \"910797699334508544\",\n" +
                "                \"updatedByName\": \"tejaswini kaza\",\n" +
                "                \"tp_runtime_form_data_994102731543871488\": [\n" +
                "                    {\n" +
                "                        \"_id\": 994193008575823872,\n" +
                "                        \"formData\": {\n" +
                "                            \"customerName\": \"customer1\",\n" +
                "                            \"orderId\": 994192119303684096\n" +
                "                        },\n" +
                "                        \"formMetadata\": null,\n" +
                "                        \"version\": \"1\",\n" +
                "                        \"createdOn\": \"2022-07-06T10:48:04.457+00:00\",\n" +
                "                        \"createdById\": \"910797699334508544\",\n" +
                "                        \"createdByName\": \"tejaswini kaza\",\n" +
                "                        \"updatedOn\": \"2022-07-06T10:48:04.457+00:00\",\n" +
                "                        \"updatedById\": \"910797699334508544\",\n" +
                "                        \"updatedByName\": \"tejaswini kaza\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"_id\": 994249583021703168,\n" +
                "                        \"formData\": {\n" +
                "                            \"customerName\": \"customer2\",\n" +
                "                            \"orderId\": 994192119303684096\n" +
                "                        },\n" +
                "                        \"formMetadata\": null,\n" +
                "                        \"version\": \"1\",\n" +
                "                        \"createdOn\": \"2022-07-06T14:32:52.856+00:00\",\n" +
                "                        \"createdById\": \"910797699334508544\",\n" +
                "                        \"createdByName\": \"tejaswini kaza\",\n" +
                "                        \"updatedOn\": \"2022-07-06T14:32:52.856+00:00\",\n" +
                "                        \"updatedById\": \"910797699334508544\",\n" +
                "                        \"updatedByName\": \"tejaswini kaza\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"tp_runtime_form_data_994122561634369536\": [\n" +
                "                    {\n" +
                "                        \"_id\": 994232096431456256,\n" +
                "                        \"formData\": {\n" +
                "                            \"parcelName\": \"parcel1\",\n" +
                "                            \"parcelId\": 994192119303684096\n" +
                "                        },\n" +
                "                        \"formMetadata\": null,\n" +
                "                        \"version\": \"1\",\n" +
                "                        \"createdOn\": \"2022-07-06T13:23:23.728+00:00\",\n" +
                "                        \"createdById\": \"910797699334508544\",\n" +
                "                        \"createdByName\": \"tejaswini kaza\",\n" +
                "                        \"updatedOn\": \"2022-07-06T13:23:23.728+00:00\",\n" +
                "                        \"updatedById\": \"910797699334508544\",\n" +
                "                        \"updatedByName\": \"tejaswini kaza\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"_id\": 994239734070296576,\n" +
                "                        \"formData\": {\n" +
                "                            \"parcelName\": \"parcel2\",\n" +
                "                            \"parcelId\": 994192119303684096\n" +
                "                        },\n" +
                "                        \"formMetadata\": null,\n" +
                "                        \"version\": \"1\",\n" +
                "                        \"createdOn\": \"2022-07-06T13:53:44.683+00:00\",\n" +
                "                        \"createdById\": \"910797699334508544\",\n" +
                "                        \"createdByName\": \"tejaswini kaza\",\n" +
                "                        \"updatedOn\": \"2022-07-06T13:53:44.683+00:00\",\n" +
                "                        \"updatedById\": \"910797699334508544\",\n" +
                "                        \"updatedByName\": \"tejaswini kaza\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"_id\": 994508534322515968,\n" +
                "                        \"formData\": {\n" +
                "                            \"parcelName\": \"parcel3\",\n" +
                "                            \"parcelId\": 994192119303684096\n" +
                "                        },\n" +
                "                        \"formMetadata\": null,\n" +
                "                        \"version\": \"1\",\n" +
                "                        \"createdOn\": \"2022-07-07T07:41:51.657+00:00\",\n" +
                "                        \"createdById\": \"910797699334508544\",\n" +
                "                        \"createdByName\": \"tejaswini kaza\",\n" +
                "                        \"updatedOn\": \"2022-07-07T07:41:51.657+00:00\",\n" +
                "                        \"updatedById\": \"910797699334508544\",\n" +
                "                        \"updatedByName\": \"tejaswini kaza\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"_id\": 994249123246292992,\n" +
                "                \"formData\": {\n" +
                "                    \"orderName\": \"order2\"\n" +
                "                },\n" +
                "                \"formMetadata\": null,\n" +
                "                \"version\": \"1\",\n" +
                "                \"createdOn\": \"2022-07-06T14:31:03.237+00:00\",\n" +
                "                \"createdById\": \"910797699334508544\",\n" +
                "                \"createdByName\": \"tejaswini kaza\",\n" +
                "                \"updatedOn\": \"2022-07-06T14:31:03.237+00:00\",\n" +
                "                \"updatedById\": \"910797699334508544\",\n" +
                "                \"updatedByName\": \"tejaswini kaza\",\n" +
                "                \"tp_runtime_form_data_994102731543871488\": [\n" +
                "                    {\n" +
                "                        \"_id\": 994249903449751552,\n" +
                "                        \"formData\": {\n" +
                "                            \"customerName\": \"customer3\",\n" +
                "                            \"orderId\": 994249123246292992\n" +
                "                        },\n" +
                "                        \"formMetadata\": null,\n" +
                "                        \"version\": \"1\",\n" +
                "                        \"createdOn\": \"2022-07-06T14:34:09.252+00:00\",\n" +
                "                        \"createdById\": \"910797699334508544\",\n" +
                "                        \"createdByName\": \"tejaswini kaza\",\n" +
                "                        \"updatedOn\": \"2022-07-06T14:34:09.252+00:00\",\n" +
                "                        \"updatedById\": \"910797699334508544\",\n" +
                "                        \"updatedByName\": \"tejaswini kaza\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"_id\": 994256859321253888,\n" +
                "                        \"formData\": {\n" +
                "                            \"customerName\": \"customer4\",\n" +
                "                            \"orderId\": 994249123246292992\n" +
                "                        },\n" +
                "                        \"formMetadata\": null,\n" +
                "                        \"version\": \"1\",\n" +
                "                        \"createdOn\": \"2022-07-06T15:01:47.661+00:00\",\n" +
                "                        \"createdById\": \"910797699334508544\",\n" +
                "                        \"createdByName\": \"tejaswini kaza\",\n" +
                "                        \"updatedOn\": \"2022-07-06T15:01:47.661+00:00\",\n" +
                "                        \"updatedById\": \"910797699334508544\",\n" +
                "                        \"updatedByName\": \"tejaswini kaza\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"tp_runtime_form_data_994122561634369536\": [\n" +
                "                    {\n" +
                "                        \"_id\": 994509203343364096,\n" +
                "                        \"formData\": {\n" +
                "                            \"parcelName\": \"parcel4\",\n" +
                "                            \"parcelId\": 994249123246292992\n" +
                "                        },\n" +
                "                        \"formMetadata\": null,\n" +
                "                        \"version\": \"1\",\n" +
                "                        \"createdOn\": \"2022-07-07T07:44:31.164+00:00\",\n" +
                "                        \"createdById\": \"910797699334508544\",\n" +
                "                        \"createdByName\": \"tejaswini kaza\",\n" +
                "                        \"updatedOn\": \"2022-07-07T07:44:31.164+00:00\",\n" +
                "                        \"updatedById\": \"910797699334508544\",\n" +
                "                        \"updatedByName\": \"tejaswini kaza\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"_id\": 994509211807469568,\n" +
                "                        \"formData\": {\n" +
                "                            \"parcelName\": \"parcel4\",\n" +
                "                            \"parcelId\": 994249123246292992\n" +
                "                        },\n" +
                "                        \"formMetadata\": null,\n" +
                "                        \"version\": \"1\",\n" +
                "                        \"createdOn\": \"2022-07-07T07:44:33.182+00:00\",\n" +
                "                        \"createdById\": \"910797699334508544\",\n" +
                "                        \"createdByName\": \"tejaswini kaza\",\n" +
                "                        \"updatedOn\": \"2022-07-07T07:44:33.182+00:00\",\n" +
                "                        \"updatedById\": \"910797699334508544\",\n" +
                "                        \"updatedByName\": \"tejaswini kaza\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"totalPages\": 1,\n" +
                "        \"totalElements\": 2,\n" +
                "        \"page\": 0,\n" +
                "        \"size\": 5,\n" +
                "        \"numberOfElements\": 2\n" +
                "    },\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"Form data retrieved successfully\"\n" +
                "}";
        LinkedHashMap<String, Object> testFormData = new LinkedHashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String,Object> responseMap=new HashMap<>();
        LinkedHashMap<String,Object> dataMap=new LinkedHashMap<>();
        List<Map<String,Object>> contentListTest=new ArrayList<>();
        Map<String,Object> singleContentTest=new HashMap<>();
        singleContentTest.put(FORM_ID,TEST_FORM_ID);
        singleContentTest.put(ID,TEST_ID);
        singleContentTest.put(VERSION,TEST_VERSION);
        singleContentTest.put(FORM_DATA,testFormData);
        singleContentTest.put(FORM_META_DATA,testFormMetaData);
        singleContentTest.put(CREATED_BY_NAME,CREATED_BY_USER_NAME);
        singleContentTest.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        singleContentTest.put(CREATED_BY_ID,BIGINTEGER_ID);
        singleContentTest.put(UPDATED_BY_ID,BIGINTEGER_ID);
        singleContentTest.put(CREATED_ON,TEST_CREATED_ON);
        singleContentTest.put(UPDATED_ON,TEST_UPDATED_ON);
        contentListTest.add(singleContentTest);
        dataMap.put(CONTENT,contentListTest);
        responseMap.put(SUCCESS,true);
        responseMap.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
        responseMap.put(DATA,dataMap);
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(GET),any())).thenReturn(getResponse);
        Mockito.when(mockObjectMapper.readValue(getResponse,Map.class)).thenReturn(responseMap);
        Mockito.when(mockObjectMapper.convertValue(responseMap.get(DATA),Map.class)).thenReturn(dataMap);
        Mockito.when(mockObjectMapper.convertValue(dataMap.get(CONTENT),List.class)).thenReturn(contentListTest);
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID, null, TEST_FILTER, null, null));
    }

    @Test
    void getAllFormDataByFormIdPaginationTest() throws JsonProcessingException {
        ReflectionTestUtils.setField(mockFormDataServiceImpl, ELASTIC_ENABLE, true);
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        String getResponse="{\n" +
                "    \"data\": {\n" +
                "        \"content\": [\n" +
                "            {\n" +
                "                \"_id\": 994192119303684096,\n" +
                "                \"formData\": {\n" +
                "                    \"orderName\": \"order1\"\n" +
                "                },\n" +
                "                \"formMetadata\": null,\n" +
                "                \"version\": \"2\",\n" +
                "                \"createdOn\": \"2022-07-06T10:44:32.438+00:00\",\n" +
                "                \"createdById\": \"910797699334508544\",\n" +
                "                \"createdByName\": \"tejaswini kaza\",\n" +
                "                \"updatedOn\": \"2022-07-06T10:45:32.665+00:00\",\n" +
                "                \"updatedById\": \"910797699334508544\",\n" +
                "                \"updatedByName\": \"tejaswini kaza\",\n" +
                "                \"tp_runtime_form_data_994102731543871488\": [\n" +
                "                    {\n" +
                "                        \"_id\": 994193008575823872,\n" +
                "                        \"formData\": {\n" +
                "                            \"customerName\": \"customer1\",\n" +
                "                            \"orderId\": 994192119303684096\n" +
                "                        },\n" +
                "                        \"formMetadata\": null,\n" +
                "                        \"version\": \"1\",\n" +
                "                        \"createdOn\": \"2022-07-06T10:48:04.457+00:00\",\n" +
                "                        \"createdById\": \"910797699334508544\",\n" +
                "                        \"createdByName\": \"tejaswini kaza\",\n" +
                "                        \"updatedOn\": \"2022-07-06T10:48:04.457+00:00\",\n" +
                "                        \"updatedById\": \"910797699334508544\",\n" +
                "                        \"updatedByName\": \"tejaswini kaza\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"_id\": 994239586799894528,\n" +
                "                        \"formData\": {\n" +
                "                            \"parcelName\": \"customer2\",\n" +
                "                            \"orderId\": 994192119303684096\n" +
                "                        },\n" +
                "                        \"formMetadata\": null,\n" +
                "                        \"version\": \"1\",\n" +
                "                        \"createdOn\": \"2022-07-06T13:53:09.571+00:00\",\n" +
                "                        \"createdById\": \"910797699334508544\",\n" +
                "                        \"createdByName\": \"tejaswini kaza\",\n" +
                "                        \"updatedOn\": \"2022-07-06T13:53:09.572+00:00\",\n" +
                "                        \"updatedById\": \"910797699334508544\",\n" +
                "                        \"updatedByName\": \"tejaswini kaza\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"_id\": 994192119303684096,\n" +
                "                \"formData\": {\n" +
                "                    \"orderName\": \"order1\"\n" +
                "                },\n" +
                "                \"formMetadata\": null,\n" +
                "                \"version\": \"2\",\n" +
                "                \"createdOn\": \"2022-07-06T10:44:32.438+00:00\",\n" +
                "                \"createdById\": \"910797699334508544\",\n" +
                "                \"createdByName\": \"tejaswini kaza\",\n" +
                "                \"updatedOn\": \"2022-07-06T10:45:32.665+00:00\",\n" +
                "                \"updatedById\": \"910797699334508544\",\n" +
                "                \"updatedByName\": \"tejaswini kaza\",\n" +
                "                \"tp_runtime_form_data_994122561634369536\": [\n" +
                "                    {\n" +
                "                        \"_id\": 994232096431456256,\n" +
                "                        \"formData\": {\n" +
                "                            \"parcelName\": \"parcel1\",\n" +
                "                            \"orderId\": 994192119303684096\n" +
                "                        },\n" +
                "                        \"formMetadata\": null,\n" +
                "                        \"version\": \"1\",\n" +
                "                        \"createdOn\": \"2022-07-06T13:23:23.728+00:00\",\n" +
                "                        \"createdById\": \"910797699334508544\",\n" +
                "                        \"createdByName\": \"tejaswini kaza\",\n" +
                "                        \"updatedOn\": \"2022-07-06T13:23:23.728+00:00\",\n" +
                "                        \"updatedById\": \"910797699334508544\",\n" +
                "                        \"updatedByName\": \"tejaswini kaza\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"_id\": 994239734070296576,\n" +
                "                        \"formData\": {\n" +
                "                            \"parcelName\": \"parcel2\",\n" +
                "                            \"orderId\": 994192119303684096\n" +
                "                        },\n" +
                "                        \"formMetadata\": null,\n" +
                "                        \"version\": \"1\",\n" +
                "                        \"createdOn\": \"2022-07-06T13:53:44.683+00:00\",\n" +
                "                        \"createdById\": \"910797699334508544\",\n" +
                "                        \"createdByName\": \"tejaswini kaza\",\n" +
                "                        \"updatedOn\": \"2022-07-06T13:53:44.683+00:00\",\n" +
                "                        \"updatedById\": \"910797699334508544\",\n" +
                "                        \"updatedByName\": \"tejaswini kaza\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"totalPages\": 1,\n" +
                "        \"totalElements\": 2,\n" +
                "        \"page\": 0,\n" +
                "        \"size\": 5,\n" +
                "        \"numberOfElements\": 2\n" +
                "    },\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"Form data retrieved successfully\"\n" +
                "}";
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(GET),any())).thenReturn(getResponse);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        LinkedHashMap<String, Object> testFormData = new LinkedHashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        Map<String,Object> responseMapTest =new HashMap<>();
        ArrayList contentListTest =new ArrayList();
        LinkedHashMap<String,Object> singleContentTest =new LinkedHashMap<>();
        singleContentTest.put(FORM_ID,TEST_FORM_ID);
        singleContentTest.put(ID,TEST_ID);
        singleContentTest.put(VERSION,TEST_VERSION);
        singleContentTest.put(FORM_DATA,testFormData);
        singleContentTest.put(FORM_META_DATA,testFormMetaData);
        singleContentTest.put(CREATED_BY_NAME,CREATED_BY_USER_NAME);
        singleContentTest.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        singleContentTest.put(CREATED_BY_ID,BIGINTEGER_ID);
        singleContentTest.put(UPDATED_BY_ID,BIGINTEGER_ID);
        singleContentTest.put(CREATED_ON,TEST_CREATED_ON);
        singleContentTest.put(UPDATED_ON,TEST_UPDATED_ON);
        contentListTest.add(singleContentTest);
        Map<String,Object> dataMapTest =new HashMap<>();
        dataMapTest.put(CONTENT, contentListTest);
        dataMapTest.put(TOTAL_PAGES, ONE);
        dataMapTest.put(TOTAL_ELEMENTS,ONE);
        dataMapTest.put(PAGE,ZERO);
        dataMapTest.put(SIZE,PAGE_SIZE);
        dataMapTest.put(NUMBER_OF_ELEMENTS,ONE);
        responseMapTest.put(DATA, dataMapTest);
        responseMapTest.put(SUCCESS,true);
        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
        Mockito.when(mockObjectMapper.readValue(getResponse,Map.class)).thenReturn(responseMapTest);
        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
        Mockito.when(mockObjectMapper.convertValue(dataMapTest.get(CONTENT),List.class)).thenReturn(contentListTest);
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,null,TEST_FILTER,null,null, PageRequest.of(1,5)));
    }

    @Test
    void getAllFormDataByFormIdEmptySortBySortOrderTest()
    {
        ReflectionTestUtils.setField(mockFormDataServiceImpl, ELASTIC_ENABLE, true);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        LinkedHashMap<String, Object> testFormData = new LinkedHashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        Map<String,Object> responseMapTest =new HashMap<>();
        ArrayList contentListTest =new ArrayList();
        LinkedHashMap<String,Object> singleContentTest =new LinkedHashMap<>();
        singleContentTest.put(FORM_ID,TEST_FORM_ID);
        singleContentTest.put(ID,TEST_ID);
        singleContentTest.put(VERSION,TEST_VERSION);
        singleContentTest.put(FORM_DATA,testFormData);
        singleContentTest.put(FORM_META_DATA,testFormMetaData);
        singleContentTest.put(CREATED_BY_NAME,CREATED_BY_USER_NAME);
        singleContentTest.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        singleContentTest.put(CREATED_BY_ID,BIGINTEGER_ID);
        singleContentTest.put(UPDATED_BY_ID,BIGINTEGER_ID);
        singleContentTest.put(CREATED_ON,TEST_CREATED_ON);
        singleContentTest.put(UPDATED_ON,TEST_UPDATED_ON);
        contentListTest.add(singleContentTest);
        Map<String,Object> dataMapTest =new HashMap<>();
        dataMapTest.put(CONTENT, contentListTest);
        responseMapTest.put(DATA, dataMapTest);
        responseMapTest.put(SUCCESS,true);
        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ +TEST_FORM_ID)).thenReturn(true);
        Mockito.doReturn(aggregationResults).when(mockMongoTemplate).aggregate(Mockito.any(Aggregation.class), Mockito.eq(COLLECTION), Mockito.eq(Map.class));
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID, TEST_RELATIONS, TEST_FILTER, null, null));
    }

    @Test
    void getAllFormDataByFormIdSortBySortOrderTest() throws JsonProcessingException
    {
        ReflectionTestUtils.setField(mockFormDataServiceImpl, ELASTIC_ENABLE, true);
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        String responseTest =RESPONSE_VALUE_17;
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(GET),any())).thenReturn(responseTest);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        LinkedHashMap<String, Object> testFormData = new LinkedHashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        Map<String,Object> responseMapTest =new HashMap<>();
        ArrayList contentListTest =new ArrayList();
        LinkedHashMap<String,Object> singleContentTest =new LinkedHashMap<>();
        singleContentTest.put(FORM_ID,TEST_FORM_ID);
        singleContentTest.put(ID,TEST_ID);
        singleContentTest.put(VERSION,TEST_VERSION);
        singleContentTest.put(FORM_DATA,testFormData);
        singleContentTest.put(FORM_META_DATA,testFormMetaData);
        singleContentTest.put(CREATED_BY_NAME,CREATED_BY_USER_NAME);
        singleContentTest.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        singleContentTest.put(CREATED_BY_ID,BIGINTEGER_ID);
        singleContentTest.put(UPDATED_BY_ID,BIGINTEGER_ID);
        singleContentTest.put(CREATED_ON,TEST_CREATED_ON);
        singleContentTest.put(UPDATED_ON,TEST_UPDATED_ON);
        contentListTest.add(singleContentTest);
        Map<String,Object> dataMapTest =new HashMap<>();
        dataMapTest.put(CONTENT, contentListTest);
        dataMapTest.put(TOTAL_PAGES, ONE);
        dataMapTest.put(TOTAL_ELEMENTS,ONE);
        dataMapTest.put(PAGE,ZERO);
        dataMapTest.put(SIZE,PAGE_SIZE);
        dataMapTest.put(NUMBER_OF_ELEMENTS,ONE);
        responseMapTest.put(DATA, dataMapTest);
        responseMapTest.put(SUCCESS,true);
        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
        Mockito.when(mockObjectMapper.convertValue(dataMapTest.get(CONTENT),List.class)).thenReturn(contentListTest);
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,null,TEST_FILTER,CREATED_ON,DESCENDING));
    }

    @Test
    void getAllFormDataByFormIdElastic() throws Exception
    {
        ReflectionTestUtils.setField(mockFormDataServiceImpl,"elasticEnable",true);
        List<String> list1 = new ArrayList<>();
        list1.add("10");
        list1.add("2");
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put("id",null);
        FormDataSchema formDataSchemaTest = new FormDataSchema(null, TEST_FORM_ID, TEST_VERSION, testFormData, testFormMetaData);
        LinkedHashMap<String,Map<String,Object>> schemaMap=new LinkedHashMap<>();
        LinkedHashMap<String,Object> fieldsMap=new LinkedHashMap<>();
        fieldsMap.put(REQUIRED,false);
        fieldsMap.put(UNIQUE,false);
        schemaMap.put(NAME,fieldsMap);
        Map<String, Object> givenData = new HashMap<>();
        givenData.put(NAME, NAME_VALUE);
        Map<String, Object> formDataMap = new HashMap<>();
        formDataMap.put(UNDERSCORE_ID,Long.parseLong(UNDERSCORE_ID_VALUE));
        formDataMap.put(FORM_ID,TEST_FORM_ID);
        formDataMap.put(VERSION, String.valueOf(1));
        formDataMap.put(FORM_META_DATA, formDataSchemaTest.getFormMetadata());
        formDataMap.put(FORM_DATA, formDataSchemaTest.getFormData());
        formDataMap.put(CREATED_ON, Instant.now());
        formDataMap.put(CREATED_BY_ID,CREATED_BY_USER_ID);
        formDataMap.put(CREATED_BY_NAME, CREATED_BY_USER_NAME);
        formDataMap.put(UPDATED_ON,TEST_UPDATED_ON);
        formDataMap.put(UPDATED_BY_ID,UPDATED_BY_USER_ID);
        formDataMap.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        formDataMap.put(DATA,UPDATED_BY_USER_NAME);
        LinkedHashMap data1 = new LinkedHashMap<>();
        data1.put("abc","abc");
        LinkedHashMap<String,Object> data = new LinkedHashMap<>();
        data.put(FORM_DATA,data1);
        ArrayList list2 = new ArrayList<>();
        list2.add(data);
        when(mockObjectMapper.readValue(anyString(), eq(Map.class))).thenReturn(formDataMap);
        Date currentDate = new Date();
        Document document1 = new Document("version",1);
        document1.append("formData",formDataMap);
        document1.append("formMetaData",formDataMap);
        document1.append("_id","1");
        document1.append(CREATED_ON,currentDate);
        document1.append(CREATED_BY_ID,"1");
        document1.append(CREATED_BY_NAME,STRING);
        String responseTest =RESPONSE_VALUE_17;
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),any(),any())).thenReturn(responseTest);
        Map<String,Object> responseMapTest =new HashMap<>();
        ArrayList contentListTest =new ArrayList();
        LinkedHashMap<String,Object> singleContentTest =new LinkedHashMap<>();
        singleContentTest.put(FORM_ID,TEST_FORM_ID);
        singleContentTest.put(ID,TEST_ID);
        singleContentTest.put(VERSION,TEST_VERSION);
        singleContentTest.put(FORM_DATA,testFormData);
        singleContentTest.put(FORM_META_DATA,testFormMetaData);
        singleContentTest.put(CREATED_BY_NAME,CREATED_BY_USER_NAME);
        singleContentTest.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        singleContentTest.put(CREATED_BY_ID,BIGINTEGER_ID);
        singleContentTest.put(UPDATED_BY_ID,BIGINTEGER_ID);
        singleContentTest.put(CREATED_ON,TEST_CREATED_ON);
        singleContentTest.put(UPDATED_ON,TEST_UPDATED_ON);
        contentListTest.add(singleContentTest);
        Map<String,Object> dataMapTest =new HashMap<>();
        dataMapTest.put(CONTENT, contentListTest);
        dataMapTest.put(TOTAL_PAGES, ONE);
        dataMapTest.put(TOTAL_ELEMENTS,ONE);
        dataMapTest.put(PAGE,ZERO);
        dataMapTest.put(SIZE,PAGE_SIZE);
        dataMapTest.put(NUMBER_OF_ELEMENTS,ONE);
        responseMapTest.put(DATA, dataMapTest);
        responseMapTest.put(SUCCESS,true);
        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
        Mockito.when(mockObjectMapper.convertValue(dataMapTest.get(CONTENT),List.class)).thenReturn(contentListTest);
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID, null, TEST_FILTER, CREATED_ON, DESCENDING));
    }

    @Test
    void getAllFormDataByFormId() throws Exception
    {
        ReflectionTestUtils.setField(mockFormDataServiceImpl,"elasticEnable",true);
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        String responseTest =RESPONSE_VALUE_17;
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(GET),any())).thenReturn(responseTest);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        LinkedHashMap<String, Object> testFormData = new LinkedHashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        Map<String,Object> responseMapTest =new HashMap<>();
        ArrayList contentListTest =new ArrayList();
        LinkedHashMap<String,Object> singleContentTest =new LinkedHashMap<>();
        singleContentTest.put(FORM_ID,TEST_FORM_ID);
        singleContentTest.put(ID,TEST_ID);
        singleContentTest.put(VERSION,TEST_VERSION);
        singleContentTest.put(FORM_DATA,testFormData);
        singleContentTest.put(FORM_META_DATA,testFormMetaData);
        singleContentTest.put(CREATED_BY_NAME,CREATED_BY_USER_NAME);
        singleContentTest.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        singleContentTest.put(CREATED_BY_ID,BIGINTEGER_ID);
        singleContentTest.put(UPDATED_BY_ID,BIGINTEGER_ID);
        singleContentTest.put(CREATED_ON,TEST_CREATED_ON);
        singleContentTest.put(UPDATED_ON,TEST_UPDATED_ON);
        contentListTest.add(singleContentTest);
        Map<String,Object> dataMapTest =new HashMap<>();
        dataMapTest.put(CONTENT, contentListTest);
        dataMapTest.put(TOTAL_PAGES, ONE);
        dataMapTest.put(TOTAL_ELEMENTS,ONE);
        dataMapTest.put(PAGE,ZERO);
        dataMapTest.put(SIZE,PAGE_SIZE);
        dataMapTest.put(NUMBER_OF_ELEMENTS,ONE);
        responseMapTest.put(DATA, dataMapTest);
        responseMapTest.put(SUCCESS,true);
        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
        Mockito.when(mockObjectMapper.convertValue(dataMapTest.get(CONTENT),List.class)).thenReturn(contentListTest);
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID,null));
    }

    @Test
    void getAllFormDataByFormIdElasticEnable() throws Exception
    {
        ReflectionTestUtils.setField(mockFormDataServiceImpl, ELASTIC_ENABLE, true);
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        String responseTest =RESPONSE_VALUE_17;
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(GET),any())).thenReturn(responseTest);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        LinkedHashMap<String, Object> testFormData = new LinkedHashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        Map<String,Object> responseMapTest =new HashMap<>();
        ArrayList contentListTest =new ArrayList();
        LinkedHashMap<String,Object> singleContentTest =new LinkedHashMap<>();
        singleContentTest.put(FORM_ID,TEST_FORM_ID);
        singleContentTest.put(ID,TEST_ID);
        singleContentTest.put(VERSION,TEST_VERSION);
        singleContentTest.put(FORM_DATA,testFormData);
        singleContentTest.put(FORM_META_DATA,testFormMetaData);
        singleContentTest.put(CREATED_BY_NAME,CREATED_BY_USER_NAME);
        singleContentTest.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        singleContentTest.put(CREATED_BY_ID,BIGINTEGER_ID);
        singleContentTest.put(UPDATED_BY_ID,BIGINTEGER_ID);
        singleContentTest.put(CREATED_ON,TEST_CREATED_ON);
        singleContentTest.put(UPDATED_ON,TEST_UPDATED_ON);
        contentListTest.add(singleContentTest);
        Map<String,Object> dataMapTest =new HashMap<>();
        dataMapTest.put(CONTENT, contentListTest);
        dataMapTest.put(TOTAL_PAGES, ONE);
        dataMapTest.put(TOTAL_ELEMENTS,ONE);
        dataMapTest.put(PAGE,ZERO);
        dataMapTest.put(SIZE,PAGE_SIZE);
        dataMapTest.put(NUMBER_OF_ELEMENTS,ONE);
        responseMapTest.put(DATA, dataMapTest);
        responseMapTest.put(SUCCESS,true);
        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
        Mockito.when(mockObjectMapper.convertValue(dataMapTest.get(CONTENT),List.class)).thenReturn(contentListTest);
        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
        Assertions.assertNotNull(mockFormDataServiceImpl.getAllFormDataByFormId(TEST_FORM_ID, null));
    }

    @Test
    void getFormDataByFormIdAndIdElastic() throws Exception
    {
        ReflectionTestUtils.setField(mockFormDataServiceImpl,"elasticEnable",true);
        List<Map<String,Object>> list = new ArrayList<>();
        LinkedHashMap<String,Object> data = new LinkedHashMap<>();
        List<LinkedHashMap> data1 = new ArrayList();
        LinkedHashMap linkedHashMap = new LinkedHashMap<>();
        LinkedHashMap linkedHashMap1 = new LinkedHashMap<>();
        linkedHashMap1.put("abc","abc");
        linkedHashMap.put(FORM_DATA,linkedHashMap1);
        data1.add(linkedHashMap);
        data.put(FORM_DATA,data1);
        ArrayList list1 = new ArrayList<>();
        list1.add(data);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        list.add(testFormMetaData);
        Map<String, Object> testFormData2 = new HashMap<>();
        Map<String, Object> testFormData3 = new HashMap<>();
        testFormData3.put(FORM_DATA, linkedHashMap);
        testFormData2.put(NAME, NAME_VALUE);
        testFormData2.put(AGE,AGE_VALUE);
        testFormData2.put(DATA,AGE_VALUE);
        testFormData2.put(CONTENT,AGE_VALUE);
        testFormData2.put(PAGE,AGE_VALUE);
        testFormData2.put(SIZE,AGE_VALUE);
        testFormData2.put(TOTAL_PAGES,AGE_VALUE);
        testFormData2.put(TOTAL_ELEMENTS,1L);
        testFormData2.put(NUMBER_OF_ELEMENTS,AGE_VALUE);
        when(mockWebClientWrapper.createWebClient(any())).thenReturn(mockWebClient);
        when(mockObjectMapper.readValue(anyString(), eq(Map.class))).thenReturn(testFormData2);
        when(mockObjectMapper.convertValue(any(), eq(Map.class))).thenReturn(testFormData3);
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        LinkedHashMap<String, Object> testFormData = new LinkedHashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        String responseTest =RESPONSE_VALUE_17;
        Map<String,Object> responseMapTest =new HashMap<>();
        ArrayList contentListTest =new ArrayList();
        LinkedHashMap<String,Object> singleContentTest =new LinkedHashMap<>();
        singleContentTest.put(FORM_ID,TEST_FORM_ID);
        singleContentTest.put(ID,TEST_ID);
        singleContentTest.put(VERSION,TEST_VERSION);
        singleContentTest.put(FORM_DATA,testFormData);
        singleContentTest.put(FORM_META_DATA,testFormMetaData);
        singleContentTest.put(CREATED_BY_NAME,CREATED_BY_USER_NAME);
        singleContentTest.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        singleContentTest.put(CREATED_BY_ID,BIGINTEGER_ID);
        singleContentTest.put(UPDATED_BY_ID,BIGINTEGER_ID);
        singleContentTest.put(CREATED_ON,TEST_CREATED_ON);
        singleContentTest.put(UPDATED_ON,TEST_UPDATED_ON);
        contentListTest.add(singleContentTest);
        Map<String,Object> dataMapTest =new HashMap<>();
        dataMapTest.put(CONTENT, contentListTest);
        dataMapTest.put(TOTAL_PAGES, ONE);
        dataMapTest.put(TOTAL_ELEMENTS,ONE);
        dataMapTest.put(PAGE,ZERO);
        dataMapTest.put(SIZE,PAGE_SIZE);
        dataMapTest.put(NUMBER_OF_ELEMENTS,ONE);
        responseMapTest.put(DATA, dataMapTest);
        responseMapTest.put(SUCCESS,true);
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(GET),any())).thenReturn(responseTest);
        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
        Assertions.assertNotNull(mockFormDataServiceImpl.getFormDataByFormIdAndId(TEST_FORM_ID,TEST_FORM_ID, null));
    }

    @Test
    void getFormDataByFormIdAndId() throws Exception
    {
        ReflectionTestUtils.setField(mockFormDataServiceImpl,"elasticEnable",true);
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        String responseTest =RESPONSE_VALUE_17;
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        LinkedHashMap<String, Object> testFormData = new LinkedHashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        Map<String,Object> responseMapTest =new HashMap<>();
        ArrayList contentListTest =new ArrayList();
        LinkedHashMap<String,Object> singleContentTest =new LinkedHashMap<>();
        singleContentTest.put(FORM_ID,TEST_FORM_ID);
        singleContentTest.put(ID,TEST_ID);
        singleContentTest.put(VERSION,TEST_VERSION);
        singleContentTest.put(FORM_DATA,testFormData);
        singleContentTest.put(FORM_META_DATA,testFormMetaData);
        singleContentTest.put(CREATED_BY_NAME,CREATED_BY_USER_NAME);
        singleContentTest.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        singleContentTest.put(CREATED_BY_ID,BIGINTEGER_ID);
        singleContentTest.put(UPDATED_BY_ID,BIGINTEGER_ID);
        singleContentTest.put(CREATED_ON,TEST_CREATED_ON);
        singleContentTest.put(UPDATED_ON,TEST_UPDATED_ON);
        contentListTest.add(singleContentTest);
        Map<String,Object> dataMapTest =new HashMap<>();
        dataMapTest.put(CONTENT, contentListTest);
        dataMapTest.put(TOTAL_PAGES, ONE);
        dataMapTest.put(TOTAL_ELEMENTS,ONE);
        dataMapTest.put(PAGE,ZERO);
        dataMapTest.put(SIZE,PAGE_SIZE);
        dataMapTest.put(NUMBER_OF_ELEMENTS,ONE);
        responseMapTest.put(DATA, dataMapTest);
        responseMapTest.put(SUCCESS,true);
        responseMapTest.put(MESSAGE,ELASTIC_DATA_FETCHED_SUCCESSFULLY);
        Date currentDate = new Date();
        Document document = new Document("version",1);
        document.append("formData",STRING);
        document.append("formMetaData",STRING);
        document.append(UNDERSCORE_ID,"1");
        document.append(VERSION,"1");
        document.append(CREATED_ON,currentDate);
        document.append(UPDATED_ON,currentDate);
        document.append(CREATED_BY_ID,"1");
        document.append(UPDATED_BY_ID,"1");
        document.append(CREATED_BY_NAME,STRING);
        document.append(UPDATED_BY_NAME,STRING);
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),eq(GET),any())).thenReturn(responseTest);
        Mockito.when(mockObjectMapper.readValue(responseTest,Map.class)).thenReturn(responseMapTest);
        Mockito.when(mockObjectMapper.convertValue(responseMapTest.get(DATA),Map.class)).thenReturn(dataMapTest);
        Assertions.assertNotNull(mockFormDataServiceImpl.getFormDataByFormIdAndId(TEST_FORM_ID, TEST_FORM_ID,null));
     }

    @Test
    void deleteAllFormDataByFormIdTest()
    {
        ReflectionTestUtils.setField(mockFormDataServiceImpl, ELASTIC_ENABLE, true);
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        String responseTest =RESPONSE_VALUE_30;
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),any(),any())).thenReturn(responseTest);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        Map<String,Object> responseMapTest =new HashMap<>();
        Map<String,Object> dataMapTest =new HashMap<>();
        dataMapTest.put(FORM_ID,TEST_FORM_ID);
        dataMapTest.put(ID,TEST_ID);
        dataMapTest.put(VERSION,TEST_VERSION);
        dataMapTest.put(FORM_DATA,testFormData);
        dataMapTest.put(FORM_META_DATA,testFormMetaData);
        dataMapTest.put(CREATED_BY_NAME,CREATED_BY_USER_NAME);
        dataMapTest.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        dataMapTest.put(CREATED_BY_ID,BIGINTEGER_ID);
        dataMapTest.put(UPDATED_BY_ID,BIGINTEGER_ID);
        dataMapTest.put(CREATED_ON,TEST_CREATED_ON);
        dataMapTest.put(UPDATED_ON,TEST_UPDATED_ON);
        responseMapTest.put(DATA, dataMapTest);
        responseMapTest.put(SUCCESS,true);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ +TEST_FORM_ID)).thenReturn(true);
        mockFormDataServiceImpl.deleteAllFormDataByFormId(TEST_FORM_ID);
        verify(mockWebClientWrapper,times(1)).webclientRequest(any(),any(),any(),any());
    }

    @Test
    void deleteFormDataByFormIdAndIdTest()
    {
        ReflectionTestUtils.setField(mockFormDataServiceImpl, ELASTIC_ENABLE, true);
        Mockito.when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        Mockito.when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        String responseTest =RESPONSE_VALUE_31;
        Mockito.when(mockWebClientWrapper.webclientRequest(any(),any(),any(),any())).thenReturn(responseTest);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        Map<String,Object> responseMapTest =new HashMap<>();
        Map<String,Object> dataMapTest =new HashMap<>();
        dataMapTest.put(FORM_ID,TEST_FORM_ID);
        dataMapTest.put(ID,TEST_ID);
        dataMapTest.put(VERSION,TEST_VERSION);
        dataMapTest.put(FORM_DATA,testFormData);
        dataMapTest.put(FORM_META_DATA,testFormMetaData);
        dataMapTest.put(CREATED_BY_NAME,CREATED_BY_USER_NAME);
        dataMapTest.put(UPDATED_BY_NAME,UPDATED_BY_USER_NAME);
        dataMapTest.put(CREATED_BY_ID,BIGINTEGER_ID);
        dataMapTest.put(UPDATED_BY_ID,BIGINTEGER_ID);
        dataMapTest.put(CREATED_ON,TEST_CREATED_ON);
        dataMapTest.put(UPDATED_ON,TEST_UPDATED_ON);
        responseMapTest.put(DATA, dataMapTest);
        responseMapTest.put(SUCCESS,true);
        Mockito.when(mockMongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ +TEST_FORM_ID)).thenReturn(true);
        Mockito.when(mockMongoTemplate.getCollection(anyString())).thenReturn(mockMongoCollection);
        Mockito.when(mockMongoCollection.deleteMany(any())).thenReturn(mockDeleteResult);
        Mockito.when(mockDeleteResult.getDeletedCount()).thenReturn(Long.valueOf(1));
        mockFormDataServiceImpl.deleteFormDataByFormIdAndId(TEST_FORM_ID,TEST_ID);
        verify(mockWebClientWrapper,times(1)).webclientRequest(any(),any(),any(),any());
    }
}





