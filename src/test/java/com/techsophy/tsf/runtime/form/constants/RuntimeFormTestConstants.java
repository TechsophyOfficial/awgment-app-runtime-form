package com.techsophy.tsf.runtime.form.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RuntimeFormTestConstants {
    //GlobalMessageSourceConstants
    public static final String TEST_ACTIVE_PROFILE = "test";
    public static final String KEY = "key";
    public static final String ARGS = "args";

    //FORMCONTROLLERTESTConstants
    public static final String errorCode = "123";
    public static final String TENANT = "tenant";
    public static final String TEST_FORMS_DATA_1 = "testdata/form-schema1.json";
    public static final String TEST_FORMS_DATA_2 = "testdata/form-schema2.json";
    public static final String BASE_URL_TEST = "/form-runtime";
    public static final String VERSION_V1_TEST = "/v1";
    public static final String FORM_BY_ID_URL_TEST = "/forms/{id}";
    public final static String TYPE_FORM = "form";
    public final static String FORM_ID_NOT_FOUND_WITH_GIVEN_ID = "Form id not found with given id";
    public final static String ENTITY_NOT_FOUND_WITH_GIVEN_ID = "Entity not found with given id";
    public static final String TEST_RUNTIME_FORM_DATA_1 = "testdata/runtime-form-data1.json";
    public static final String TEST_RUNTIME_FORM_DATA_AUDIT_1 = "testdata/runtime-form-data-audit1.json";
    public static final String TEST_FORM_ID = "123";
    public static final String TEST_FORM_DATA_ID = "123";
    public static final String TEST_RELATIONS = "994102731543871488:orderId,994122561634369536:parcelId";
    public static final String USER_DETAILS_NOT_FOUND_WITH_GIVEN_ID = "UserDetails Not found with given id";
    public final static Map<String, Object> TEST_FORM_DATA = null;
    public static final Map<String, Object> TEST_FORM_META_DATA = null;
    public static final String LOCALE_EN = "en";
    public static final String ZERO = "0";
    public static final String FIVE = "5";
    public static final String Q = "123";
    public static final String FORM_VERSION = "formVersion";
    public static final String FORM_DATA = "formData";
    public static final String NAME = "name";
    public static final String NAME_VALUE = "abc efg";
    public static final String AGE = "age";
    public static final String DATA = "data";
    public static final String CONTENT = "content";
    public static final String AGE_VALUE = "23";
    public static final String PERSON_AGE = "100";
    public static final String UNDERSCORE_ID_VALUE = "123";
    public static final String CREATED_BY_USER_ID = "847117072898674688";
    public static final String CREATED_BY_USER_NAME = "Kaza tejaswini";
    public static final String UPDATED_BY_USER_ID = "847117072898674688";
    public static final String UPDATED_BY_USER_NAME = "kaza tejaswini";
    public static final String TEST_FORM_DATA_ID_VALUE = "1";
    public static final String SEARCH_STRING = "searchString";
    public static final String DEFAULT_PAGE_LIMIT = "defaultPageLimit";
    public static final String TEST_ID_VALUE = "123";
    public static final String ELASTIC_ENABLE = "elasticEnable";
    public static final String ELASTIC_SOURCE = "elasticSource";
    public static final String GATEWAY_API = "gatewayApi";
    public static final String GATEWAY_API_VALUE = "https://dummy";
    public static final String RESPONSE_VALUE = "{\"data\":null}";
    public static final String SUCCESS = "success";
    public static final String MESSAGE = "message";
    public static final String ELASTIC_DATA_FETCHED_SUCCESSFULLY = "Elastic data fetched successfully";
    public static final String RESPONSE_VALUE_2 = "{\"data\":{\"content\":[{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"}],\"totalPages\":1,\"totalElements\":1,\"page\":0,\"size\":20,\"numberOfElements\":1},\n" +
            "                    \"success\":true,\"message\":\"Data retrieved successfully\"}";
    public static final String RESPONSE_VALUE_3 = "{\"data\":{\"content\":[{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"}],\"totalPages\":1,\"totalElements\":1,\"page\":0,\"size\":20,\"numberOfElements\":1},\n" +
            "{\"data\":{\"content\":[{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"}],\"totalPages\":1,\"totalElements\":1,\"page\":0,\"size\":20,\"numberOfElements\":1},\n" +
            "                    \"success\":true,\"message\":\"Data retrieved successfully\"}";
    public static final String RESPONSE_VALUE_4 = "{\"data\":{\"content\":[{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"}],\"totalPages\":1,\"totalElements\":1,\"page\":0,\"size\":20,\"numberOfElements\":1},\n" +
            "                    \"success\":true,\"message\":\"Data retrieved successfully\"}";
    public static final String RESPONSE_VALUE_5 = "{\"data\":{\"content\":[{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"}],\"totalPages\":1,\"totalElements\":1,\"page\":0,\"size\":20,\"numberOfElements\":1},\n" +
            "                    \"success\":true,\"message\":\"Data retrieved successfully\"}";
    public static final String RESPONSE_VALUE_6 = "{\"data\":{\"content\":[{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"}],\"totalPages\":1,\"totalElements\":1,\"page\":0,\"size\":20,\"numberOfElements\":1},\n" +
            "                    \"success\":true,\"message\":\"Data retrieved successfully\"}";
    public static final String RESPONSE_VALUE_7 = "{\"data\":{\"content\":[{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"}],\"totalPages\":1,\"totalElements\":1,\"page\":0,\"size\":20,\"numberOfElements\":1},\n" +
            "                    \"success\":true,\"message\":\"Data retrieved successfully\"}";
    public static final String RESPONSE_VALUE_8 = "{\"data\":{\"content\":[{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"}],\"totalPages\":1,\"totalElements\":1,\"page\":0,\"size\":20,\"numberOfElements\":1},\n" +
            "                    \"success\":true,\"message\":\"Data retrieved successfully\"}";
    public static final String ONE = "1";
    public static final String PAGE_SIZE = "20";
    public static final String RESPONSE_VALUE_9 = " {\"data\":{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"},\"success\":true,\"message\":\"Elastic data fetched successfully\"}";
    public static final String RESPONSE_VALUE_10 = " {\"data\":{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"},\"success\":true,\"message\":\"Elastic data fetched successfully\"}";
    public static final String RESPONSE_VALUE_11 = " {\"data\":{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"},\"success\":true,\"message\":\"Elastic data fetched successfully\"}";
    public static final String RESPONSE_VALUE_12 = "{\"data\":{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"},\"success\":true,\"message\":\"Elastic data fetched successfully\"}";
    public static final String RESPONSE_VALUE_13 = "{\"data\":{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"},\"success\":true,\"message\":\"Elastic data fetched successfully\"}";
    public static final String RESPONSE_VALUE_14 = "{\"data\":{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"},\"success\":true,\"message\":\"Elastic data fetched successfully\"}";
    public static final String TEST_EMAIL_ADDRESS = "abc@gmail.com";
    public static final String RESPONSE_VALUE_15 = " {\"data\":{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"},\"success\":true,\"message\":\"Elastic data fetched successfully\"}";
    public static final String RESPONSE_VALUE_16 = "{\"data\":{\"content\":[{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"}],\"totalPages\":1,\"totalElements\":1,\"page\":0,\"size\":20,\"numberOfElements\":1},\n" +
            "                    \"success\":true,\"message\":\"Data retrieved successfully\"}";
    public static final String RESPONSE_VALUE_17 = "{\"data\":{\"content\":[{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"}],\"totalPages\":1,\"totalElements\":1,\"page\":0,\"size\":20,\"numberOfElements\":1},\n" +
            "                    \"success\":true,\"message\":\"Data retrieved successfully\"}";
    public static final String RESPONSE_VALUE_18 = "{\"data\":{\"content\":[{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"}],\"totalPages\":1,\"totalElements\":1,\"page\":0,\"size\":20,\"numberOfElements\":1},\n" +
            "                    \"success\":true,\"message\":\"Data retrieved successfully\"}";
    public static final String RESPONSE_VALUE_19 = "{\"data\":{\"content\":[{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"}],\"totalPages\":1,\"totalElements\":1,\"page\":0,\"size\":20,\"numberOfElements\":1},\n" +
            "                    \"success\":true,\"message\":\"Data retrieved successfully\"}";
    public static final String RESPONSE_VALUE_20 = "{\"data\":{\"content\":[{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"}],\"totalPages\":1,\"totalElements\":1,\"page\":0,\"size\":20,\"numberOfElements\":1},\n" +
            "                    \"success\":true,\"message\":\"Data retrieved successfully\"}";
    public static final String RESPONSE_VALUE_21 = "{\"data\":{\"content\":[{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"}],\"totalPages\":1,\"totalElements\":1,\"page\":0,\"size\":20,\"numberOfElements\":1},\n" +
            "                    \"success\":true,\"message\":\"Data retrieved successfully\"}";
    public static final String RESPONSE_VALUE_22 = "{\"data\":{\"content\":[{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"}],\"totalPages\":1,\"totalElements\":1,\"page\":0,\"size\":20,\"numberOfElements\":1},\n" +
            "                    \"success\":true,\"message\":\"Data retrieved successfully\"}";
    public static final String RESPONSE_VALUE_23 = "{\"data\":{\"content\":[{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"}],\"totalPages\":1,\"totalElements\":1,\"page\":0,\"size\":20,\"numberOfElements\":1},\n" +
            "                    \"success\":true,\"message\":\"Data retrieved successfully\"}";
    public static final String RESPONSE_VALUE_24 = "{\"data\":{\"content\":[{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"}],\"totalPages\":1,\"totalElements\":1,\"page\":0,\"size\":20,\"numberOfElements\":1},\n" +
            "                    \"success\":true,\"message\":\"Data retrieved successfully\"}";
    public static final String RESPONSE_VALUE_25 = "{\"data\":{\"content\":[{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"}],\"totalPages\":1,\"totalElements\":1,\"page\":0,\"size\":20,\"numberOfElements\":1},\n" +
            "                    \"success\":true,\"message\":\"Data retrieved successfully\"}";
    public static final String RESPONSE_VALUE_26 = "{\"data\":{\"content\":[{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"}],\"totalPages\":1,\"totalElements\":1,\"page\":0,\"size\":20,\"numberOfElements\":1},\n" +
            "                    \"success\":true,\"message\":\"Data retrieved successfully\"}";
    public static final String RESPONSE_VALUE_27 = "{\"data\":{\"content\":[{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"}],\"totalPages\":1,\"totalElements\":1,\"page\":0,\"size\":20,\"numberOfElements\":1},\n" +
            "                    \"success\":true,\"message\":\"Data retrieved successfully\"}";
    public static final String RESPONSE_VALUE_28 = "{\"data\":{\"content\":[{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"}],\"totalPages\":1,\"totalElements\":1,\"page\":0,\"size\":20,\"numberOfElements\":1},\n" +
            "                    \"success\":true,\"message\":\"Data retrieved successfully\"}";
    public static final String RESPONSE_VALUE_29 = "{\"data\":{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"},\"success\":true,\"message\":\"Elastic data fetched successfully\"}";
    public static final String RESPONSE_VALUE_30 = " {\"data\":{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"},\"success\":true,\"message\":\"Elastic data fetched successfully\"}";
    public static final String RESPONSE_VALUE_31 = " {\"data\":{\"formId\":\"928232634435125248\",\"createdByName\":\"tejaswini kaza\",\"formMetadata\":{\"formVersion\":\"101\"},\"updatedByName\":\"tejaswini kaza\",\"formData\":{\"name\":\"akhil\",\"age\":\"205\"},\"id\":\"945588115922505728\",\"updatedOn\":\"2022-02-22T07:49:35.014336Z\",\"updatedById\":\"910797699334508544\",\"version\":\"1\",\"createdOn\":\"2022-02-22T07:49:35.013250Z\",\"createdById\":\"910797699334508544\"},\"success\":true,\"message\":\"Elastic data fetched successfully\"}";

    //FormServiceConstants
    public final static String MIN_LENGTH = "minLength";
    public final static String MAX_LENGTH = "maxLength";
    public final static String TEST_TYPE_COMPONENT = "component";
    public final static String TEST_TYPE_FORM = "form";
    public static final String TEST_FORMS_DATA = "testdata/form-schema1.json";
    public final static String TEST_ID_OR_NAME_LIKE = "abc";
    public final static @NotNull String TEST_ID = "1";
    public final static @NotNull String TEST_NAME = "form1";
    public final static Map<String, Object> TEST_COMPONENTS = null;
    public final static Map<String, Object> TEST_PROPERTIES = null;
    public final static @NotNull Integer TEST_VERSION = 1;
    public final static Boolean IS_DEFAULT_VALUE = true;
    public final static String TEST_CREATED_BY_ID = "1";
    public final static String STRING = "abc";
    public final static String COLLECTION = "tp_runtime_form_data_123";
    public final static Instant TEST_CREATED_ON = Instant.now();
    public final static String TEST_UPDATED_BY_ID = "1";
    public final static Instant TEST_UPDATED_ON = Instant.now();
    public final static String TEST_CREATED_BY_NAME = "user1";
    public final static String TEST_UPDATED_BY_NAME = "user1";
    public static final String BIGINTEGER_ID = "847117072898674688";
    public static final String USER_FIRST_NAME = "tejaswini";
    public static final String USER_LAST_NAME = "Kaza";
    public static final String MAIL_ID = "tejaswini.k@techsophy.com";
    public static final String TEST_TOKEN = "testdata/token.txt";


    //FormDataServiceConstants
    public static final String TEST_SORT_BY = "createdOn";
    public static final String TEST_SORT_ORDER = "desc";
    public static final String FILTER="name:abc";
    public static final String TEST_FILTER = "id:123,version:1";
    public static final String TEST_GROUP_BY = "formData.name";
    public static final String TEST_OPERATION = "count";
    public static final String TEST_COUNT_VALUE = "4";

    //TokenUtilsTest
    public static final String TOKEN_TXT_PATH = "testdata/token.txt";
    public static final String TECHSOPHY_PLATFORM = "techsophy-platform";
    public static final String PREFERED_USER_NAME = "preferred_username";
    public static final String ALG = "alg";
    public static final String NONE = "none";

    //INITILIZATION CONSTANTS
    public static final String DEPARTMENT = "department";

    public static final String EMAIL_ID = "emailId";
    public static final String MOBILE_NUMBER = "mobileNumber";
    public static final String LAST_NAME = "lastName";
    public static final String FIRST_NAME = "firstName";
    public static final String USER_NAME = "userName";



    public static final String NUMBER = "1234567890";

    //UserDetailsTestConstants
    public static final String ABC = "abc:abc,abc:abc";
    public static final String USER_DETAILS_RETRIEVED_SUCCESS
            = "User details retrieved successfully";
    public static final String INITIALIZATION_DATA = "{\"data\":[{\"id\":\"847117072898674688\",\"userName\":\"tejaswini\",\"firstName\":\"Kaza\",\"lastName\":\"Tejaswini\",\"mobileNumber\":\"1234567890\",\"emailId\":\"tejaswini.k@techsophy.com\",\"department\":null,\"createdById\":null,\"createdByName\":null,\"createdOn\":null,\"updatedById\":null,\"updatedByName\":null,\"updatedOn\":null}],\"success\":true,\"message\":\"User details retrieved successfully\"}\n";
    public static final String LOGGED_USER_ID = "847117072898674688";


    //WEBCLIENTWrapperTestConstants
    public static final String LOCAL_HOST_URL = "http://localhost:8080";
    public static final String TOKEN = "token";
    public static final String TEST = "test";
    public static final String PUT = "put";
    public static final String DELETE = "delete";
}

