package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.FormAclDto;
import com.techsophy.tsf.runtime.form.entity.FormAclEntity;
import com.techsophy.tsf.runtime.form.repository.FormAclRepository;
import com.techsophy.tsf.runtime.form.service.impl.FormAclServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
 class FormAclServiceTest {
    @Mock
    FormAclRepository formAclRepository;
    @Mock
    MongoTemplate mongoTemplate;
    @Mock
    ObjectMapper objectMapper;
    @Mock
    GlobalMessageSource globalMessageSource;
    @InjectMocks
    FormAclServiceImpl formAclService;

    @Test
    void saveFormIdWithAclID() throws JsonProcessingException {
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setFormId("123");
        formAclDto.setAclId("123");
        formAclService.saveFormIdWithAclID(formAclDto);
        Mockito.verify(mongoTemplate,Mockito.times(1)).findAndModify((Query) any(), (UpdateDefinition) any(), (FindAndModifyOptions) any(),any());
    }
    @Test
    void getFormIdWithAclID() throws JsonProcessingException {
        FormAclEntity formAclDto = new FormAclEntity("123","123");
        Mockito.when(formAclRepository.findById(any())).thenReturn(Optional.of(formAclDto));
        formAclService.getFormIdWithAclID(BigInteger.ONE);
        Mockito.verify(formAclRepository,Mockito.times(1)).findById(any());
    }
    @Test
    void getAllFormsIdWithAclID() throws JsonProcessingException {
        FormAclEntity formAclDto = new FormAclEntity("1223","123");
        Pageable pageable = PageRequest.of(1,1);
        Page<FormAclEntity> page = new PageImpl<>(List.of(formAclDto),pageable,0);
        Mockito.when(formAclRepository.findAll((Pageable) any())).thenReturn(page);
        formAclService.getAllFormsIdWithAclID(1,1);
        Mockito.verify(formAclRepository,Mockito.times(1)).findAll((Pageable) any());
    }
    @Test
    void deleteFormIdWithAclId() throws JsonProcessingException {
        FormAclEntity formAclDto = new FormAclEntity("1223","123");
        doNothing().when(formAclRepository).deleteById(any());
        formAclService.deleteFormIdWithAclId(BigInteger.ONE);
        Mockito.verify(formAclRepository,Mockito.times(1)).deleteById(any());
    }
}
