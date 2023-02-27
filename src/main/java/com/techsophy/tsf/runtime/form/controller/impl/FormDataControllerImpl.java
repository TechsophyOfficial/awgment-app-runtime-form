package com.techsophy.tsf.runtime.form.controller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techsophy.tsf.commons.ACLEvaluator;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.controller.FormDataController;
import com.techsophy.tsf.runtime.form.dto.AggregationResponse;
import com.techsophy.tsf.runtime.form.dto.FormDataResponse;
import com.techsophy.tsf.runtime.form.dto.FormDataResponseSchema;
import com.techsophy.tsf.runtime.form.dto.FormDataSchema;
import com.techsophy.tsf.runtime.form.exception.ACLException;
import com.techsophy.tsf.runtime.form.model.ApiResponse;
import com.techsophy.tsf.runtime.form.service.FormAclService;
import com.techsophy.tsf.runtime.form.service.FormDataService;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static com.techsophy.tsf.runtime.form.constants.ErrorConstants.ACCESS_DENIED;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import org.springframework.util.StringUtils;

@RestController
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class FormDataControllerImpl implements FormDataController
{
    private final GlobalMessageSource globalMessageSource;
    private final FormDataService formDataService;
    private final FormAclService formAclService;
    private final TokenUtils tokenUtils;
    @Value(GATEWAY_URL)
    private String gatewayUrl;

    private Optional<String> getAclId(String formId)
    {
        try
        {
            return Optional.ofNullable(formAclService.getFormAcl(formId).getAclId());
        }
        catch (Exception e)
        {
            return Optional.empty();
        }
    }

    public ACLEvaluator getAclEvaluatorObject(String aclId, Map<String,?> context, String gatewayUrl)
    {
        return new ACLEvaluator(aclId,context,gatewayUrl);
    }

    private void checkACLRead(String formId, String relations)
    {
        String aclId=getAclId(formId).orElse(EMPTY_STRING);
        if(StringUtils.hasText(aclId))
        {
            ACLEvaluator aclEvaluator=getAclEvaluatorObject(aclId,null,gatewayUrl);
            if(aclEvaluator.getRead(tokenUtils.getTokenFromContext()).getDecision().equals(DENY))
            {
                throw new ACLException(ACCESS_DENIED,globalMessageSource.get(ACCESS_DENIED,aclId));
            }
            if(relations!=null)
            {
                Arrays.stream(relations.split(COMMA))
                        .forEach(x->{
                            String[] keyValuePair= x.split(COLON);
                            String aclId1=getAclId(keyValuePair[0]).orElse(EMPTY_STRING);
                            if(StringUtils.hasText(aclId1))
                            {
                                ACLEvaluator aclEvaluator1=  getAclEvaluatorObject(aclId1,null,gatewayUrl);
                                if(aclEvaluator1.getRead(tokenUtils.getTokenFromContext()).getDecision().equals(DENY))
                                {
                                    throw new ACLException(ACCESS_DENIED,globalMessageSource.get(ACCESS_DENIED));
                                }
                            }
                        });
            }
        }
    }

    @Override
    public ApiResponse<FormDataResponse> saveFormData(FormDataSchema formDataSchema) throws IOException
    {
        FormDataResponse formDataResponse=formDataService.saveFormData(formDataSchema);
        return new ApiResponse<>(formDataResponse,true,globalMessageSource.get(SAVE_FORM_DATA_SUCCESS));
    }

    @Override
    public ApiResponse<FormDataResponse> updateFormData(FormDataSchema formDataSchema) throws JsonProcessingException
    {
         String aclId=getAclId(formDataSchema.getFormId()).orElse(EMPTY_STRING);
         if(StringUtils.hasText(aclId))
         {
             ACLEvaluator aclEvaluator=getAclEvaluatorObject(aclId,null,gatewayUrl);
             if(aclEvaluator.getUpdate(tokenUtils.getTokenFromContext()).getDecision().equals(DENY))
             {
                 throw new ACLException(ACCESS_DENIED,globalMessageSource.get(ACCESS_DENIED,aclId));
             }
         }
         FormDataResponse formDataResponse=formDataService.updateFormData(formDataSchema);
         return new ApiResponse<>(formDataResponse,true,globalMessageSource.get(UPDATE_FORM_DATA_SUCCESS));
    }

    @Override
    public ApiResponse<Object> getAllFormDataByFormId(String formId, String relations, Integer page, Integer pageSize, String sortBy, String sortOrder, String filter, String q)
    {
        checkACLRead(formId, relations);
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
        checkACLRead(formId, relations);
        return new ApiResponse<>(formDataService.getFormDataByFormIdAndId(formId, id, relations), true, globalMessageSource.get(GET_FORM_DATA_SUCCESS));
    }

    public ApiResponse<Void> deleteAllFormDataByFormId(String formId)
    {
        checkACLDelete(formId);
        formDataService.deleteAllFormDataByFormId(formId);
        return new ApiResponse<>(null,true,globalMessageSource.get(DELETE_ALL_FORM_DATA_SUCCESS));
    }

    @Override
    public ApiResponse<Void> deleteFormDataByFormIdAndId(String formId, String id)
    {
        checkACLDelete(formId);
        formDataService.deleteFormDataByFormIdAndId(formId,id);
        return new ApiResponse<>(null,true,globalMessageSource.get(DELETE_FORM_DATA_SUCCESS));
    }

    @Override
    public ApiResponse<AggregationResponse> aggregateByFormIdFilterGroupBy(String formId, String filter, String groupBy, String operation)
    {
        checkACLRead(formId,EMPTY_STRING);
        AggregationResponse aggregationResponse=formDataService.aggregateByFormIdFilterGroupBy(formId,filter,groupBy,operation);
        return new ApiResponse<>(aggregationResponse,true,globalMessageSource.get(GET_FORM_DATA_SUCCESS));
    }

    private void checkACLDelete(String formId)
    {
        String aclId=getAclId(formId).orElse(EMPTY_STRING);
        if(StringUtils.hasText(aclId))
        {
            ACLEvaluator aclEvaluator=getAclEvaluatorObject(aclId,null,gatewayUrl);
            if(aclEvaluator.getDelete(tokenUtils.getTokenFromContext()).getDecision().equals(DENY))
            {
                throw new ACLException(ACCESS_DENIED,globalMessageSource.get(ACCESS_DENIED,aclId));
            }
        }
    }
}
