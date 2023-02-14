package com.techsophy.tsf.runtime.form.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techsophy.tsf.runtime.form.dto.FormAclDto;
import com.techsophy.tsf.runtime.form.entity.FormAclEntity;
import com.techsophy.tsf.runtime.form.model.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.math.BigInteger;
import static com.techsophy.tsf.runtime.form.constants.FormAclConstants.*;
import static com.techsophy.tsf.runtime.form.constants.FormAclConstants.CREATE_OR_ALL_ACCESS;
import static com.techsophy.tsf.runtime.form.constants.FormAclConstants.DELETE_OR_ALL_ACCESS;
import static com.techsophy.tsf.runtime.form.constants.FormAclConstants.FORM_ID;
import static com.techsophy.tsf.runtime.form.constants.FormAclConstants.READ_OR_ALL_ACCESS;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;

@RequestMapping(BASE_URL+ VERSION_V1+FORMS)
public interface FormAclController {
    @PostMapping(ACL)
    @PreAuthorize(CREATE_OR_ALL_ACCESS)
    ApiResponse<FormAclDto> saveFormAcl(@RequestBody @Validated FormAclDto formAclDto) throws JsonProcessingException;

    @GetMapping(FORMID+ACL)
    @PreAuthorize(READ_OR_ALL_ACCESS)
    ApiResponse<FormAclDto> getFormAcl(@PathVariable(FORM_ID) BigInteger id) throws JsonProcessingException;

    @GetMapping(ACL)
    @PreAuthorize(READ_OR_ALL_ACCESS)
    ApiResponse<Page<FormAclEntity>> getAllFormsAcl(@RequestParam(required = false,defaultValue = "0") Integer page,
                                                    @RequestParam(required = false,defaultValue = "200") Integer size) throws JsonProcessingException;

    @DeleteMapping(FORMID+ACL)
    @PreAuthorize(DELETE_OR_ALL_ACCESS)
    ApiResponse<Void> deleteFormAcl(@PathVariable(FORM_ID) BigInteger id) throws JsonProcessingException;


}
