package com.techsophy.tsf.runtime.form.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FormModelerConstants
{
    //DatabaseChangeLog
    public static final String ORDER_1="1";
    public static final String ORDER_2="2";
    public static final String ORDER_3="3";
    public static final String ORDER_4="4";
    public static final String SYSTEM_VERSION_1="1";
    public static final String ADD_USER_FORM= "add-user-form01";
    public static final String EDIT_GROUP_FORM= "edit-group-form01";
    public static final String ADD_GROUP_FORM= "add-group-form01";
    public static final String EDIT_USER_FORM= "edit-user-form01";
    public static final String EXECUTION_IS_FAILED ="storing data using mongock is failed";
    public static final String TP_ADD_GROUP="TP_ADD_GROUP.json";
    public static final String TP_EDIT_USER="TP_EDIT_USER.json";
    public static final String TP_ADD_USER="TP_ADD_USER.json";
    public static final String TP_EDIT_GROUP="TP_EDIT_GROUP.json";

    //LoggingHandler
    public static final String CONTROLLER_CLASS_PATH = "execution(* com.techsophy.tsf.runtime.form.controller..*(..))";
    public static final String SERVICE_CLASS_PATH= "execution(* com.techsophy.tsf.runtime.form.service..*(..))";
    public static final String EXCEPTION = "ex";
    public static final String IS_INVOKED_IN_CONTROLLER= "{} () is invoked in controller ";
    public static final String IS_INVOKED_IN_SERVICE= "{} () is invoked in service ";
    public static final String EXECUTION_IS_COMPLETED_IN_CONTROLLER="{} () execution is completed  in controller";
    public static final String EXECUTION_IS_COMPLETED_IN_SERVICE="{} () execution is completed  in service";
    public static final String EXCEPTION_THROWN="An exception has been thrown in ";
    public static final String CAUSE="Cause : ";
    public static final String BRACKETS_IN_CONTROLLER="() in controller";
    public static final String BRACKETS_IN_SERVICE="() in service";

    //ACLConstants
    public static final String ALLOW="allow";

    //JWTRoleConverter
    public static final String AWGMENT_ROLES_MISSING_IN_CLIENT_ROLES ="AwgmentRoles are missing in clientRoles";
    public static final String CLIENT_ROLES_MISSING_IN_USER_INFORMATION="ClientRoles are missing in the userInformation";

    /*CustomFilterConstants*/
    public static final String AUTHORIZATION="Authorization";

    //JWTRoleConverter
    public static final String CLIENT_ROLES="clientRoles";
    public static final String USER_INFO_URL= "/protocol/openid-connect/userinfo";

    /*LocaleConfig Constants*/
    public static final String ACCEPT_LANGUAGE = "Accept-Language";
    public static final String BASENAME_ERROR_MESSAGES = "classpath:errorMessages";
    public static final String BASENAME_MESSAGES = "classpath:messages";
    public static final Long CACHEMILLIS = 3600L;
    public static final Boolean USEDEFAULTCODEMESSAGE = true;

    /*TenantAuthenticationManagerConstants*/
    public static final String KEYCLOAK_ISSUER_URI = "${keycloak.issuer-uri}";

    // Roles
    public static final String HAS_ANY_AUTHORITY="hasAnyAuthority('";
    public static final String HAS_ANY_AUTHORITY_ENDING="')";
    public static final String AWGMENT_RUNTIME_FORM_CREATE_OR_UPDATE = "awgment-runtime-form-create-or-update";
    public static final String AWGMENT_RUNTIME_FORM_READ = "awgment-runtime-form-read";
    public static final String AWGMENT_RUNTIME_FORM_DELETE = "awgment-runtime-form-delete";
    public static final String AWGMENT_RUNTIME_FORM_ALL = "awgment-runtime-form-all";
    public static final String OR=" or ";
    public static final String CREATE_OR_ALL_ACCESS =HAS_ANY_AUTHORITY+ AWGMENT_RUNTIME_FORM_CREATE_OR_UPDATE +HAS_ANY_AUTHORITY_ENDING+OR+HAS_ANY_AUTHORITY+AWGMENT_RUNTIME_FORM_ALL+HAS_ANY_AUTHORITY_ENDING;
    public static final String READ_OR_ALL_ACCESS =HAS_ANY_AUTHORITY+ AWGMENT_RUNTIME_FORM_READ +HAS_ANY_AUTHORITY_ENDING+OR+HAS_ANY_AUTHORITY+AWGMENT_RUNTIME_FORM_ALL+HAS_ANY_AUTHORITY_ENDING;
    public static final String DELETE_OR_ALL_ACCESS =HAS_ANY_AUTHORITY+ AWGMENT_RUNTIME_FORM_DELETE +HAS_ANY_AUTHORITY_ENDING+OR+HAS_ANY_AUTHORITY+AWGMENT_RUNTIME_FORM_ALL+HAS_ANY_AUTHORITY_ENDING;

    //FormDataControllerConstants
    public static final String RELATIONS="relations";
    public static final String FORM_DATA_URL="/form-data";
    public static final String FORM_DATA_ID_URL ="/form-data/{formId}";
    public static final String SORT_ORDER="sort-order";
    public static final String FILTER = "filter";
    public static final String FORM_DATA_AGGREGATE="/form-data/aggregate";
    public static final String GROUP_BY="groupBy";
    public static final String OPERATION="operation";
    public static final String COUNT="count";

    //FormDataAuditControllerConstants
    public static final String HISTORY ="/history";
    public static final String FORM_DATA_DOCUMENT_ID_URL="/form-data/document-id";
    public static final String FORM_DATA_ID ="formDataId";
    public static final String VERSION="version";
    public static final String AUDIT="_audit";

    /*FormControllerConstants*/
    public static final String BASE_URL = "/form-runtime";
    public static final String VERSION_V1 = "/v1";
    public static final String FORMS_URL = "/forms";
    public static final String FORM_BY_ID_URL = "/forms/{id}";
    public static final String SEARCH_FORM_URL = "/forms/search";
    public static final String ID = "id";
    public static final String INCLUDE_CONTENT = "include-content";
    public static final String ID_OR_NAME_LIKE = "idOrNameLike";
    public static final String TYPE = "type";
    public static final String COMPONENT="component";
    public static final String DEPLOY_FORM_SUCCESS ="DEPLOY_FORM_SUCCESS";
    public static final String DEPLOY_COMPONENT_SUCCESS ="DEPLOY_COMPONENT_SUCCESS";
    public static final String GET_FORM_SUCCESS="GET_FORM_SUCCESS";
    public static final String GET_COMPONENT_SUCCESS="GET_COMPONENT_SUCCESS";
    public static final String DELETE_FORM_SUCCESS="DELETE_FORM_SUCCESS";
    public static final String DELETE_COMPONENT_SUCCESS="DELETE_COMPONENT_SUCCESS";
    public static final String SAVE_FORM_DATA_SUCCESS="SAVE_FORM_DATA_SUCCESS";
    public static final String UPDATE_FORM_DATA_SUCCESS="UPDATE_FORM_DATA_SUCCESS";
    public static final String GET_FORM_DATA_SUCCESS="GET_FORM_DATA_SUCCESS";
    public static final String DELETE_FORM_DATA_SUCCESS="DELETE_FORM_DATA_SUCCESS";
    public static final String DELETE_ALL_FORM_DATA_SUCCESS="DELETE_ALL_FORM_DATA_SUCCESS";
    public static final String FORM="form";

    /*FormSchemaConstants*/
    public static final String NAME_NOT_BLANK = "Name should not be blank";
    public static final String ID_NOT_NULL = "Id should not be null";
    public static final String VERSION_NOT_BLANK="Version should not be blank";
    public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String TIME_ZONE = "UTC";
    public static final String FORM_ID_NOT_BLANK="FormId should not be blank";
    public static final String UNDERSCORE_ID="_id";

    /*FormDefinitionConstants*/
    public static final String TP_FORM_DEFINITION_COLLECTION = "tp_runtime_form_definition";

    /*FormDefinitionCustomRepositoryConstants*/
    public static final String FORM_ID ="formId";
    public static final String FORM_NAME ="name";
    public static final String FORM_TYPE ="type";
    public static final String COMPONENTS="components";

    //FormDataService
    public static final String TP_RUNTIME_FORM_DATA ="tp_runtime_form_data_";
    public static final String FORM_DATA="formData";
    public static final String CREATED_BY_ID="createdById";
    public static final String CREATED_BY_NAME="createdByName";
    public static final String UPDATED_BY_ID="updatedById";
    public static final String UPDATED_BY_NAME="updatedByName";
    public static final String FORM_META_DATA="formMetaData";
    public static final String  DOT=".";
    public static final String COMMA=",";
    public static final String NULL="null";
    public static final String DEFAULT_PAGE_LIMIT="${default.pagelimit}";
    public static final String SLASH="/";
    public static final String PARAM_SOURCE="?source=";
    public static final String PARAM_INDEX_NAME="?indexName=";
    public static final String REGEX_PATTERN_1="^\"|\"$";
    public static final String TOTAL_PAGES="totalPages";
    public static final String TOTAL_ELEMENTS="totalElements";
    public static final String NUMBER_OF_ELEMENTS="numberOfElements";
    public static final String ELASTIC_SOURCE ="${elastic.source}";
    public static final String ELASTIC_ENABLE="${elastic.enable}";
    public static final String Q="q";
    public static final String CONTAINS_ATLEAST_ONE_ALPHABET=".*[a-zA-Z]+.*";
    public static final String COUNT_WORDS="\\s+";
    public static final String METADATA="metaData";
    public static final String LOOKUP="    $lookup: {\n";
    public static final String FROM_MATCH="        from: '%s',\n";
    public static final String LET="        'let': {\n";
    public static final String LOCAL_ID="            localId: '$_id'\n";
    public static final String CLOSING_BRACKET="        },\n";
    public static final String PIPELINE="        pipeline: [{\n";
    public static final String MATCH="            $match: {\n";
    public static final String EXPR="                $expr: {\n";
    public static final String OR_CONDITION="                    $or: [{\n";
    public static final String EQ_EXPRESSION="                            $eq: [\n";
    public static final String DOLLAR_LOCALID="                                '$$localId',\n";
    public static final String  NEXT_LINE="                                {\n";
    public static final String CONVERT="                                    $convert: {\n";
    public static final String INPUT="                                        input: '$%s',\n";
    public static final String TO_LONG="                                        to: 'long',\n";
    public static final String ON_ERROR="                                        onError: '0',\n";
    public static final String ON_NULL="                                        onNull: '0'\n";
    public static final String CLOSING_BRACKET_NEXT_LINE="                                    }\n";
    public static final String CLOSE_ARRAY="                            ]\n";
    public static final String CLOSE_BRACKET_COMMA_NEXT_LINE="                        },\n";
    public static final String OPENING_BRACKET_NEXT_LINE="                        {\n";
    public static final String DOLLAR_STRING_REF_NEXT_LINE="                                '$%s'\n";
    public static final String CLOSE_MAP_ARRAY_NEXT_LINE="        }],\n";
    public static final String AS_STRING_REFERENCE="        as: '%s'\n";
    public static final String MONGO_AGGREGATION_STAGE_PIPELINE_1= "{\n" +
         LOOKUP+
            FROM_MATCH +
         LET+
            LOCAL_ID +
            CLOSING_BRACKET+
            PIPELINE +
            MATCH +
            EXPR+
            OR_CONDITION+
         EQ_EXPRESSION+
        DOLLAR_LOCALID +
            NEXT_LINE +
            CONVERT+
            INPUT+
            TO_LONG +
            ON_ERROR +
            ON_NULL+
            CLOSING_BRACKET_NEXT_LINE+
            CLOSING_BRACKET_NEXT_LINE +
            CLOSE_ARRAY+
            CLOSE_BRACKET_COMMA_NEXT_LINE+
            OPENING_BRACKET_NEXT_LINE +
        EQ_EXPRESSION +
        DOLLAR_LOCALID +
            DOLLAR_STRING_REF_NEXT_LINE+
        CLOSE_ARRAY +
        CLOSING_BRACKET_NEXT_LINE +
        CLOSE_ARRAY +
        CLOSING_BRACKET_NEXT_LINE +
            CLOSING_BRACKET_NEXT_LINE +
            CLOSE_MAP_ARRAY_NEXT_LINE+
            AS_STRING_REFERENCE +
        CLOSING_BRACKET_NEXT_LINE +
        "}";

    //Constants for Requestmapping
    public static final String ELASTIC_VERSION1 = "/elastic/v1";

    /*TokenUtilsAndWebclientWrapperConstants*/
    public static final String PREFERED_USERNAME="preferred_username";
    public static final String EMPTY_STRING="";
    public static final String BEARER="Bearer ";
    public static final String REGEX_SPLIT="\\.";
    public static final String ISS="iss";
    public static final String URL_SEPERATOR="/";
    public static final int SEVEN=7;
    public static final int ONE=1;
    public static final String CONTENT_TYPE="Content-Type";
    public static final String APPLICATION_JSON ="application/json";
    public static final String GET="GET";
    public static final String PUT="PUT";
    public static final String DELETE="DELETE";
    public static final String POST = "POST";

    //TokenUtilsConstants
    public static final String CREATED_ON = "createdOn";
    public static final String UPDATED_ON = "updatedOn";
    public static final String DESCENDING = "desc";
    public static final String COLON = ":";
    public static final String CREATED_ON_ASC = "createdOn: ASC";

    //UserDetailsConstants
    public static final String GATEWAY_URI = "${gateway.uri}";
    public static final String GATEWAY = "gateway ";
    public static final String ACCOUNT_URL = "/accounts/v1/users";
    public static final String FILTER_COLUMN = "?filter-column=loginId&filter-value=";
    public static final String ONLY_MANDATORY_FIELDS_TRUE = "&only-mandatory-fields=true";
    public static final String DATA = "data";
    public static final String PAGE = "page";
    public static final String SIZE = "size";
    public static final String SORT_BY = "sort-by";
    public static final String LOGGED_USER = "loggeduser";
    public static final String TOKEN = "token";

    /*MainMethodConstants*/
    public static final String PACKAGE_NAME = "com.techsophy.tsf.runtime.*";
    public static final String COMMOMN_PACKAGE_NAME = "com.techsophy.tsf.commons.user";
    public static final String MULTI_TENANCY_PACKAGE_NAME = "com.techsophy.multitenancy.mongo.*";
    public static final String VERSION_1 = "1.0";
    public static final String GATEWAY_URL = "${gateway.uri}";
    public static final String RUNTIME_FORM = "tp-app-runtime-form";
    public static final String RUNTIME_FORM_MODELER_API_VERSION_1 = "Runtime Form API v1.0";
    public static final String SERVICE = "service";
    public static final String DATABASE_NAME = "techsophy-platform";
}
