package com.techsophy.tsf.runtime.form.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.techsophy.idgenerator.IdGeneratorImpl;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.FormAclDto;
import com.techsophy.tsf.runtime.form.dto.PaginationResponsePayload;
import com.techsophy.tsf.runtime.form.entity.FormAclEntity;
import com.techsophy.tsf.runtime.form.repository.FormAclRepository;
import com.techsophy.tsf.runtime.form.service.FormAclService;
import com.techsophy.tsf.runtime.form.utils.UserDetails;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;
import static com.techsophy.tsf.runtime.form.constants.ErrorConstants.ENTITY_ID_NOT_FOUND_EXCEPTION;
import static com.techsophy.tsf.runtime.form.constants.ErrorConstants.LOGGED_IN_USER_ID_NOT_FOUND;
import static com.techsophy.tsf.runtime.form.constants.FormAclConstants.ID;
import static com.techsophy.tsf.runtime.form.constants.FormAclConstants.NO_RECORD_FOUND;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class FormAclServiceImpl implements FormAclService {
    private final FormAclRepository formAclRepository;
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;
    private final GlobalMessageSource globalMessageSource;
    private final IdGeneratorImpl idGenerator;
    private final UserDetails userDetails;
    @Override
    public FormAclDto saveFormAcl(FormAclDto formAclDto) {
        FormAclEntity existFormAcl = formAclRepository.findByFormId(formAclDto.getFormId()).orElse(null);
        FormAclEntity formAclEntity = objectMapper.convertValue(formAclDto,FormAclEntity.class);
        String loggedInUserId = userDetails.getCurrentAuditor().orElse(null);
        formAclEntity.setUpdatedById(String.valueOf(loggedInUserId));
        formAclEntity.setUpdatedOn(String.valueOf(Date.from(Instant.now())));
        if(existFormAcl==null)
        {
            //Here one to one mapping for Id and FormId
            formAclEntity.setId(new BigInteger(formAclDto.getFormId()));
            formAclEntity.setCreatedOn(String.valueOf(Date.from(Instant.now())));
            formAclEntity.setCreatedById(String.valueOf(loggedInUserId));

        }
        else if(!String.valueOf(existFormAcl.getId()).equalsIgnoreCase(formAclDto.getId()))
        {
            return null;
        }
        else
        {
            formAclEntity.setCreatedById(existFormAcl.getCreatedById());
            formAclEntity.setCreatedOn(existFormAcl.getCreatedOn());
        }
        formAclEntity = formAclRepository.save(formAclEntity);
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
