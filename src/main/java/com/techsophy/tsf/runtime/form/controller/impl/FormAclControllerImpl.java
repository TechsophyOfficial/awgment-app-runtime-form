package com.techsophy.tsf.runtime.form.controller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techsophy.tsf.runtime.form.controller.FormAclController;
import com.techsophy.tsf.runtime.form.dto.FormAclDto;
import com.techsophy.tsf.runtime.form.model.ApiResponse;
import com.techsophy.tsf.runtime.form.service.FormAclService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;

@RestController
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class FormAclControllerImpl implements FormAclController {
    
    private final FormAclService formAclService;
    @Override
    public ApiResponse<FormAclDto> saveFormIdWithAclID(FormAclDto formAclDto) throws JsonProcessingException {
         return  new ApiResponse<>(formAclService.saveFormIdWithAclID(formAclDto),true,"formAcl created successfully");
    }

    @Override
    public ApiResponse<FormAclDto> getFormIdWithAclID(BigInteger id) throws JsonProcessingException {
        return new ApiResponse<>(formAclService.getFormIdWithAclID(id),true,"Data get successfully");
    }

    @Override
    public ApiResponse getAllFormsIdWithAclID(Integer page, Integer size) throws JsonProcessingException {
        return new ApiResponse<>(formAclService.getAllFormsIdWithAclID(page,size),true,"All FormsAcl data get successfully");
    }

    @Override
    public ApiResponse<Void> deleteFormIdWithAclID(BigInteger id) throws JsonProcessingException {
        formAclService.deleteFormIdWithAclId(id);
        return new ApiResponse<>(null, true,"deleted formAcl successfully");
    }

}
