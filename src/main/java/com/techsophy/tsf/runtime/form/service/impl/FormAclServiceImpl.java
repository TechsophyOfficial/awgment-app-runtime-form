package com.techsophy.tsf.runtime.form.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.FormAclDto;
import com.techsophy.tsf.runtime.form.dto.PaginationResponsePayload;
import com.techsophy.tsf.runtime.form.entity.FormAclEntity;
import com.techsophy.tsf.runtime.form.exception.EntityIdNotFoundException;
import com.techsophy.tsf.runtime.form.repository.FormAclRepository;
import com.techsophy.tsf.runtime.form.service.FormAclService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import java.math.BigInteger;

import static com.techsophy.tsf.runtime.form.constants.ErrorConstants.ENTITY_ID_NOT_FOUND_EXCEPTION;
import static com.techsophy.tsf.runtime.form.constants.FormAclConstants.ACL_ID;
import static com.techsophy.tsf.runtime.form.constants.FormAclConstants.FORM_ID;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class FormAclServiceImpl implements FormAclService {
    private final FormAclRepository formAclRepository;
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;
    private final GlobalMessageSource globalMessageSource;
    @Override
    public FormAclDto saveFormIdWithAclID(FormAclDto formAclDto) throws JsonProcessingException {
        Query query = new Query().addCriteria(Criteria.where(FORM_ID).is(formAclDto.getFormId()));
        Update updateDefinition = new Update().set(ACL_ID,formAclDto.getAclId());
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(true);
        FormAclEntity formAclEntity = mongoTemplate.findAndModify(query,updateDefinition,options,FormAclEntity.class);
        return this.objectMapper.convertValue(formAclEntity,FormAclDto.class);
    }

    @Override
    public FormAclDto getFormIdWithAclID(BigInteger id) {
        FormAclEntity formAclEntity = this.formAclRepository.findById(id).orElseThrow(()->new EntityIdNotFoundException(ENTITY_ID_NOT_FOUND_EXCEPTION,globalMessageSource.get(ENTITY_ID_NOT_FOUND_EXCEPTION, String.valueOf(id))));
        return  this.objectMapper.convertValue(formAclEntity,FormAclDto.class);
    }

    @Override
    public PaginationResponsePayload getAllFormsIdWithAclID(Integer page, Integer size) throws JsonProcessingException {
        Pageable pageable = PageRequest.of(page, size);
        Page<FormAclEntity> formAclEntityPage= formAclRepository.findAll(pageable);
        PaginationResponsePayload paginationResponsePayload = this.objectMapper.convertValue(formAclEntityPage,PaginationResponsePayload.class);
       paginationResponsePayload.setPage(page);
        return paginationResponsePayload;
    }

    @Override
    public void deleteFormIdWithAclId(BigInteger id) throws JsonProcessingException {
        this.formAclRepository.deleteById(id);
    }
}
