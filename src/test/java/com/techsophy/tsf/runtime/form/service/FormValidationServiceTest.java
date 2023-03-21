package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.assertions.Assertions;
import com.techsophy.tsf.runtime.form.dto.*;
import com.techsophy.tsf.runtime.form.service.impl.FormValidationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static com.techsophy.tsf.runtime.form.constants.FormDataConstants.CHILDREN;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.EMPTY_STRING;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.ID;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;

@ExtendWith({MockitoExtension.class})
class FormValidationServiceTest
{
    @Mock
    ObjectMapper mockObjectMapper;
    @InjectMocks
    FormValidationServiceImpl formValidationService;
    List<Map<String,Object>> list=new ArrayList<>();

    @Test
    void validateDataTest()
    {
        Map<String, Object> testFormData = new HashMap<>();
        testFormData.put(NAME, NAME_VALUE);
        testFormData.put(AGE,AGE_VALUE);
        testFormData.put(ID,EMPTY_STRING);
        Map<String, Object> testFormMetaData = new HashMap<>();
        testFormMetaData.put(FORM_VERSION, 1);
        Map<String,Object> components=new HashMap<>();
        LinkedHashMap<String,Object> component=new LinkedHashMap<>();
        component.put("label","Text Field");
        component.put("key","textField");
        List<LinkedHashMap> componentList=new ArrayList<>();
        componentList.add(component);
        components.put("display","form");
        components.put("components",componentList);
        FormResponseSchema formResponseSchemaTest = new FormResponseSchema();
        formResponseSchemaTest.setComponents(components);
        FormDataSchema formDataSchema=new FormDataSchema(TEST_ID,TEST_FORM_ID,TEST_VERSION,TEST_FORM_DATA,TEST_FORM_META_DATA);
        Assertions.assertNotNull(formValidationService.validateData(formResponseSchemaTest,formDataSchema,TEST_FORM_ID));
    }

    @Test
    void validateComponentNullTest()
    {
        Component component=new Component();
        Map<String,Object> dataMap=new HashMap<>();
        Assertions.assertNotNull(formValidationService.validateComponent(component,dataMap,TEST_FORM_ID));
    }

    @Test
    void validateComponentTest()
    {
        Component component=new Component();
        component.setType("textField");
        component.setValidate(new Validate(true,"a-z",
        null,null,null,null,1.0,10.0,1,100,
                null,null,null,2,100,null,null));
        Map<String,Object> dataMap=new HashMap<>();
        Assertions.assertNotNull(formValidationService.validateComponent(component,dataMap,TEST_FORM_ID));
    }

    @Test
    void checkComponentColumnsTest()
    {
        Component component=new Component();
        component.setType("columns");
        List<Component> componentList=new ArrayList<>();
        componentList.add(new Component());
        component.setComponents(componentList);
        List<Columns> columnsList=new ArrayList<>();
        Columns columns=new Columns();
        columns.setComponent(componentList);
        columnsList.add(columns);
        component.setColumns(columnsList);
        component.setValidate(new Validate(true,"a-z",
                null,null,null,null,1.0,10.0,1,100,
                null,null,null,2,100,null,null));
        Map<String,Object> dataMap=new HashMap<>();
        Assertions.assertNotNull(formValidationService.validateComponent(component,dataMap,TEST_FORM_ID));
    }

    @Test
    void checkComponentTableTest()
    {
        Component component=new Component();
        component.setType("table");
        List<Component> componentList=new ArrayList<>();
        componentList.add(new Component());
        component.setComponents(componentList);
        List<List<ComponentsListInsideTable>> compList=new ArrayList<>();
        component.setRowsList(compList);
        component.setValidate(new Validate(true,"a-z",
                null,null,null,null,1.0,10.0,1,100,
                null,null,null,2,100,null,null));
        Map<String,Object> dataMap=new HashMap<>();
        Assertions.assertNotNull(formValidationService.validateComponent(component,dataMap,TEST_FORM_ID));
    }

