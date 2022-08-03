package com.techsophy.tsf.runtime.form.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techsophy.tsf.runtime.form.dto.FormDataAuditResponse;
import com.techsophy.tsf.runtime.form.dto.FormDataAuditSchema;
import com.techsophy.tsf.runtime.form.dto.FormDataAuditResponseSchema;
import com.techsophy.tsf.runtime.form.model.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;

@RequestMapping(BASE_URL+VERSION_V1+HISTORY)
public interface FormDataAuditController
{
    @PostMapping(FORM_DATA_URL)
    @PreAuthorize(CREATE_OR_ALL_ACCESS)
    ApiResponse<FormDataAuditResponse> saveFormDataAudit(FormDataAuditSchema formDataAuditSchema) throws JsonProcessingException;

    @GetMapping(FORM_DATA_DOCUMENT_ID_URL)
    @PreAuthorize(READ_OR_ALL_ACCESS)
    ApiResponse<List<FormDataAuditResponseSchema>> getAllFormDatAuditByFormIdAndDocumentId(@RequestParam(value = FORM_ID) String formId, @RequestParam(FORM_DATA_ID) String formDataId);
}
