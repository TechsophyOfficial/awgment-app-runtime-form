package com.techsophy.tsf.runtime.form.controller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.controller.FormDataController;
import com.techsophy.tsf.runtime.form.dto.AggregationResponse;
import com.techsophy.tsf.runtime.form.dto.FormDataResponse;
import com.techsophy.tsf.runtime.form.dto.FormDataResponseSchema;
import com.techsophy.tsf.runtime.form.dto.FormDataSchema;
import com.techsophy.tsf.runtime.form.model.ApiResponse;
import com.techsophy.tsf.runtime.form.service.FormDataService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.util.List;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;

@RestController
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class FormDataControllerImpl implements FormDataController
{
    private final GlobalMessageSource globalMessageSource;
    private final FormDataService formDataService;

    @Override
    public ApiResponse<FormDataResponse> saveFormData(FormDataSchema formDataSchema) throws IOException
    {
        FormDataResponse formDataResponse=formDataService.saveFormData(formDataSchema);
        return new ApiResponse<>(formDataResponse,true,globalMessageSource.get(SAVE_FORM_DATA_SUCCESS));
    }

    @Override
    public ApiResponse<FormDataResponse> updateFormData(FormDataSchema formDataSchema) throws JsonProcessingException
    {
       FormDataResponse formDataResponse=formDataService.updateFormData(formDataSchema);
       return new ApiResponse<>(formDataResponse,true,globalMessageSource.get(UPDATE_FORM_DATA_SUCCESS));
    }

    @Override
    public ApiResponse<Object> getAllFormDataByFormId(String formId, String relations, Integer page, Integer pageSize, String sortBy, String sortOrder, String filter, String q) throws JsonProcessingException
    {
        if (StringUtils.hasText(filter))
        {
            if (page == null)
            {
                return new ApiResponse<>(formDataService.getAllFormDataByFormId(formId, relations, filter, sortBy, sortOrder), true, globalMessageSource.get(GET_FORM_DATA_SUCCESS));
            }
            else
            {
                return new ApiResponse<>(formDataService.getAllFormDataByFormId(formId, relations, filter, sortBy, sortOrder, PageRequest.of(page, pageSize)), true, globalMessageSource.get(GET_FORM_DATA_SUCCESS));
            }
        }
        else if ((StringUtils.hasText(sortBy) || StringUtils.hasText(sortOrder)) || (page != null || pageSize != null) || StringUtils.hasText(q))
        {
            if (page == null)
            {
                return new ApiResponse<>(formDataService.getAllFormDataByFormIdAndQ(formId, relations, q, sortBy, sortOrder), true, globalMessageSource.get(GET_FORM_DATA_SUCCESS));
            }
            else
            {
                return new ApiResponse<>(formDataService.getAllFormDataByFormIdAndQ(formId, relations, q, sortBy, sortOrder, PageRequest.of(page, pageSize)), true, globalMessageSource.get(GET_FORM_DATA_SUCCESS));
            }
        }
        return new ApiResponse<>(formDataService.getAllFormDataByFormId(formId,relations), true, globalMessageSource.get(GET_FORM_DATA_SUCCESS));
    }

    @Override
    public ApiResponse<List<FormDataResponseSchema>> getFormDataByFormIdAndId(String formId, String id, String relations)
    {
        return new ApiResponse<>(formDataService.getFormDataByFormIdAndId(formId, id, relations), true, globalMessageSource.get(GET_FORM_DATA_SUCCESS));
    }

    public ApiResponse<Void> deleteAllFormDataByFormId(String formId)
    {
        formDataService.deleteAllFormDataByFormId(formId);
        return new ApiResponse<>(null,true,globalMessageSource.get(DELETE_ALL_FORM_DATA_SUCCESS));
    }

    @Override
    public ApiResponse<Void> deleteFormDataByFormIdAndId(String formId, String id)
    {
       formDataService.deleteFormDataByFormIdAndId(formId,id);
       return new ApiResponse<>(null,true,globalMessageSource.get(DELETE_FORM_DATA_SUCCESS));
    }

    @Override
    public ApiResponse<AggregationResponse> aggregateByFormIdFilterGroupBy(String formId, String filter, String groupBy, String operation) throws JsonProcessingException {
        AggregationResponse aggregationResponse=formDataService.aggregateByFormIdFilterGroupBy(formId,filter,groupBy,operation);
        return new ApiResponse<>(aggregationResponse,true,globalMessageSource.get(GET_FORM_DATA_SUCCESS));
    }
}
