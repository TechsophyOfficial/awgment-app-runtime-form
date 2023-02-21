package com.techsophy.tsf.runtime.form.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techsophy.tsf.runtime.form.dto.FormAclDto;
import com.techsophy.tsf.runtime.form.entity.FormAclEntity;
import com.techsophy.tsf.runtime.form.model.ApiResponse;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import static com.techsophy.tsf.runtime.form.constants.FormAclConstants.*;
import static com.techsophy.tsf.runtime.form.constants.FormAclConstants.FORM_ID;
import static com.techsophy.tsf.runtime.form.constants.FormAclConstants.OR;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;

@RequestMapping(BASE_URL+ VERSION_V1+FORMS)
public interface FormAclController {
    @GetMapping(FORMID+ACL)
    @ApiOperation(value =GET_FORM_ACL ,notes=REQUIRES_ROLE+AWGMENT_RUNTIME_FORMACL_READ+OR+AWGMENT_RUNTIME_FORMACL_ALL)
    @PreAuthorize("hasAnyAuthority('awgment-form-acl-read') or hasAnyAuthority('awgment-form-acl-all')")
    ApiResponse<FormAclDto> getFormAcl(@PathVariable(FORM_ID) String formId) throws JsonProcessingException;

    @PostMapping(ACL)
    @ApiOperation(value =SAVE_FORM_ACL ,notes=REQUIRES_ROLE+AWGMENT_RUNTIME_FORMACL_CREATE_OR_UPDATE+OR+AWGMENT_RUNTIME_FORMACL_ALL)

    @PreAuthorize("hasAnyAuthority('awgment-form-acl-create-or-update') or hasAnyAuthority('awgment-form-acl-all')")
    ApiResponse<FormAclDto> saveFormAcl(@RequestBody @Validated FormAclDto formAclDto) throws JsonProcessingException;

    @GetMapping(ACL)
    @ApiOperation(value =GET_FORMS_ACL ,notes=REQUIRES_ROLE+AWGMENT_RUNTIME_FORMACL_READ+OR+AWGMENT_RUNTIME_FORMACL_ALL)
    @PreAuthorize("hasAnyAuthority('awgment-form-acl-read') or hasAnyAuthority('awgment-form-acl-all')")
    ApiResponse<Page<FormAclEntity>> getAllFormsAcl(@RequestParam(required = false,defaultValue = "0") Long page,
                                                    @RequestParam(required = false,defaultValue = "200") Long size) throws JsonProcessingException;

    @DeleteMapping(FORMID+ACL)
    @ApiOperation(value =DELETE_FORM_ACL ,notes=REQUIRES_ROLE+AWGMENT_RUNTIME_FORMACL_DELETE+OR+AWGMENT_RUNTIME_FORMACL_ALL)
    @PreAuthorize("hasAnyAuthority('awgment-form-acl-delete') or hasAnyAuthority('awgment-form-acl-all')")
    ApiResponse<Void> deleteFormAcl(@PathVariable(FORM_ID) String formId) throws JsonProcessingException;

}
