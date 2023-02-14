package com.techsophy.tsf.runtime.form.controller;

import com.techsophy.tsf.runtime.form.controller.impl.FormAclControllerImpl;
import com.techsophy.tsf.runtime.form.dto.FormAclDto;
import com.techsophy.tsf.runtime.form.dto.PaginationResponsePayload;
import com.techsophy.tsf.runtime.form.exception.EntityIdNotFoundException;
import com.techsophy.tsf.runtime.form.exception.UserDetailsIdNotFoundException;
import com.techsophy.tsf.runtime.form.model.ApiResponse;
import com.techsophy.tsf.runtime.form.service.FormAclService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigInteger;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
 class FormAclControllerTest {
    @Mock
    FormAclService formAclService;
    @InjectMocks
    FormAclControllerImpl formAclController;
    @Test
    void saveFormAclSuccess() throws Exception
    {
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setFormId("123");
        formAclDto.setAclId("123");
        Mockito.when(formAclService.saveFormAcl(any())).thenReturn(formAclDto);
        formAclController.saveFormAcl(formAclDto);
        verify(formAclService,times(1)).saveFormAcl(any());
    }
    @Test
    void getFormAclSuccess() throws Exception
    {
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setFormId("123");
        formAclDto.setAclId("123");
        Mockito.when(formAclService.getFormAcl(any())).thenReturn(formAclDto);
        formAclController.getFormAcl(BigInteger.ONE);
        verify(formAclService,times(1)).getFormAcl(any());
    }
    @Test
    void getAllFormsAclSuccess() throws Exception
    {
        ApiResponse apiResponse = new ApiResponse("abc",true,"getting successfully");
        PaginationResponsePayload paginationResponsePayload = new PaginationResponsePayload();
        Mockito.when(formAclService.getAllFormsAcl(any(),any())).thenReturn(paginationResponsePayload);
        formAclController.getAllFormsAcl(Integer.valueOf("1"),Integer.valueOf("1"));
        verify(formAclService,times(1)).getAllFormsAcl(any(),any());
    }
    @Test
    void deleteFormAclSuccess() throws Exception
    {
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setFormId("123");
        formAclDto.setAclId("123");
        doNothing().when(formAclService).deleteFormAcl(any());
        formAclController.deleteFormAcl(BigInteger.ONE);
        verify(formAclService,times(1)).deleteFormAcl(any());
    }
}
