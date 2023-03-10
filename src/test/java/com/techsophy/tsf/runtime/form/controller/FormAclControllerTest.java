package com.techsophy.tsf.runtime.form.controller;

import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.controller.impl.FormAclControllerImpl;
import com.techsophy.tsf.runtime.form.dto.FormAclDto;
import com.techsophy.tsf.runtime.form.dto.PaginationResponsePayload;
import com.techsophy.tsf.runtime.form.exception.EntityPathException;
import com.techsophy.tsf.runtime.form.service.FormAclService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
 class FormAclControllerTest {
    @Mock
    FormAclService formAclService;
    @Mock
    GlobalMessageSource globalMessageSource;
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
    void saveFormAclExceptionWithWrongId() throws Exception
    {
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setId("1");
        formAclDto.setFormId("123");
        formAclDto.setAclId("123");
        Mockito.when(formAclService.saveFormAcl(any())).thenReturn(null);
        Assertions.assertThrows(EntityPathException.class,()->formAclController.saveFormAcl(formAclDto));
    }
    @Test
    void getFormAclSuccess() throws Exception
    {
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setFormId("123");
        formAclDto.setAclId("123");
        Mockito.when(formAclService.getFormAcl(any())).thenReturn(formAclDto);
        formAclController.getFormAcl("1");
        verify(formAclService,times(1)).getFormAcl(any());
    }

    @Test
    void getAllFormsAclSuccess() throws Exception
    {
        List<Map<String,Object>> list = new ArrayList<>();
        Map<String,Object> map = new HashMap<>();
        map.put("abc","abc");
        list.add(map);
        PaginationResponsePayload paginationResponsePayload = new PaginationResponsePayload();
        paginationResponsePayload.setContent(list);
        Mockito.when(formAclService.getAllFormsAcl(any(),any())).thenReturn(paginationResponsePayload);
        formAclController.getAllFormsAcl(1L,1L);
        verify(formAclService,times(1)).getAllFormsAcl(any(),any());
    }
    @Test
    void getAllFormsAclNotContainData() throws Exception
    {
        PaginationResponsePayload paginationResponsePayload = new PaginationResponsePayload();
        Mockito.when(formAclService.getAllFormsAcl(any(),any())).thenReturn(paginationResponsePayload);
        formAclController.getAllFormsAcl(1L,1L);
        verify(formAclService,times(1)).getAllFormsAcl(any(),any());
    }

    @Test
    void deleteFormAclSuccess() throws Exception
    {
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setFormId("123");
        formAclDto.setAclId("123");
        Mockito.when(formAclService.deleteFormAcl(any())).thenReturn(1L);
        formAclController.deleteFormAcl("1");
        verify(formAclService,times(1)).deleteFormAcl(any());
    }
    @Test
    void deleteFormAclNotFound() throws Exception
    {
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setFormId("123");
        formAclDto.setAclId("123");
        Mockito.when(formAclService.deleteFormAcl(any())).thenReturn(0L);
        Mockito.when(globalMessageSource.get(any(),any())).thenReturn("abc");
        Assertions.assertThrows(EntityPathException.class,()->formAclController.deleteFormAcl("1"));
    }
}
