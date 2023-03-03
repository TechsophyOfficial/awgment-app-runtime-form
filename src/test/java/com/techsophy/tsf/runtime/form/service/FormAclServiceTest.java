package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.techsophy.idgenerator.IdGeneratorImpl;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.FormAclDto;
import com.techsophy.tsf.runtime.form.dto.PaginationResponsePayload;
import com.techsophy.tsf.runtime.form.entity.FormAclEntity;
import com.techsophy.tsf.runtime.form.exception.EntityIdNotFoundException;
import com.techsophy.tsf.runtime.form.exception.EntityPathException;
import com.techsophy.tsf.runtime.form.repository.FormAclRepository;
import com.techsophy.tsf.runtime.form.service.impl.FormAclServiceImpl;
import com.techsophy.tsf.runtime.form.utils.UserDetails;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.BIGINTEGER_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    @Mock
    MongoCollection<Document> mongoCollection;
    @Mock
    DeleteResult deleteResult;
    @Mock
    IdGeneratorImpl idGenerator;
    @Mock
    UserDetails userDetails;
    @InjectMocks
    FormAclServiceImpl formAclService;
    List<Map<String, Object>> userList = new ArrayList<>();
    @BeforeEach
    public void init()
    {
        Map<String, Object> map = new HashMap<>();
        map.put(ID, BIGINTEGER_ID);
        userList.add(map);
    }

    @Test
    void saveFormAclSuccessWithId() throws JsonProcessingException {
        FormAclDto formAclDto = new FormAclDto("1","1","1");
        FormAclEntity formAclEntity = new FormAclEntity("1","1");
        Mockito.when(objectMapper.convertValue(any(),eq(FormAclEntity.class))).thenReturn(formAclEntity);
        Mockito.when(formAclRepository.findById(any())).thenReturn(Optional.of(formAclEntity));
        Mockito.when(formAclRepository.save(any())).thenReturn(formAclEntity);
        formAclService.saveFormAcl(formAclDto);
        verify(formAclRepository,times(1)).save(any());
    }
    @Test
    void saveFormAclSuccessWithIdNuLL() throws JsonProcessingException {
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setAclId("1");
        formAclDto.setFormId("1");
        FormAclEntity formAclEntity = new FormAclEntity("1","1");
        Mockito.when(objectMapper.convertValue(any(),eq(FormAclEntity.class))).thenReturn(formAclEntity);
        Mockito.when(userDetails.getCurrentAuditor()).thenReturn(Optional.of("1"));
        Mockito.when(formAclRepository.save(any())).thenReturn(formAclEntity);
        formAclService.saveFormAcl(formAclDto);
        verify(formAclRepository,times(1)).save(any());
    }
    @Test
    void saveFormAclExceptionWithIdNotFoundException() throws JsonProcessingException {
        FormAclDto formAclDto = new FormAclDto();
        formAclDto.setId("1");
        formAclDto.setAclId("1");
        formAclDto.setFormId("1");
        FormAclEntity formAclEntity = new FormAclEntity("1","1");
        Mockito.when(objectMapper.convertValue(any(),eq(FormAclEntity.class))).thenReturn(formAclEntity);
        Mockito.when(formAclRepository.findById(any())).thenThrow(EntityIdNotFoundException.class);
        Assertions.assertThrows(EntityIdNotFoundException.class,()->formAclService.saveFormAcl(formAclDto));
    }
    @Test
    void getFormAclSuccess() throws JsonProcessingException {
        FormAclEntity formAclDto = new FormAclEntity("123","123");
        Mockito.when(formAclRepository.findByFormId(any())).thenReturn(Optional.of(formAclDto));
        formAclService.getFormAcl("1");
        Mockito.verify(formAclRepository,Mockito.times(1)).findByFormId(any());
    }
    @Test
    void getFormAclNotFound() throws JsonProcessingException {
        FormAclEntity formAclDto = new FormAclEntity("123","123");
        Mockito.when(formAclRepository.findByFormId(any())).thenThrow(EntityPathException.class);
        Assertions.assertThrows(EntityPathException.class, () ->
                formAclService.getFormAcl("1"));
        Mockito.verify(formAclRepository,Mockito.times(1)).findByFormId(any());
    }
    @Test
    void getAllFormsAclSuccess() throws JsonProcessingException {
        FormAclEntity formAclDto = new FormAclEntity("1223","123");
        Pageable pageable = PageRequest.of(1,1);
        Page<FormAclEntity> page = new PageImpl<>(List.of(formAclDto),pageable,0);
        PaginationResponsePayload paginationResponsePayload = new PaginationResponsePayload();
        paginationResponsePayload.setPage(1);
        Mockito.when(formAclRepository.findAll((Pageable) any())).thenReturn(page);
        Mockito.when(objectMapper.convertValue(page,PaginationResponsePayload.class)).thenReturn(paginationResponsePayload);
        formAclService.getAllFormsAcl(1L,1L);
        Mockito.verify(formAclRepository,Mockito.times(1)).findAll((Pageable) any());
    }
    @Test
    void deleteFormAclSuccess() throws JsonProcessingException {
        FormAclEntity formAclDto = new FormAclEntity("1223","123");
        Document document = new Document();
        document.append("name", "Ram");
        Mockito.when(mongoTemplate.getCollection(any())).thenReturn(mongoCollection);
        Mockito.when(mongoCollection.deleteOne(any())).thenReturn(deleteResult);
        Mockito.when(deleteResult.getDeletedCount()).thenReturn(1L);
        formAclService.deleteFormAcl("1");
        verify(mongoTemplate,times(1)).getCollection(any());
    }
}
