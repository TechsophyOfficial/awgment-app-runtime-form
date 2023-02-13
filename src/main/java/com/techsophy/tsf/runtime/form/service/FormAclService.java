package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techsophy.tsf.runtime.form.dto.FormAclDto;
import com.techsophy.tsf.runtime.form.dto.PaginationResponsePayload;

import java.math.BigInteger;

public interface FormAclService {
    FormAclDto saveFormIdWithAclID(FormAclDto formAclDto) throws JsonProcessingException;

    FormAclDto  getFormIdWithAclID(BigInteger id) ;

    PaginationResponsePayload getAllFormsIdWithAclID(Integer page, Integer size) throws JsonProcessingException;

    void deleteFormIdWithAclId(BigInteger id) throws JsonProcessingException;

}
