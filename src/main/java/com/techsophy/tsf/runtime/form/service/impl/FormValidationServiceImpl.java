package com.techsophy.tsf.runtime.form.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.techsophy.tsf.runtime.form.constants.FormDataConstants.CHILDREN;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.COMPONENTS;

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
            if(x.get("type").equals("textarea"))
            {
                x.remove("rows");
            }
            if(x.get("type").equals("tree"))
            {
                List<LinkedHashMap> componentList= (List<LinkedHashMap>) x.get("components");
                componentList.forEach(y->y.remove("rows"));
                x.put("components",componentList);
            }
            Component  component=objectMapper.convertValue(x,Component.class);
            validationResultList.addAll(validateComponent("",component,formDataMap,formId));
        });
        return validationResultList;
    }

    public List<ValidationResult> internalLoop(String prefix,Component component,List<ValidationResult> validationResultList,List<Component> componentList,Map<String,Object> data,String formId)
    {
        for(Component c:componentList)
        {
            validationResultList.addAll(validateComponent(prefix,c,component.getData(data,c.getType()),formId));
        }
        return validationResultList;
    }

    public List<ValidationResult> validateComponent(String prefix, Component component,Map<String,Object> data,String formId)
    {
        List<ValidationResult> validationResultList=new ArrayList<>();
        if(component.isContainer(component)&&!component.getType().equals("address"))
        {
            if(!component.getType().equals("fieldset")&&!component.getType().equals("well")
            &&!component.getType().equals("table")&&!component.getType().equals("tabs"))
            {
                prefix=prefix+"."+component.getLabel();
            }
            if(component.getComponents()!=null)
            {
                if(component.getType().equals("tabs"))
                {
                    List<Component> componentList=component.getComponents().get(0).getComponents();
                    validationResultList.addAll(internalLoop(prefix,component,validationResultList,componentList,data,formId));
                }
                else if(component.getType().equals("tree"))
                {
                    if(data.get(component.getLabel())!=null)
                    {
                        data= (Map<String, Object>) data.get(component.getLabel());
                    }
                    if(data.get(CHILDREN)!=null)
                    {
                        List<Map<String,Object>> childrenList= (List<Map<String, Object>>) data.get(CHILDREN);
                        List<Component> componentList=component.getComponents();
                        while (!childrenList.isEmpty())
                        {
//                            for(Component c:componentList)
//                            {
//                                validationResultList.addAll(validateComponent(prefix,c,component.getData(data,new HashMap<>()),formId));
//                            }
                            internalLoop(prefix,component,validationResultList,componentList,data,formId);
                            data=childrenList.get(0);
                            childrenList= (List<Map<String, Object>>) data.get(CHILDREN);
                        }
//                        for(Component c:componentList)
//                        {
//                            validationResultList.addAll(validateComponent(prefix,c,component.getData(data,new HashMap<>()),formId));
//                        }
                       validationResultList.addAll(internalLoop(prefix,component,validationResultList,componentList,data,formId));
                    }
                }
                else
                {
//                    for(Component c:component.getComponents())
//                    {
//                        validationResultList.addAll(validateComponent(prefix,c,component.getData(data,new HashMap<>()),formId));
//                    }
                    validationResultList.addAll(internalLoop(prefix,component,validationResultList,component.getComponents(),data,formId));
                }
            }
            if(component.getType().equals("datamap"))
            {
                validationResultList.addAll(validateComponent(prefix,component.getValueComponent(),component.getData(data,component.getValueComponent().getType()),formId));
            }
            List<Columns> columns=component.getColumns();
            if(columns!=null)
            {
                List<Component> componentList=new ArrayList<>();
                for(Columns c:columns)
                {
                    componentList.addAll(c.getComponent());
                }
//               List<Component> componentList= columns.stream().map(x->x.getComponent().get(0)).collect(Collectors.toList());
//               for (Component value : c)
//                {
//                    validationResultList.addAll(validateComponent(prefix, value, data, formId));
//                }
                validationResultList.addAll(internalLoop(prefix,component,validationResultList,componentList,data,formId));
            }
            if(component.getRows()!=null)
            {
                List<Component> componentList= component.getRows().stream().flatMap(x -> x.stream().map(Internal::getComponents)).flatMap(Collection::stream).collect(Collectors.toList());
//                for(Component component1:componentList)
//                {
//                   validationResultList.addAll(validateComponent(prefix,component1,data,formId));
//                }
                validationResultList.addAll(internalLoop(prefix,component,validationResultList,componentList,data,formId));
            }
        }
        else
        {
            if(component.getValidate()!=null)
            {
                return component.getValidate().validate(new Validate.ComponentData(component, data,prefix,formId),globalMessageSource,mongoTemplate);
            }
        }
        return validationResultList;
    }
}
