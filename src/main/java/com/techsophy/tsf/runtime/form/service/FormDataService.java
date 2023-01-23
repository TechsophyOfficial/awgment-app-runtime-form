package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techsophy.tsf.runtime.form.dto.*;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

public interface FormDataService
{
    FormDataResponse saveFormData(FormDataSchema formDataSchema) throws IOException;

    FormDataResponse updateFormData(FormDataSchema formDataSchema) throws JsonProcessingException;

    List<FormDataResponseSchema> getAllFormDataByFormId(String formId,String relations,String filter,String sortBy, String sortOrder);

    PaginationResponsePayload getAllFormDataByFormId(String formId,String relations,String filter, String sortBy, String sortOrder, Pageable pageable);

    List<FormDataResponseSchema> getAllFormDataByFormIdAndQ(String formId,String relations,String q, String sortBy, String sortOrder);

    PaginationResponsePayload getAllFormDataByFormIdAndQ(String formId, String relations, String q, String sortBy, String sortOrder, Pageable pageable);

    PaginationResponsePayload getAllFormDataByFormId(String formId, String relations);

    List<FormDataResponseSchema> getFormDataByFormIdAndId(String formId, String id, String relations);

    void deleteAllFormDataByFormId(String formId);

    void deleteFormDataByFormIdAndId(String formId, String id);

    AggregationResponse aggregateByFormIdFilterGroupBy(String formId, String filter, String groupBy, String operation);
}
