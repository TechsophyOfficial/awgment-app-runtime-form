package com.techsophy.tsf.runtime.form.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.FormDataAuditResponse;
import com.techsophy.tsf.runtime.form.dto.FormDataAuditResponseSchema;
import com.techsophy.tsf.runtime.form.dto.FormDataAuditSchema;
import com.techsophy.tsf.runtime.form.entity.FormDataAuditDefinition;
import com.techsophy.tsf.runtime.form.exception.FormIdNotFoundException;
import com.techsophy.tsf.runtime.form.exception.InvalidInputException;
import com.techsophy.tsf.runtime.form.exception.UserDetailsIdNotFoundException;
import com.techsophy.tsf.runtime.form.service.FormDataAuditService;
import com.techsophy.tsf.runtime.form.utils.UserDetails;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.Instant;
import java.util.*;

import static com.techsophy.tsf.runtime.form.constants.ErrorConstants.*;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Service
@AllArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class FormDataAuditServiceImpl implements FormDataAuditService
{
    private MongoTemplate mongoTemplate;
    private final GlobalMessageSource globalMessageSource;
    private final UserDetails userDetails;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(FormDataAuditServiceImpl.class);

    @Override
    public FormDataAuditResponse saveFormDataAudit(FormDataAuditSchema formDataAuditSchema) throws JsonProcessingException
    {
        Map<String, Object> loggedInUserDetails = userDetails.getUserDetails().get(0);
        if (isEmpty(String.valueOf(loggedInUserDetails.get(ID))))
        {
            throw new UserDetailsIdNotFoundException(LOGGED_IN_USER_ID_NOT_FOUND,globalMessageSource.get(LOGGED_IN_USER_ID_NOT_FOUND, loggedInUserDetails.get(ID).toString()));
        }
        if(isEmpty(formDataAuditSchema.getFormId()))
        {
            throw new InvalidInputException(FORM_ID_CANNOT_BE_EMPTY,globalMessageSource.get(FORM_ID_CANNOT_BE_EMPTY,formDataAuditSchema.getFormId()));
        }
        BigInteger loggedInUserId = BigInteger.valueOf(Long.parseLong(loggedInUserDetails.get(ID).toString()));
        FormDataAuditDefinition formDataAuditDefinition=objectMapper.convertValue(formDataAuditSchema,FormDataAuditDefinition.class);
        formDataAuditDefinition.setCreatedById(String.valueOf(loggedInUserId));
        formDataAuditDefinition.setCreatedOn(String.valueOf(Date.from(Instant.now())));
        mongoTemplate.save(formDataAuditDefinition, TP_RUNTIME_FORM_DATA +formDataAuditSchema.getFormId()+AUDIT);
        return new FormDataAuditResponse(formDataAuditSchema.getId(), formDataAuditSchema.getVersion());
    }

    @Override
    public List<FormDataAuditResponseSchema> getAllFormDataAuditByFormIdAndDocumentId(String formId, String formDataId)
    {
        LinkedHashMap<String, Object> formData;
        Map<String,Object> formMetaData;
        FormDataAuditResponseSchema formDataAuditResponseSchema;
        List<FormDataAuditResponseSchema> formDataAuditResponseSchemasList=new ArrayList<>();
        Bson filter= Filters.eq(FORM_DATA_ID,Long.valueOf(formDataId));
        MongoCursor<Document> cursor;
        if(!mongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA +formId+AUDIT))
        {
            throw new FormIdNotFoundException(FORM_DATA_DOES_NOT_EXISTS_WITH_GIVEN_FORMID,globalMessageSource.get(FORM_DATA_DOES_NOT_EXISTS_WITH_GIVEN_FORMID,formId));
        }
        try
       {
           MongoCollection<Document> collection=mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA +formId+AUDIT);
           FindIterable<Document> documents=collection.find(filter);
           cursor=documents.iterator();
           while(cursor.hasNext())
           {
               Document document=cursor.next();
               if(!document.isEmpty())
               {
                   formData = objectMapper.convertValue(document.get(FORM_DATA),LinkedHashMap.class);
                   formMetaData=objectMapper.convertValue(document.get(FORM_META_DATA),Map.class);
                   formDataAuditResponseSchema=new FormDataAuditResponseSchema
                           (document.get(UNDERSCORE_ID).toString(),document.get(FORM_DATA_ID).toString(),document.get(FORM_ID).toString(),
                                   Integer.valueOf(String.valueOf(document.get(VERSION))),formData,formMetaData,
                                   document.get(CREATED_BY_ID).toString(),String.valueOf(document.get(CREATED_ON)));
                   formDataAuditResponseSchemasList.add(formDataAuditResponseSchema);
               }
           }
       }
       catch(Exception e)
       {
           logger.info(e.getMessage());
           throw new FormIdNotFoundException(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC,globalMessageSource.get(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC,formDataId));
       }
        return formDataAuditResponseSchemasList;
    }
}
