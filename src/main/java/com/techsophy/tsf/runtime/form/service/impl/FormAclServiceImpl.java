package com.techsophy.tsf.runtime.form.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.FormAclDto;
import com.techsophy.tsf.runtime.form.dto.PaginationResponsePayload;
import com.techsophy.tsf.runtime.form.entity.FormAclEntity;
import com.techsophy.tsf.runtime.form.repository.FormAclRepository;
import com.techsophy.tsf.runtime.form.service.FormAclService;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
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

import static com.mongodb.client.model.Filters.eq;
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
    public FormAclDto saveFormAcl(FormAclDto formAclDto) throws JsonProcessingException {
        Query query = new Query().addCriteria(Criteria.where(FORM_ID).is(formAclDto.getFormId()));
        Update updateDefinition = new Update().set(ACL_ID,formAclDto.getAclId());
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(true);
        FormAclEntity formAclEntity = mongoTemplate.findAndModify(query,updateDefinition,options,FormAclEntity.class);
        return this.objectMapper.convertValue(formAclEntity,FormAclDto.class);
    }

    @Override
    public FormAclDto getFormAcl(String formId) {
        FormAclEntity formAclEntity = this.formAclRepository.findByFormId(formId)
                .orElse(null);
        return  this.objectMapper.convertValue(formAclEntity,FormAclDto.class);
    }

    @Override
    public PaginationResponsePayload getAllFormsAcl(Long page, Long size) throws JsonProcessingException {
        Pageable pageable = PageRequest.of(Math.toIntExact(page), Math.toIntExact(size));
        Page<FormAclEntity> formAclEntityPage= formAclRepository.findAll(pageable);
        PaginationResponsePayload paginationResponsePayload = this.objectMapper.convertValue(formAclEntityPage,PaginationResponsePayload.class);
       paginationResponsePayload.setPage(Math.toIntExact(page));
        return paginationResponsePayload;
    }

    @Override
    public Long deleteFormAcl(String formId) throws JsonProcessingException {
        MongoCollection<Document> collection = mongoTemplate.getCollection("tp_formAcl");
        DeleteResult deleteOnePublisher = collection.deleteOne(eq("formId", formId));
        return deleteOnePublisher.getDeletedCount();
    }
}
