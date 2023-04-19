package com.techsophy.tsf.runtime.form.controller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techsophy.tsf.commons.acl.ACLDecision;
import com.techsophy.tsf.commons.acl.ACLEvaluation;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.controller.FormDataController;
import com.techsophy.tsf.runtime.form.dto.AggregationResponse;
import com.techsophy.tsf.runtime.form.dto.FormAclDto;
import com.techsophy.tsf.runtime.form.dto.FormDataResponseSchema;
import com.techsophy.tsf.runtime.form.dto.FormDataSchema;
import com.techsophy.tsf.runtime.form.entity.FormDataDefinition;
import com.techsophy.tsf.runtime.form.exception.ACLException;
import com.techsophy.tsf.runtime.form.model.ApiResponse;
import com.techsophy.tsf.runtime.form.service.FormAclService;
import com.techsophy.tsf.runtime.form.service.FormDataService;
import com.techsophy.tsf.runtime.form.utils.RelationUtils;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.util.*;
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
    private final ACLEvaluation aclEvaluation;

    private static Optional<String> getAclFilter(List<ACLDecision> aclDecisionList)
    {
        return aclDecisionList
                .stream()
                .map(ACLDecision::getAdditionalDetails)
                .map(additionalDetailsMap->{
                    Map<String,String> runtimeFormMap=(Map<String, String>) additionalDetailsMap.get(RUNTIME_FORM_APP);
                    if(runtimeFormMap==null||runtimeFormMap.isEmpty())
                    {
                        throw new NoSuchElementException("runtime-form-app field is missing or it was empty in additionalDetailsMap inside ACLDefinition");
                    }
                    return Optional.of(runtimeFormMap);
                })
                .map(x->x.flatMap(runtimeFormApp->Optional.ofNullable(runtimeFormApp.get(FILTERS))))
                .reduce((filter1, filter2) -> filter1.flatMap(optionalFilter -> Optional.of(optionalFilter + filter2.orElse(""))))
                .orElse(Optional.empty());
    }

    @Override
    @Transactional
    public ApiResponse<FormDataDefinition> saveFormData(FormDataSchema formDataSchema, String filter) throws IOException
    {
        List<ACLDecision> aclDecisionList=checkACL(UPDATE_RULE, Collections.singletonList(formDataSchema.getFormId()));
        FormDataDefinition formDataDefinition =formDataService.saveFormData(formDataSchema,filter,getAclFilter(aclDecisionList).orElse(""));
        return new ApiResponse<>(formDataDefinition,true,globalMessageSource.get(SAVE_FORM_DATA_SUCCESS));
    }

    @Override
    @Transactional
    public ApiResponse<FormDataDefinition> updateFormData(FormDataSchema formDataSchema, String filter) throws JsonProcessingException
    {
         List<ACLDecision> aclDecisionList=checkACL(UPDATE_RULE, Collections.singletonList(formDataSchema.getFormId()));
         FormDataDefinition formDataDefinition=formDataService.updateFormData(formDataSchema,filter,getAclFilter(aclDecisionList).orElse(""));
         return new ApiResponse<>(formDataDefinition,true,globalMessageSource.get(UPDATE_FORM_DATA_SUCCESS));
    }

    @Override
    public ApiResponse<Object> getAllFormDataByFormId(String formId, String relations, Integer page, Integer pageSize, String sortBy, String sortOrder, String filter, String q) throws JsonProcessingException
    {
        List<String> listOfFormIds=new ArrayList<>();
        listOfFormIds.add(formId);
        listOfFormIds.addAll(relationUtils.getListOfFormIdsUsingRelations(relations));
        List<ACLDecision> aclDecisionList=checkACL(READ_RULE,listOfFormIds);
        if (StringUtils.hasText(filter))
        {
            if (page == null)
            {
                return new ApiResponse<>(formDataService.getAllFormDataByFormId(formId, relations, filter, sortBy, sortOrder,getAclFilter(aclDecisionList).orElse("")), true, globalMessageSource.get(GET_FORM_DATA_SUCCESS));
            }
            else
            {
                return new ApiResponse<>(formDataService.getAllFormDataByFormId(formId, relations, filter, sortBy, sortOrder, PageRequest.of(page, pageSize),getAclFilter(aclDecisionList).orElse("")), true, globalMessageSource.get(GET_FORM_DATA_SUCCESS));
            }
        }
        else if ((StringUtils.hasText(sortBy) || StringUtils.hasText(sortOrder)) || (page != null || pageSize != null) || StringUtils.hasText(q))
        {
            if (page == null)
            {
                return new ApiResponse<>(formDataService.getAllFormDataByFormIdAndQ(formId, relations, q, sortBy, sortOrder,getAclFilter(aclDecisionList).orElse("")), true, globalMessageSource.get(GET_FORM_DATA_SUCCESS));
            }
            else
            {
                return new ApiResponse<>(formDataService.getAllFormDataByFormIdAndQ(formId, relations, q, sortBy, sortOrder, PageRequest.of(page, pageSize),getAclFilter(aclDecisionList).orElse("")), true, globalMessageSource.get(GET_FORM_DATA_SUCCESS));
            }
        }
        return new ApiResponse<>(formDataService.getAllFormDataByFormId(formId,relations,getAclFilter(aclDecisionList).orElse("")), true, globalMessageSource.get(GET_FORM_DATA_SUCCESS));
    }

    @Override
    public ApiResponse<List<FormDataResponseSchema>> getFormDataByFormIdAndId(String formId, String id, String relations)
    {
        List<String> listOfFormIds=new ArrayList<>();
        listOfFormIds.add(formId);
        listOfFormIds.addAll(relationUtils.getListOfFormIdsUsingRelations(relations));
        List<ACLDecision> aclDecisionList=checkACL(READ_RULE,listOfFormIds);
        return new ApiResponse<>(formDataService.getFormDataByFormIdAndId(formId, id, relations,getAclFilter(aclDecisionList).orElse("")), true, globalMessageSource.get(GET_FORM_DATA_SUCCESS));
    }

    @Override
    public ApiResponse<Void> deleteAllFormDataByFormId(String formId)
    {
        checkACL(DELETE_RULE, Collections.singletonList(formId));
        formDataService.deleteAllFormDataByFormId(formId);
        return new ApiResponse<>(null,true,globalMessageSource.get(DELETE_ALL_FORM_DATA_SUCCESS));
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteFormDataByFormIdAndId(String formId, String id,String filter)
    {
        List<ACLDecision> aclDecisionList=checkACL(DELETE_RULE, Collections.singletonList(formId));
        formDataService.deleteFormDataByFormIdAndId(formId,id,filter,getAclFilter(aclDecisionList).orElse(""));
        return new ApiResponse<>(null,true,globalMessageSource.get(DELETE_FORM_DATA_SUCCESS));
    }

    @Override
    public ApiResponse<AggregationResponse> aggregateByFormIdFilterGroupBy(String formId, String filter, String groupBy, String operation) throws JsonProcessingException
    {
        checkACL(READ_RULE, Collections.singletonList(formId));
        AggregationResponse aggregationResponse=formDataService.aggregateByFormIdFilterGroupBy(formId,filter,groupBy,operation);
        return new ApiResponse<>(aggregationResponse,true,globalMessageSource.get(GET_FORM_DATA_SUCCESS));
    }

    private List<ACLDecision> checkACL(String rule,List<String> formIdList)
    {
        List<ACLDecision> aclDecisionList=new ArrayList<>();
        formIdList.forEach(x->{
               FormAclDto dto = formAclService.getFormAcl(x);
               if(dto==null) return;
               String aclId = dto.getAclId();
                ACLDecision decision =null;
                switch (rule)
                {
                    case READ_RULE:
                        decision = aclEvaluation.getRead(aclId,tokenUtils.getTokenFromContext(),null);
                        break;
                    case UPDATE_RULE:
                        decision = aclEvaluation.getUpdate(aclId,tokenUtils.getTokenFromContext(),null);
                        break;
                    case DELETE_RULE:
                        decision = aclEvaluation.getDelete(aclId,tokenUtils.getTokenFromContext(),null);
                        break;
                    default: break;
                }
            assert decision != null;
            if( ! decision.getDecision().equals(ALLOW) )
                {
                    throw new ACLException(ACCESS_DENIED,globalMessageSource.get(ACCESS_DENIED,aclId));
                }
            aclDecisionList.add(decision);
        });
        return aclDecisionList;
    }
}
