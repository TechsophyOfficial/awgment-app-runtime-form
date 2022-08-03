package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techsophy.tsf.runtime.form.dto.FormDataAuditResponse;
import com.techsophy.tsf.runtime.form.dto.FormDataAuditResponseSchema;
import com.techsophy.tsf.runtime.form.dto.FormDataAuditSchema;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface FormDataAuditService
{
    @Transactional(rollbackFor = Exception.class)
    FormDataAuditResponse saveFormDataAudit(FormDataAuditSchema formDataAuditSchema) throws JsonProcessingException;

    List<FormDataAuditResponseSchema> getAllFormDataAuditByFormIdAndDocumentId(String formId, String formDataId);
}
