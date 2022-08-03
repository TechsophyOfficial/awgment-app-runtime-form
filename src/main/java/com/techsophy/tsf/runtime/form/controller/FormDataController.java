package com.techsophy.tsf.runtime.form.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techsophy.tsf.runtime.form.dto.FormDataResponse;
import com.techsophy.tsf.runtime.form.dto.FormDataSchema;
import com.techsophy.tsf.runtime.form.model.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;

@RequestMapping(BASE_URL+ VERSION_V1)
public interface FormDataController
{
    @PostMapping(FORM_DATA_URL)
    @PreAuthorize(CREATE_OR_ALL_ACCESS)
    ApiResponse<FormDataResponse> saveFormData(@RequestBody @Validated FormDataSchema formDataSchema) throws JsonProcessingException;

    @PatchMapping(FORM_DATA_URL)
    ApiResponse<FormDataResponse> updateFormData(@RequestBody @Validated FormDataSchema formDataSchema) throws JsonProcessingException;

    @GetMapping(FORM_DATA_URL)
    @PreAuthorize(READ_OR_ALL_ACCESS)
    ApiResponse getAllFormDataByFormId(@RequestParam(value = FORM_ID) String formId,
                                       @RequestParam(value = RELATIONS, required = false) String relations,
                                       @RequestParam(value = PAGE, required = false) Integer page,
                                       @RequestParam(value = SIZE, required = false) Integer pageSize,
                                       @RequestParam(value = SORT_BY, required = false) String sortBy,
                                       @RequestParam(value = SORT_ORDER, required = false) String sortOrder,
                                       @RequestParam(value = FILTER, required = false) String filter,
                                       @RequestParam(value = Q, required = false) String q) throws JsonProcessingException;

    @GetMapping(FORM_DATA_ID_URL)
    @PreAuthorize(READ_OR_ALL_ACCESS)
    ApiResponse getFormDataByFormIdAndId(@PathVariable(FORM_ID) String formId, @RequestParam(ID) String id, @RequestParam(value = RELATIONS, required = false) String relations);

    @DeleteMapping(FORM_DATA_URL)
    @PreAuthorize(DELETE_OR_ALL_ACCESS)
    ApiResponse<Void> deleteAllFormDataByFormId(@RequestParam(value = FORM_ID) String formId);

    @DeleteMapping(FORM_DATA_ID_URL)
    @PreAuthorize(DELETE_OR_ALL_ACCESS)
    ApiResponse<Void> deleteFormDataByFormIdAndId(@PathVariable(FORM_ID) String formId, @RequestParam(value=ID) String id);

    @PostMapping(FORM_DATA_VALIDATE_URL)
    @PreAuthorize(CREATE_OR_ALL_ACCESS)
    ApiResponse<String> validateFormDataByFormId(@RequestBody @Validated FormDataSchema formDataSchema) throws JsonProcessingException;


    @GetMapping(FORM_DATA_AGGREGATE)
    @PreAuthorize(READ_OR_ALL_ACCESS)
    ApiResponse  aggregateByFormIdFilterGroupBy(@RequestParam(value = FORM_ID) String formId, @RequestParam(value = FILTER,required = false) String filter,
                                                                     @RequestParam(value = GROUP_BY) String groupBy, @RequestParam(value = OPERATION) String operation);
}
