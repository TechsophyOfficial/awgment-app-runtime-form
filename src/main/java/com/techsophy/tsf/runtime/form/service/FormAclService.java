package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techsophy.tsf.runtime.form.dto.FormAclDto;
import com.techsophy.tsf.runtime.form.dto.PaginationResponsePayload;

public interface FormAclService {
    FormAclDto saveFormAcl(FormAclDto formAclDto) ;

    FormAclDto getFormAcl(String formId) ;

    PaginationResponsePayload getAllFormsAcl(Long page, Long size) throws JsonProcessingException;

    Long deleteFormAcl(String formId) throws JsonProcessingException;

}
