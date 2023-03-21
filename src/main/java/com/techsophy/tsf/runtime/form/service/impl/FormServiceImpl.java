package com.techsophy.tsf.runtime.form.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.FormResponseSchema;
import com.techsophy.tsf.runtime.form.dto.FormSchema;
import com.techsophy.tsf.runtime.form.entity.FormDefinition;
import com.techsophy.tsf.runtime.form.exception.EntityIdNotFoundException;
import com.techsophy.tsf.runtime.form.exception.FormIdNotFoundException;
import com.techsophy.tsf.runtime.form.exception.UserDetailsIdNotFoundException;
import com.techsophy.tsf.runtime.form.repository.FormDefinitionRepository;
import com.techsophy.tsf.runtime.form.service.FormService;
import com.techsophy.tsf.runtime.form.utils.UserDetails;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.stream.Stream;
import static com.techsophy.tsf.runtime.form.constants.ErrorConstants.*;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Service
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class FormServiceImpl implements FormService
{
    private final FormDefinitionRepository formDefinitionRepository;
    private final ObjectMapper objectMapper;
    private final GlobalMessageSource globalMessageSource;
    private final UserDetails userDetails;

    @Override
    public void saveRuntimeForm(FormSchema formSchema) throws JsonProcessingException
    {
        Map<String,Object> loggedInUserDetails =userDetails.getUserDetails().get(0);
        if (isEmpty(loggedInUserDetails.get(ID).toString()))
        {
            throw new UserDetailsIdNotFoundException(LOGGED_IN_USER_ID_NOT_FOUND,globalMessageSource.get(LOGGED_IN_USER_ID_NOT_FOUND,loggedInUserDetails.get(ID).toString()));
        }
        BigInteger loggedInUserId = BigInteger.valueOf(Long.parseLong(loggedInUserDetails.get(ID).toString()));
        FormDefinition formDefinition=new FormDefinition();
        formDefinition.setId(BigInteger.valueOf(Long.parseLong(formSchema.getId())));
        formDefinition.setName(formSchema.getName());
        formDefinition.setVersion(BigInteger.valueOf(formSchema.getVersion()));
        formDefinition.setComponents(formSchema.getComponents());
        formDefinition.setAcls(formSchema.getAcls());
        formDefinition.setProperties(formSchema.getProperties());
        formDefinition.setType(formSchema.getType());
        formDefinition.setIsDefault(formSchema.getIsDefault());
        formDefinition.setElasticPush(formSchema.getElasticPush());
        formDefinition.setCreatedById(String.valueOf(loggedInUserId));
        formDefinition.setCreatedOn(String.valueOf(Date.from(Instant.now())));
        formDefinition.setUpdatedById(String.valueOf(loggedInUserId));
        formDefinition.setUpdatedOn(String.valueOf(Date.from(Instant.now())));
        if(String.valueOf(formSchema.getIsDefault()).equals(NULL))
        {
            formDefinition.setIsDefault(false);
        }
        this.formDefinitionRepository.save(formDefinition);
    }

    @Override
    public FormResponseSchema getRuntimeFormById(String id)
    {
        FormDefinition definition = this.formDefinitionRepository.findById(BigInteger.valueOf(Long.parseLong(id)))
                .orElseThrow(() -> new FormIdNotFoundException(FORM_NOT_FOUND_EXCEPTION,globalMessageSource.get(FORM_NOT_FOUND_EXCEPTION,id)));
        return this.objectMapper.convertValue(definition,FormResponseSchema.class);
    }

    public Stream<FormResponseSchema> getAllRuntimeForms(boolean content, String type)
    {
        if(isEmpty(type))
        {
            return this.formDefinitionRepository.findAll().stream()
                    .map(formio->this.objectMapper.convertValue(formio,FormResponseSchema.class))
                    .map(formSchema->{
                        if(content)
                        {
                            return formSchema;
                        }
                        formSchema.setComponents(null);
                        return formSchema;
                    });
        }
        else
        {
            return this.formDefinitionRepository.findByType(type).stream()
                    .map(formio->
                            this.objectMapper.convertValue(formio,FormResponseSchema.class))
                    .map(formSchema->{
                        if(content)
                        {
                            return formSchema;
                        }
                        formSchema.setComponents(null);
                        return formSchema;
                    });
    }}

    @Override
    public boolean deleteRuntimeFormById(String id)
    {
        if (!formDefinitionRepository.existsById(BigInteger.valueOf(Long.parseLong(id))))
        {
            throw new EntityIdNotFoundException(ENTITY_ID_NOT_FOUND_EXCEPTION,globalMessageSource.get(ENTITY_ID_NOT_FOUND_EXCEPTION,id));
        }
        FormResponseSchema formResponseSchema=getRuntimeFormById(id);
        this.formDefinitionRepository.deleteById(BigInteger.valueOf(Long.parseLong(id)));
        return StringUtils.equals(formResponseSchema.getType(),COMPONENT);
    }

    @Override
    public Stream<FormResponseSchema> searchRuntimeFormByIdOrNameLike(String idOrNameLike, String type) throws UnsupportedEncodingException
    {
        if(StringUtils.isNotEmpty(type))
        {
            return this.formDefinitionRepository
                    .findByNameOrIdAndType(idOrNameLike,type)
                    .stream()
                    .map(formio ->{
                        FormResponseSchema formSchema = this.objectMapper.convertValue(formio, FormResponseSchema.class);
                        formSchema.setComponents(null);
                        return formSchema;
                    });
        }
        return this.formDefinitionRepository
                .findByNameOrId(idOrNameLike)
                .stream()
                .map(formio ->{
                    FormResponseSchema formSchema = this.objectMapper.convertValue(formio, FormResponseSchema.class);
                    formSchema.setComponents(null);
                    return formSchema;
                });
        }
    }
