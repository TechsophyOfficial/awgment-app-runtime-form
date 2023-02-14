package com.techsophy.tsf.runtime.form.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)

public class FormAclConstants {

    public static final String ACL ="/acl";
    public static final String FORMS ="/forms";
    public static final String FORMID ="/{formId}";
    public static final String FORM_ID ="formId";
    public static final String ACL_ID ="aclId";
    public static final String HAS_ANY_AUTHORITY="hasAnyAuthority('";
    public static final String HAS_ANY_AUTHORITY_ENDING="')";
    public static final String AWGMENT_RUNTIME_FORMACL_CREATE_OR_UPDATE = "awgment-form-acl-create-or-update";
    public static final String AWGMENT_RUNTIME_FORMACL_READ = "awgment-form-acl-read";
    public static final String AWGMENT_RUNTIME_FORMACL_DELETE = "awgment-form-acl-delete";
    public static final String AWGMENT_RUNTIME_FORMACL_ALL = "awgment-form-acl-all";
    public static final String OR=" or ";
    public static final String CREATE_OR_ALL_ACCESS =HAS_ANY_AUTHORITY+ AWGMENT_RUNTIME_FORMACL_CREATE_OR_UPDATE +HAS_ANY_AUTHORITY_ENDING+OR+HAS_ANY_AUTHORITY+AWGMENT_RUNTIME_FORMACL_ALL+HAS_ANY_AUTHORITY_ENDING;
    public static final String READ_OR_ALL_ACCESS =HAS_ANY_AUTHORITY+ AWGMENT_RUNTIME_FORMACL_READ +HAS_ANY_AUTHORITY_ENDING+OR+HAS_ANY_AUTHORITY+AWGMENT_RUNTIME_FORMACL_ALL+HAS_ANY_AUTHORITY_ENDING;
    public static final String DELETE_OR_ALL_ACCESS =HAS_ANY_AUTHORITY+ AWGMENT_RUNTIME_FORMACL_DELETE +HAS_ANY_AUTHORITY_ENDING+OR+HAS_ANY_AUTHORITY+AWGMENT_RUNTIME_FORMACL_ALL+HAS_ANY_AUTHORITY_ENDING;
}
