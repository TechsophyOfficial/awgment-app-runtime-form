package com.techsophy.tsf.runtime.form.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.exception.InvalidInputException;
import lombok.RequiredArgsConstructor;
import org.codehaus.plexus.util.StringUtils;
import java.util.*;
import static com.techsophy.tsf.runtime.form.constants.ErrorConstants.NO_COMPONENTS_IN_SCHEMA;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;

@RequiredArgsConstructor
public class ValidateFormUtils
{
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static GlobalMessageSource globalMessageSource;

    public static LinkedHashMap<String,LinkedHashMap<String,Object>> getSchema(Map<String,Object> schema)
    {
        if (schema.isEmpty())
        {
            return null;
        }
       LinkedHashMap<String,LinkedHashMap<String,Object>> fieldsMap = new LinkedHashMap<>();
        if (!schema.containsKey(COMPONENTS))
        {
            throw new InvalidInputException(NO_COMPONENTS_IN_SCHEMA,globalMessageSource.get(NO_COMPONENTS_IN_SCHEMA));
        }
        getLabels(schema,fieldsMap,false,null);
        return fieldsMap;
    }

    public static void getLabels(Map<String,Object> schema,LinkedHashMap<String,LinkedHashMap<String,Object>> fieldsMap,boolean insideEditGrid,String nameOfEditGrid)
    {
        getComponents(schema).forEach(component ->
        {
            LinkedHashMap<String,Object> constraintsMap=new LinkedHashMap<>();
            if(component.containsKey(CONDITIONAL)&&component.get(CONDITIONAL)!=null)
            {
                Map<String,Object> conditionalMap=new HashMap<>();
                if(((Map)component.get(CONDITIONAL)).get(SHOW)!=null&&StringUtils.isNotEmpty(String.valueOf(((Map)(component.get(CONDITIONAL))).get(SHOW))))
                {
                    conditionalMap.put(SHOW, ((Map) component.get(CONDITIONAL)).get(SHOW));
                }
                if(((Map)component.get(CONDITIONAL)).get(WHEN)!=null&&StringUtils.isNotEmpty(String.valueOf(((Map)(component.get(CONDITIONAL))).get(WHEN))))
                {
                    conditionalMap.put(WHEN,((Map)component.get(CONDITIONAL)).get(WHEN));
                }
                if(((Map)component.get(CONDITIONAL)).get(EQ)!=null&&StringUtils.isNotEmpty(String.valueOf(((Map)(component.get(CONDITIONAL))).get(EQ))))
                {
                    conditionalMap.put(EQ, ((Map) component.get(CONDITIONAL)).get(EQ));
                }
                if(conditionalMap.size()!=0)
                {
                    constraintsMap.put(CONDITIONAL,conditionalMap);
                }
            }
            if (component.containsKey(VALIDATE))
            {
                Object validate = component.get(VALIDATE);
                Map<String, Object> validateMap = objectMapper.convertValue(validate,Map.class);
                if(validateMap.containsKey(REQUIRED))
                {
                    constraintsMap.put(REQUIRED,validateMap.get(REQUIRED));
                }
                if(validateMap.get(MIN_LENGTH)!=null&&StringUtils.isNotEmpty(String.valueOf(validateMap.get(MIN_LENGTH))))
                {
                    constraintsMap.put(MIN_LENGTH,validateMap.get(MIN_LENGTH));
                }
                if(validateMap.get(MAX_LENGTH)!=null&&StringUtils.isNotEmpty(String.valueOf(validateMap.get(MAX_LENGTH))))
                {
                    constraintsMap.put(MAX_LENGTH,validateMap.get(MAX_LENGTH));
                }
                if(validateMap.get(MIN_WORDS)!=null&&StringUtils.isNotEmpty(String.valueOf(validateMap.get(MIN_WORDS))))
                {
                    constraintsMap.put(MIN_WORDS,validateMap.get(MIN_WORDS));
                }
                if(validateMap.get(MAX_WORDS)!=null&&StringUtils.isNotEmpty(String.valueOf(validateMap.get(MAX_WORDS))))
                {
                    constraintsMap.put(MAX_WORDS,validateMap.get(MAX_WORDS));
                }
                if(validateMap.get(MIN)!=null&&StringUtils.isNotEmpty(String.valueOf(validateMap.get(MIN))))
                {
                    constraintsMap.put(MIN,validateMap.get(MIN));
                }
                if(validateMap.get(MAX)!=null&&StringUtils.isNotEmpty(String.valueOf(validateMap.get(MAX))))
                {
                    constraintsMap.put(MAX,validateMap.get(MAX));
                }
                fieldsMap.put(String.valueOf(component.get(KEY)),constraintsMap);
            }
            if(component.containsKey(UNIQUE))
            {
                constraintsMap.put(UNIQUE, component.get(UNIQUE));
                fieldsMap.put(String.valueOf(component.get(KEY)),constraintsMap);
            }
            if(component.containsKey(TYPE)&&component.get(TYPE).equals(DATAGRID))
            {
                constraintsMap.put(TYPE,component.get(DATAGRID));
                fieldsMap.put(String.valueOf(component.get(KEY)),constraintsMap);
            }
            if(insideEditGrid&&!nameOfEditGrid.isBlank())
            {
                constraintsMap.put(INSIDE_EDIT_GRID,nameOfEditGrid);
                fieldsMap.put(String.valueOf(component.get(KEY)),constraintsMap);
            }
            if (component.containsKey(COLUMNS)||component.containsKey(COMPONENTS))
            {
                if(constraintsMap.get(INSIDE_EDIT_GRID)!=null)
                {
                    constraintsMap.put(INSIDE_EDIT_GRID,constraintsMap.get(INSIDE_EDIT_GRID) +DOT+component.get(KEY));
                }
                else
                {
                    constraintsMap.put(INSIDE_EDIT_GRID,component.get(KEY));
                }
                fieldsMap.put(String.valueOf(component.get(KEY)),constraintsMap);
                if(constraintsMap.get(INSIDE_EDIT_GRID)!=null)
                {
                    getLabels(component,fieldsMap,true,String.valueOf(constraintsMap.get(INSIDE_EDIT_GRID)));
                }
                else
                {
                    getLabels(component,fieldsMap,true,String.valueOf(component.get(KEY)));
                }
            }
        });
    }

    private static List<Map<String, Object>> getComponents(Map<String, Object> schema)
    {
        if (schema.containsKey(COMPONENTS))
        {
            Object components = schema.get(COMPONENTS);
            if (components instanceof List)
            {
                return objectMapper.convertValue(components, new TypeReference<>() {});
            }
            else
            {
                return List.of();
            }
        }
        else if (schema.containsKey(COLUMNS))
        {
            Object columns = schema.get(COLUMNS);
            if (columns instanceof List)
            {
                return objectMapper.convertValue(columns, new TypeReference<>(){});
            }
            else
            {
                return List.of();
            }
        }
        return List.of();
    }

    public static List<String> getTypes(List<String> keys)
    {
        List<String> types = new ArrayList<>();
        Iterator<String> itr = keys.iterator();
        while(itr.hasNext())
        {
            String s = itr.next();
            if(s.contains(GRID))
            {
                types.add(s);
                itr.remove();
            }
        }
        return types;
    }

    public static void setGlobalMessageSource(GlobalMessageSource globalMessageSource)
    {
        ValidateFormUtils.globalMessageSource =globalMessageSource;
    }
}