package com.techsophy.tsf.runtime.form.controller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.controller.FormDataAuditController;
import com.techsophy.tsf.runtime.form.dto.FormDataAuditResponse;
import com.techsophy.tsf.runtime.form.dto.FormDataAuditSchema;
import com.techsophy.tsf.runtime.form.dto.FormDataAuditResponseSchema;
import com.techsophy.tsf.runtime.form.model.ApiResponse;
import com.techsophy.tsf.runtime.form.service.FormDataAuditService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.GET_FORM_DATA_SUCCESS;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.SAVE_FORM_DATA_SUCCESS;

@RestController
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class FormDataAuditControllerImpl implements FormDataAuditController
{
    private final GlobalMessageSource globalMessageSource;
    private final FormDataAuditService formDataAuditService;

    @Override
    public ApiResponse<FormDataAuditResponse> saveFormDataAudit(FormDataAuditSchema formDataAuditSchema) throws JsonProcessingException
    {
        FormDataAuditResponse formDataAuditResponse=formDataAuditService.saveFormDataAudit(formDataAuditSchema);
        return new ApiResponse<>(formDataAuditResponse,true,globalMessageSource.get(SAVE_FORM_DATA_SUCCESS));
    }

    @Override
    public ApiResponse<List<FormDataAuditResponseSchema>> getAllFormDatAuditByFormIdAndDocumentId(String formId, String formDataId)
    {
        return new ApiResponse<>(formDataAuditService.getAllFormDataAuditByFormIdAndDocumentId(formId, formDataId),true,globalMessageSource.get(GET_FORM_DATA_SUCCESS));
    }
}
