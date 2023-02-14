package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techsophy.tsf.runtime.form.dto.FormAclDto;
import com.techsophy.tsf.runtime.form.dto.PaginationResponsePayload;

import java.math.BigInteger;

public interface FormAclService {
    FormAclDto saveFormAcl(FormAclDto formAclDto) throws JsonProcessingException;

    FormAclDto getFormAcl(BigInteger id) ;

    PaginationResponsePayload getAllFormsAcl(Integer page, Integer size) throws JsonProcessingException;

    void deleteFormAcl(BigInteger id) throws JsonProcessingException;

}
