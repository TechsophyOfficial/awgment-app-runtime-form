package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techsophy.tsf.runtime.form.dto.*;
import com.techsophy.tsf.runtime.form.entity.FormDataDefinition;
import org.springframework.data.domain.Pageable;
import java.io.IOException;
import java.util.List;

public interface FormDataService
{
    FormDataDefinition saveFormData(FormDataSchema formDataSchema, String filter,String aclFilter,List<String> orFilter) throws IOException;

    FormDataDefinition updateFormData(FormDataSchema formDataSchema, String filter,String aclFilter,List<String> orFilter) throws JsonProcessingException;

    List<FormDataResponseSchema> getAllFormDataByFormId(String formId, String relations, String filter, String sortBy, String sortOrder,String aclFilter, List<String> orFilter) throws JsonProcessingException;

    PaginationResponsePayload getAllFormDataByFormId(String formId, String relations, String filter, String sortBy, String sortOrder, Pageable pageable,String aclFilter,List<String> orFilter) throws JsonProcessingException;

    List<FormDataResponseSchema> getAllFormDataByFormIdAndQ(String formId, String relations, String q, String sortBy, String sortOrder,String aclFilter,List<String> orFilter);

    PaginationResponsePayload getAllFormDataByFormIdAndQ(String formId, String relations, String q, String sortBy, String sortOrder, Pageable pageable,String aclFilter,List<String> orFilter);

    PaginationResponsePayload getAllFormDataByFormId(String formId, String relations,String aclFilter,List<String> orFilter);

   List<FormDataResponseSchema> getFormDataByFormIdAndId(String formId, String id, String relations,String aclFilter,List<String> orFilter);

    void deleteAllFormDataByFormId(String formId);

    void deleteFormDataByFormIdAndId(String formId, String id, String filter, String aclFilter,List<String> orFilter);

    AggregationResponse aggregateByFormIdFilterGroupBy(String formId, String filter, String groupBy, String operation) throws JsonProcessingException;
}