    @Test
    void checkComponentDataMapTest()
    {
        Component component=new Component();
        component.setType("datamap");
        List<Component> componentList=new ArrayList<>();
        componentList.add(new Component());
        component.setComponents(componentList);
        List<List<ComponentsListInsideTable>> compList=new ArrayList<>();
        component.setRowsList(compList);
        component.setValidate(new Validate(true,"a-z",
                null,null,null,null,1.0,10.0,1,100,
                null,null,null,2,100,null,null));
        Map<String,Object> dataMap=new HashMap<>();
        Assertions.assertNotNull(formValidationService.validateComponent(component,dataMap,TEST_FORM_ID));
    }

    @Test
    void checkComponentDataGridTest()
    {
        Component component=new Component();
        component.setLabel("label1");
        component.setType("datagrid");
        List<Component> componentList=new ArrayList<>();
        componentList.add(component);
        component.setComponents(componentList);
        List<List<ComponentsListInsideTable>> compList=new ArrayList<>();
        component.setRowsList(compList);
        component.setValidate(new Validate(true,"a-z",
                null,null,null,null,1.0,10.0,1,100,
                null,null,null,2,100,null,null));
        Map<String,Object> dataMap=new HashMap<>();
        List<Map<String,Object>> dataList=new ArrayList<>();
        dataList.add(new HashMap<>());
        dataMap.put("label1",dataList);
        Assertions.assertNotNull(formValidationService.validateComponent(component,dataMap,TEST_FORM_ID));
    }

    @Test
    void checkComponentTreeTest()
    {
        Component component=new Component();
        component.setType("tree");
        List<Component> componentList=new ArrayList<>();
        componentList.add(new Component());
        component.setComponents(componentList);
        List<List<ComponentsListInsideTable>> compList=new ArrayList<>();
        component.setRowsList(compList);
        component.setValidate(new Validate(true,"a-z",
                null,null,null,null,1.0,10.0,1,100,
                null,null,null,2,100,null,null));
        Map<String,Object> dataMap=new HashMap<>();
        List<Map<String,Object>> childrenList=new ArrayList<>();
        Map<String,Object> children=new HashMap<>();
        children.put("children",new ArrayList<>());
        childrenList.add(children);
        dataMap.put(CHILDREN,childrenList);
        Assertions.assertNotNull(formValidationService.validateComponent(component,dataMap,TEST_FORM_ID));
    }

    @Test
    void checkComponentTabsTest()
    {
        Component component=new Component();
        component.setComponents(new ArrayList<>());
        component.setType("tabs");
        List<Component> componentList=new ArrayList<>();
        componentList.add(new Component());
        component.setComponents(componentList);
        List<List<ComponentsListInsideTable>> compList=new ArrayList<>();
        component.setRowsList(compList);
        component.setValidate(new Validate(true,"a-z",
                null,null,null,null,1.0,10.0,1,100,
                null,null,null,2,100,null,null));
        Map<String,Object> dataMap=new HashMap<>();
        dataMap.put(CHILDREN,new ArrayList<>());
        Assertions.assertNotNull(formValidationService.validateComponent(component,dataMap,TEST_FORM_ID));
    }

    @Test
    void checkDefaultComponentTest()
    {
        Component component=new Component();
        component.setComponents(new ArrayList<>());
        component.setType("default");
        List<Component> componentList=new ArrayList<>();
        componentList.add(new Component());
        component.setComponents(componentList);
        List<List<ComponentsListInsideTable>> compList=new ArrayList<>();
        component.setRowsList(compList);
        component.setValidate(new Validate(true,"a-z",
                null,null,null,null,1.0,10.0,1,100,
                null,null,null,2,100,null,null));
        Map<String,Object> dataMap=new HashMap<>();
        dataMap.put(CHILDREN,new ArrayList<>());
        Assertions.assertNotNull(formValidationService.validateComponent(component,dataMap,TEST_FORM_ID));
    }

}
