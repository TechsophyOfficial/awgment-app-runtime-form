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
import static com.techsophy.tsf.runtime.form.constants.FormDataConstants.DATA;
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
        List<LinkedHashMap<String,Object>> componentsList= (List<LinkedHashMap<String,Object>>) components.get(COMPONENTS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        componentsList.forEach(x->{
            Component  component=objectMapper.convertValue(x,Component.class);
            if(component!=null)
            {
                validationResultList.addAll(validateComponent(component,component.getData(formDataMap),formId));
            }
        });
        return validationResultList;
    }

    private void validateInternalComponents(List<ValidationResult> validationResultList, List<Component> componentList, Map<String,Object> data, String formId)
    {
        if(componentList!=null)
        {
            componentList.forEach(x->validationResultList.addAll(validateComponent(x,data,formId)));
        }
    }

    public List<ValidationResult> validateComponent(Component component,Map<String,Object> data,String formId)
    {
        List<ValidationResult> validationResultList=new ArrayList<>();
        if (checkIfComponentIsNotNull(component, data, formId, validationResultList))
            return component.getValidate().validate(new Validate.ComponentData(component, data, formId));
        return validationResultList;
    }

    private boolean checkIfComponentIsNotNull(Component component, Map<String, Object> data, String formId, List<ValidationResult> validationResultList)
    {
        if(component !=null)
        {
            String compType= component.getType();
            if(compType!=null)
            {
                if(component.isContainer())
                {
                    switch (compType)
                    {
                        case "columns":
                        {
                            List<Columns> columns= component.getColumns();
                            validateColumnsList(data, formId, validationResultList, columns);
                            break;
                        }
                        case "table":
                        {
                            checkDataNotNull(component, data, formId, validationResultList);
                            break;
                        }
                        case "datamap":
                        {
                            checkIfDataPresent(component, data, formId, validationResultList);
                            break;
                        }
                        case "datagrid":
                        case "editgrid":
                        {
                            checkDataForGrids(component, data, formId, validationResultList);
                            break;
                        }
                        case "tree":
                        {
                            checkTreeData(component, data, formId, validationResultList);
                            break;
                        }
                        case "tabs":
                        {
                            validateTabsComponents(component, data, formId, validationResultList);
                            break;
                        }
                        default:
                        {
                            List<Component> componentList= component.getComponents();
                            validateInternalComponents(data, formId, validationResultList, componentList);
                        }
                    }
                }
                else
                {
                    return component.getValidate() != null;
                }
            }
        }
        return false;
    }

    private void checkTreeData(Component component, Map<String, Object> data, String formId, List<ValidationResult> validationResultList) {
        if(data !=null)
        {
            checkIfChildrenExists(component, data, formId, validationResultList);
        }
    }

    private void checkDataForGrids(Component component, Map<String, Object> data, String formId, List<ValidationResult> validationResultList) {
        if(data !=null)
        {
            checkIfDataContainsComponents(component, data, formId, validationResultList);
        }
    }

    private void checkIfDataPresent(Component component, Map<String, Object> data, String formId, List<ValidationResult> validationResultList) {
        if(data !=null)
        {
            validationResultList.addAll(validateComponent(component.getValueComponent(), data, formId));
        }
    }

    private void checkDataNotNull(Component component, Map<String, Object> data, String formId, List<ValidationResult> validationResultList) {
        if(data !=null)
        {
            checkRowsListsInTable(component, data, formId, validationResultList);
        }
    }

    private void validateColumnsList(Map<String, Object> data, String formId, List<ValidationResult> validationResultList, List<Columns> columns)
    {
        if(columns !=null)
        {
            List<Component> componentList=new ArrayList<>();
            columns.forEach(x->componentList.addAll(x.getComponent()));
            validateInternalComponents(validationResultList,componentList, data, formId);
        }
    }

    private void checkRowsListsInTable(Component component, Map<String, Object> data, String formId, List<ValidationResult> validationResultList)
    {
        if(component.getRowsList()!=null)
        {
            List<Component> componentList= component.getRowsList()
                    .stream()
                    .flatMap(x->x.stream()
                            .flatMap(y->y.getComponents().stream()))
                    .collect(Collectors.toList());
            validateInternalComponents(validationResultList,componentList, data, formId);
        }
    }

    private void checkIfDataContainsComponents(Component component, Map<String, Object> data, String formId, List<ValidationResult> validationResultList)
    {
        if(data.get(component.getLabel())!=null)
        {
            List<Map<String,Object>> dataGridList= (List<Map<String, Object>>) data.get(component.getLabel());
            List<Component> componentList= component.getComponents();
            iterateDataGridList(formId, validationResultList, dataGridList, componentList);
        }
    }

    private void iterateDataGridList(String formId, List<ValidationResult> validationResultList, List<Map<String, Object>> dataGridList, List<Component> componentList)
    {
        for(Map<String,Object> m: dataGridList)
        {
            validateInternalComponents(validationResultList, componentList,m, formId);
        }
    }

    private void checkIfChildrenExists(Component component, Map<String, Object> data, String formId, List<ValidationResult> validationResultList)
    {
        if(data.get(CHILDREN)!=null)
        {
            List<Map<String,Object>> childrenList= (List<Map<String, Object>>) data.get(CHILDREN);
            List<Component> componentList= component.getComponents();
            data = iterateChildrenComponents(data, formId, validationResultList, childrenList, componentList);
            validateInternalComponents(validationResultList,componentList, data, formId);
        }
    }

    private Map<String, Object> iterateChildrenComponents(Map<String, Object> data, String formId, List<ValidationResult> validationResultList, List<Map<String, Object>> childrenList, List<Component> componentList)
    {
        while (!childrenList.isEmpty())
        {
            validateInternalComponents(validationResultList, componentList, (Map<String, Object>) data.get(DATA), formId);
            data = childrenList.get(0);
            childrenList = (List<Map<String, Object>>) data.get(CHILDREN);
        }
        return data;
    }

    private void validateTabsComponents(Component component, Map<String, Object> data, String formId, List<ValidationResult> validationResultList)
    {
        if(component.getComponents()!=null)
        {
            List<Component> componentList= component.getComponents().get(0).getComponents();
            validateInternalComponents(validationResultList,componentList, data, formId);
        }
    }

    private void validateInternalComponents(Map<String, Object> data, String formId, List<ValidationResult> validationResultList, List<Component> componentList)
    {
        if(componentList !=null)
        {
            validateInternalComponents(validationResultList, componentList, data, formId);
        }
    }
}
