package com.techsophy.tsf.runtime.form.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorConstants
{
    public static final String FORM_NOT_FOUND_EXCEPTION= "AWGMENT-RUNTIME-FORM-1001";
    public static final String ENTITY_ID_NOT_FOUND_EXCEPTION = "AWGMENT-RUNTIME-FORM-1002";
    public static final String TOKEN_VERIFICATION_FAILED= "AWGMENT-RUNTIME-FORM-1003";
    public static final String INVALID_TOKEN = "AWGMENT-RUNTIME-FORM-1004";
    public static final String AUTHENTICATION_FAILED= "AWGMENT-RUNTIME-FORM-1005";
    public static final String UNABLE_GET_TOKEN= "AWGMENT-RUNTIME-FORM-1006";
    public static final String NO_COMPONENTS_IN_SCHEMA= "AWGMENT-RUNTIME-FORM-1007";
    public static final String LOGGED_IN_USER_NOT_FOUND= "AWGMENT-RUNTIME-FORM-1008";
    public static final String TOKEN_NOT_NULL= "AWGMENT-RUNTIME-FORM-1009";
    public static final String USER_DETAILS_NOT_FOUND= "AWGMENT-RUNTIME-FORM-1010";
    public static final String USER_NOT_FOUND_BY_ID= "AWGMENT-RUNTIME-FORM-1011";
    public static final String INVALID_EMAIL_PATTERN= "AWGMENT-RUNTIME-FORM-1012";
    public static final String LOGGED_IN_USER_ID_NOT_FOUND= "AWGMENT-RUNTIME-FORM-1013";
    public static final String FORM_ID_CANNOT_BE_EMPTY= "AWGMENT-RUNTIME-FORM-1014";
    public static final String ID_CANNOT_BE_EMPTY="AWGMENT-RUNTIME-FORM-1032";
    public static final String FORM_DATA_DOES_NOT_EXISTS_WITH_GIVEN_FORMID= "AWGMENT-RUNTIME-FORM-1015";
    public static final String FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC = "AWGMENT-RUNTIME-FORM-1016";
    public static final String FORM_DATA_MISSING_MANDATORY_FIELDS= "AWGMENT-RUNTIME-FORM-1017";
    public static final String SORTBY_AND_SORTORDER_BOTH_SHOULD_BE_GIVEN= "AWGMENT-RUNTIME-FORM-1018";

    public static final String FORM_NOT_FOUND_WITH_GIVEN_FORM_ID= "AWGMENT-RUNTIME-FORM-1019";
    public static final String UNABLE_TO_SAVE_IN_ELASTIC_AND_DB= "AWGMENT-RUNTIME-FORM-1021";
    public static final String UNABLE_TO_DELETE_FORM_DATA_IN_ELASTIC_DB= "AWGMENT-RUNTIME-FORM-1022";
    public static final String FORM_DATA_HAS_DUPLICATE= "AWGMENT-RUNTIME-FORM-1023";
    public static final String FORM_DATA_MIN_LENGTH_CONDITION_FAILED= "AWGMENT-RUNTIME-FORM-1024";
    public static final String FORM_DATA_MAX_LENGTH_CONDITION_VIOLATED_BY_USER= "AWGMENT-RUNTIME-FORM-1025";
    public static final String FORM_DATA_MIN_VALUE_CONDITION_FAILED= "AWGMENT-RUNTIME-FORM-1026";
    public static final String FORM_DATA_MAX_VALUE_CONDITION_FAILED= "AWGMENT-RUNTIME-FORM-1027";
    public static final String FORM_DATA_INTEGER_FIELDS_CANNOT_CONTAIN_ALPHABETS= "AWGMENT-RUNTIME-FORM-1028";
    public static final String FORM_DATA_MIN_WORD_CONDITION_FAILED= "AWGMENT-RUNTIME-FORM-1029";
    public static final String FORM_DATA_MAX_WORD_CONDITION_EXCEEDED = "AWGMENT-RUNTIME-FORM-1030";
    public static final String SERVICE_NOT_AVAILABLE= "AWGMENT-RUNTIME-FORM-1031";
    public static final String UNABLE_TO_UPDATE_IN_MONGODB_AND_ELASTIC_DB="AWGMENT-RUNTIME-FORM-1033";
    public static final String FILTER_SHOULD_BE_GIVEN_ALONG_WITH_SORT_BY_SORT_ORDER="AWGMENT-RUNTIME-FORM-1034";
    public static final String FILTER_SHOULD_BE_GIVEN_ALONG_WITH_SORT_BY_SORT_ORDER_PAGINATION= "AWGMENT-RUNTIME-FORM-1035";
    public static final String UNABLE_TO_DELETE_FORM_DATA_IN_ELASTIC_DB_BY_FORMID_AND_ID="AWGMENT-RUNTIME-FORM-1036";
}
