package com.techsophy.tsf.runtime.form.controller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.controller.FormAclController;
import com.techsophy.tsf.runtime.form.dto.FormAclDto;
import com.techsophy.tsf.runtime.form.exception.EntityPathException;
import com.techsophy.tsf.runtime.form.model.ApiResponse;
import com.techsophy.tsf.runtime.form.service.FormAclService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import static com.techsophy.tsf.runtime.form.constants.FormAclConstants.NO_RECORD_FOUND;

@RestController
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Transactional
public class FormAclControllerImpl implements FormAclController {
    
    private final FormAclService formAclService;
    private final GlobalMessageSource globalMessageSource;
    
    @Override
    public ApiResponse<FormAclDto> saveFormAcl(FormAclDto formAclDto) {
        FormAclDto dto = formAclService.saveFormAcl(formAclDto);
        if(dto==null)
        {
            throw new EntityPathException(NO_RECORD_FOUND,globalMessageSource.get(NO_RECORD_FOUND,formAclDto.getFormId()));
        }
         return  new ApiResponse<>(dto,true,"formAcl created successfully");
    }

    @Override
    public ApiResponse<FormAclDto> getFormAcl(String formId) {
        FormAclDto dto = formAclService.getFormAcl(formId);
        if(dto == null){
            throw new EntityPathException(NO_RECORD_FOUND,globalMessageSource.get(NO_RECORD_FOUND,formId));
        }
        return new ApiResponse<>(dto,true,"Data get successfully");
    }

    @Override
    public ApiResponse getAllFormsAcl(Long page, Long size) throws JsonProcessingException {
        return new ApiResponse<>(formAclService.getAllFormsAcl(page,size),true,"All FormsAcl data get successfully");
    }

    @Override
    public ApiResponse deleteFormAcl(String formId) throws JsonProcessingException {

       Long count = formAclService.deleteFormAcl(formId);
       if(count==0)
       {
          throw  new EntityPathException(NO_RECORD_FOUND,globalMessageSource.get(NO_RECORD_FOUND,formId));
       }
        return new ApiResponse<>(null,true,"deleted successfully");
    }
}
