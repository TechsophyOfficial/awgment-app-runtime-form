package com.techsophy.tsf.runtime.form.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techsophy.tsf.runtime.form.controller.impl.FormAclControllerImpl;
import com.techsophy.tsf.runtime.form.dto.FormAclDto;
import com.techsophy.tsf.runtime.form.dto.PaginationResponsePayload;
import com.techsophy.tsf.runtime.form.model.ApiResponse;
import com.techsophy.tsf.runtime.form.service.FormAclService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

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
    void saveFormIdWithAclID() throws Exception
    {
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setFormId("123");
        formAclDto.setAclId("123");
        Mockito.when(formAclService.saveFormIdWithAclID(any())).thenReturn(formAclDto);
        formAclController.saveFormIdWithAclID(formAclDto);
        verify(formAclService,times(1)).saveFormIdWithAclID(any());
    }
    @Test
    void getFormIdWithAclID() throws Exception
    {
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setFormId("123");
        formAclDto.setAclId("123");
        Mockito.when(formAclService.getFormIdWithAclID(any())).thenReturn(formAclDto);
        formAclController.getFormIdWithAclID(BigInteger.ONE);
        verify(formAclService,times(1)).getFormIdWithAclID(any());
    }
    @Test
    void getAllFormsIdWithAclID() throws Exception
    {
        ApiResponse apiResponse = new ApiResponse("abc",true,"getting successfully");
        PaginationResponsePayload paginationResponsePayload = new PaginationResponsePayload();
        Mockito.when(formAclService.getAllFormsIdWithAclID(any(),any())).thenReturn(paginationResponsePayload);
        formAclController.getAllFormsIdWithAclID(Integer.valueOf("1"),Integer.valueOf("1"));
        verify(formAclService,times(1)).getAllFormsIdWithAclID(any(),any());
    }
    @Test
    void deleteFormIdWithAclID() throws Exception
    {
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setFormId("123");
        formAclDto.setAclId("123");
        doNothing().when(formAclService).deleteFormIdWithAclId(any());
        formAclController.deleteFormIdWithAclID(BigInteger.ONE);
        verify(formAclService,times(1)).deleteFormIdWithAclId(any());
    }
}
