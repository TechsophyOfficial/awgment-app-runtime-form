package com.techsophy.tsf.runtime.form.controller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techsophy.tsf.commons.ACLDecision;
import com.techsophy.tsf.commons.ACLEvaluator;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.controller.FormDataController;
import com.techsophy.tsf.runtime.form.dto.*;
import com.techsophy.tsf.runtime.form.exception.ACLException;
import com.techsophy.tsf.runtime.form.model.ApiResponse;
import com.techsophy.tsf.runtime.form.service.FormAclService;
import com.techsophy.tsf.runtime.form.service.FormDataService;
import com.techsophy.tsf.runtime.form.utils.RelationUtils;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static com.techsophy.tsf.runtime.form.constants.ErrorConstants.ACCESS_DENIED;
import static com.techsophy.tsf.runtime.form.constants.FormDataConstants.*;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;

@RestController
@AllArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class FormDataControllerImpl implements FormDataController
{
    private final GlobalMessageSource globalMessageSource;
    private final FormDataService formDataService;
    private final FormAclService formAclService;
    private final TokenUtils tokenUtils;
    private final RelationUtils relationUtils;
    @Value(GATEWAY_URL)
    private String gatewayUrl;

    @Override
    public ApiResponse<FormDataResponse> saveFormData(FormDataSchema formDataSchema) throws IOException
    {
        checkACL(UPDATE_RULE, Collections.singletonList(formDataSchema.getFormId()));
        FormDataResponse formDataResponse=formDataService.saveFormData(formDataSchema);
        return new ApiResponse<>(formDataResponse,true,globalMessageSource.get(SAVE_FORM_DATA_SUCCESS));
    }

    @Override
    public ApiResponse<FormDataResponse> updateFormData(FormDataSchema formDataSchema) throws JsonProcessingException
    {
         checkACL(UPDATE_RULE, Collections.singletonList(formDataSchema.getFormId()));
         FormDataResponse formDataResponse=formDataService.updateFormData(formDataSchema);
         return new ApiResponse<>(formDataResponse,true,globalMessageSource.get(UPDATE_FORM_DATA_SUCCESS));
    }

    @Override
    public ApiResponse<Object> getAllFormDataByFormId(String formId, String relations, Integer page, Integer pageSize, String sortBy, String sortOrder, String filter, String q) throws JsonProcessingException
    {
        List<String> listOfFormIds=new ArrayList<>();
        listOfFormIds.add(formId);
        listOfFormIds.addAll(relationUtils.getListOfFormIdsUsingRelations(relations));
        checkACL(READ_RULE,listOfFormIds);
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
        List<String> listOfFormIds=new ArrayList<>();
        listOfFormIds.add(formId);
        listOfFormIds.addAll(relationUtils.getListOfFormIdsUsingRelations(relations));
        checkACL(READ_RULE,listOfFormIds);
        return new ApiResponse<>(formDataService.getFormDataByFormIdAndId(formId, id, relations), true, globalMessageSource.get(GET_FORM_DATA_SUCCESS));
    }

    public ApiResponse<Void> deleteAllFormDataByFormId(String formId)
    {
        checkACL(DELETE_RULE, Collections.singletonList(formId));
        formDataService.deleteAllFormDataByFormId(formId);
        return new ApiResponse<>(null,true,globalMessageSource.get(DELETE_ALL_FORM_DATA_SUCCESS));
    }

    @Override
    public ApiResponse<Void> deleteFormDataByFormIdAndId(String formId, String id)
    {
        checkACL(DELETE_RULE, Collections.singletonList(formId));
        formDataService.deleteFormDataByFormIdAndId(formId,id);
        return new ApiResponse<>(null,true,globalMessageSource.get(DELETE_FORM_DATA_SUCCESS));
    }

    @Override
    public ApiResponse<AggregationResponse> aggregateByFormIdFilterGroupBy(String formId, String filter, String groupBy, String operation) throws JsonProcessingException {
    {
        checkACL(READ_RULE, Collections.singletonList(formId));
        AggregationResponse aggregationResponse=formDataService.aggregateByFormIdFilterGroupBy(formId,filter,groupBy,operation);
        return new ApiResponse<>(aggregationResponse,true,globalMessageSource.get(GET_FORM_DATA_SUCCESS));
    }

    private void checkACL(String rule,List<String> formIdList)
    {
        formIdList.forEach(x->{
               FormAclDto dto = formAclService.getFormAcl(x);
               if(dto==null) return;
               String aclId = dto.getAclId();
                ACLEvaluator aclEvaluator=new ACLEvaluator(aclId,null,gatewayUrl);
                ACLDecision decision =null;
                switch (rule)
                {
                    case READ_RULE:
                        decision = aclEvaluator.getRead(tokenUtils.getTokenFromContext());
                        break;
                    case UPDATE_RULE:
                        decision = aclEvaluator.getUpdate(tokenUtils.getTokenFromContext());
                        break;
                    case DELETE_RULE:
                        decision = aclEvaluator.getDelete(tokenUtils.getTokenFromContext());
                        break;
                    default: break;
                }
            assert decision != null;
            if( ! decision.getDecision().equals(ALLOW) )
                {
                    throw new ACLException(ACCESS_DENIED,globalMessageSource.get(ACCESS_DENIED,aclId));
                }
        });
    }
}
