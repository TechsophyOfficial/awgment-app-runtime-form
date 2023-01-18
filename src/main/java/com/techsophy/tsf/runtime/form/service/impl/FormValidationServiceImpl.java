package com.techsophy.tsf.runtime.form.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.techsophy.tsf.runtime.form.constants.FormDataConstants.CHILDREN;
import static com.techsophy.tsf.runtime.form.constants.FormDataConstants.DATA;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.COMPONENTS;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.EMPTY_STRING;

@Service
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class FormValidationServiceImpl
{
    private ObjectMapper objectMapper;
    private MongoTemplate mongoTemplate;
    private GlobalMessageSource globalMessageSource;

    public List<ValidationResult> validateData(FormResponseSchema formResponseSchema, FormDataSchema formData,String formId)
    {
        List<ValidationResult> validationResultList=new ArrayList<>();
        Map<String, Object> components = formResponseSchema.getComponents();
        Map<String, Object> formDataMap = formData.getFormData();
        List<LinkedHashMap> componentsList= (List<LinkedHashMap>)components.get(COMPONENTS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        componentsList.forEach(x->{
            Component  component=objectMapper.convertValue(x,Component.class);
            validationResultList.addAll(validateComponent(EMPTY_STRING,component,component.getData(formDataMap),formId));
        });
        return validationResultList;
    }

    private void validateInternalComponents(String prefix,List<ValidationResult> validationResultList, List<Component> componentList, Map<String,Object> data, String formId)
    {
        if(componentList!=null)
        {
            componentList.forEach(x->validationResultList.addAll(validateComponent(prefix,x,data,formId)));
        }
    }

    public List<ValidationResult> validateComponent(String prefix,Component component,Map<String,Object> data,String formId)
    {
        List<ValidationResult> validationResultList=new ArrayList<>();
        if(component!=null)
        {
            String compType=component.getType();
            if(compType!=null)
            {
                if(component.isContainer())
                {
                    switch (compType)
                    {
                        case "columns":
                        {
                            List<Columns> columns=component.getColumns();
                            if(columns!=null)
                            {
                                List<Component> componentList=new ArrayList<>();
                                columns.forEach(x->componentList.addAll(x.getComponent()));
                                validateInternalComponents(prefix,validationResultList,componentList,data,formId);
                            }
                            break;
                        }
                        case "table":
                        {
                            if(component.getRowsList()!=null)
                            {
                                List<Component> componentList= component.getRowsList()
                                        .stream()
                                        .flatMap(x->x.stream()
                                                .flatMap(y->y.getComponents().stream()))
                                        .collect(Collectors.toList());
                                validateInternalComponents(prefix,validationResultList,componentList,data,formId);
                            }
                            break;
                        }
                        case "datamap":
                        {
                            validationResultList.addAll(validateComponent(prefix,component.getValueComponent(),data,formId));
                            break;
                        }
                        case "datagrid":
                        case "editgrid":
                        {
                            if(data.get(component.getLabel())!=null)
                            {
                                List<Map<String,Object>> dataGridList= (List<Map<String, Object>>) data.get(component.getLabel());
                                List<Component> componentList=component.getComponents();
                                for(Map m:dataGridList)
                                {
                                    validateInternalComponents(prefix,validationResultList,componentList,m,formId);
                                }
                            }
                            break;
                        }
                        case "tree":
                        {
                            if(data.get(CHILDREN)!=null)
                            {
                                List<Map<String,Object>> childrenList= (List<Map<String, Object>>) data.get(CHILDREN);
                                List<Component> componentList=component.getComponents();
                                while (!childrenList.isEmpty())
                                {
                                    validateInternalComponents(prefix,validationResultList,componentList, (Map<String, Object>) data.get(DATA),formId);
                                    data=childrenList.get(0);
                                    childrenList= (List<Map<String, Object>>) data.get(CHILDREN);
                                }
                                validateInternalComponents(prefix,validationResultList,componentList,data,formId);
                            }
                            break;
                        }
                        case "tabs":
                        {
                            if(component.getComponents()!=null)
                            {
                                List<Component> componentList=component.getComponents().get(0).getComponents();
                                validateInternalComponents(prefix,validationResultList,componentList,data,formId);
                            }
                            break;
                        }
                        default:
                        {
                            List<Component> componentList=component.getComponents();
                            if(componentList!=null)
                            {
                                validateInternalComponents(prefix,validationResultList,componentList,data,formId);
                            }
                        }
                    }
                }
                else
                {
                    if(component.getValidate()!=null)
                    {
                        return component.getValidate().validate(new Validate.ComponentData(component,data,prefix,formId),globalMessageSource,mongoTemplate);
                    }
                }
            }
        }
        return validationResultList;
    }
}
