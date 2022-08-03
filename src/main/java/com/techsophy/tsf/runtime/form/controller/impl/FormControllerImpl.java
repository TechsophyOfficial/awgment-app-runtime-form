package com.techsophy.tsf.runtime.form.controller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.controller.FormController;
import com.techsophy.tsf.runtime.form.dto.FormResponseSchema;
import com.techsophy.tsf.runtime.form.dto.FormSchema;
import com.techsophy.tsf.runtime.form.model.ApiResponse;
import com.techsophy.tsf.runtime.form.service.FormService;
import lombok.AllArgsConstructor;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import java.io.UnsupportedEncodingException;
import java.util.stream.Stream;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;

@RestController
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class FormControllerImpl implements FormController
{
    private final FormService formServiceImpl;
    private final GlobalMessageSource globalMessageSource;

    @Override
    public ApiResponse<Void> saveRuntimeForm(FormSchema formSchema) throws JsonProcessingException
    {
        formServiceImpl.saveRuntimeForm(formSchema);
        if(StringUtils.equals(formSchema.getType(),COMPONENT))
        {
            return new ApiResponse<>(null, true, globalMessageSource.get(DEPLOY_COMPONENT_SUCCESS));
        }
        return new ApiResponse<>(null, true, globalMessageSource.get(DEPLOY_FORM_SUCCESS));
    }

    @Override
    public ApiResponse<FormResponseSchema> getRuntimeFormById(String id)
    {
        FormResponseSchema formResponseSchema=formServiceImpl.getRuntimeFormById(id);
        if(StringUtils.equals(formResponseSchema.getType(),COMPONENT))
        {
            return new ApiResponse<>(formResponseSchema,true, globalMessageSource.get(GET_COMPONENT_SUCCESS));
        }
        return new ApiResponse<>(formResponseSchema, true, globalMessageSource.get(GET_FORM_SUCCESS));
    }

    @Override
    public ApiResponse<Stream<FormResponseSchema>> getAllRuntimeForms(boolean includeContent, String type)
    {
        if(StringUtils.equals(type,COMPONENT))
        {
            return new ApiResponse<>(formServiceImpl.getAllRuntimeForms(includeContent,type),true,globalMessageSource.get(GET_COMPONENT_SUCCESS));
        }
        return new ApiResponse<>(formServiceImpl.getAllRuntimeForms(includeContent,type),true,globalMessageSource.get(GET_FORM_SUCCESS));
    }

    @Override
    public ApiResponse<Void> deleteRuntimeFormById(String id)
    {
        boolean response=formServiceImpl.deleteRuntimeFormById(id);
        if(response)
        {
            return new ApiResponse<>(null, true, globalMessageSource.get(DELETE_COMPONENT_SUCCESS));
        }
        return new ApiResponse<>(null, true, globalMessageSource.get(DELETE_FORM_SUCCESS));
    }

    @Override
    public ApiResponse<Stream<FormResponseSchema>> searchRuntimeFormByIdOrNameLike(String idOrNameLike, String type) throws UnsupportedEncodingException
    {
        if(StringUtils.equals(type,COMPONENT))
        {
            return new ApiResponse<>(this.formServiceImpl.searchRuntimeFormByIdOrNameLike(idOrNameLike,type), true, globalMessageSource.get(GET_COMPONENT_SUCCESS));
        }
        return new ApiResponse<>(this.formServiceImpl.searchRuntimeFormByIdOrNameLike(idOrNameLike,type), true, globalMessageSource.get(GET_FORM_SUCCESS));
    }
}
