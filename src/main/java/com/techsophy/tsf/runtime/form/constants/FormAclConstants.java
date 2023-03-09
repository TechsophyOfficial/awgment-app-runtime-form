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
    public static final String ID ="id";
    public static final String REQUIRES_ROLE ="user need these keycloak roles to access this method ";
    public static final String GET_FORM_ACL ="get formAcl";
    public static final String GET_FORMS_ACL ="get all formsAcl";
    public static final String SAVE_FORM_ACL ="save formAcl";
    public static final String DELETE_FORM_ACL ="delete formAcl";
    public static final String OR=" or ";
    public static final String AWGMENT_RUNTIME_FORMACL_CREATE_OR_UPDATE = "awgment-form-acl-create-or-update";
    public static final String AWGMENT_RUNTIME_FORMACL_READ = "awgment-form-acl-read";
    public static final String AWGMENT_RUNTIME_FORMACL_DELETE = "awgment-form-acl-delete";
    public static final String AWGMENT_RUNTIME_FORMACL_ALL = "awgment-form-acl-all";
    public static final String NO_RECORD_FOUND = "AWGMENT-RUNTIME-FORM-1040";
}
