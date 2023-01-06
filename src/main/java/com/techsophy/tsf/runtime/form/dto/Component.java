package com.techsophy.tsf.runtime.form.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.*;

import static com.techsophy.tsf.runtime.form.constants.FormDataConstants.DATA;

@Data
public class Component
{
    String label;
    String labelPosition;
    String placeholder;
    String description;
    Boolean enableManualMode;
    Boolean disableClearIcon;
    String manualModeViewString;
    String tooltip;
    String prefix;
    String suffix;
    String size;
    String inputFormat;
    String displayMask;
    Boolean dbIndex;
    String customClass;
    @JsonProperty("tabindex")
    String tabIndex;
    @JsonProperty("autocomplete")
    String autoComplete;
    Boolean hidden;
    Boolean hideLabel;
    Boolean mask;
    Boolean autofocus;
    String errorLabel;
    String errors;
    String key;
    List tags;
    List addons;
    List logic;
    String validateOn;
    Validate validate;
    LinkedHashMap<Object,Object> attributes;
    LinkedHashMap<Object,Object> properties;
    Boolean disabled;
    Boolean tableView;
    Boolean modalEdit;
    Boolean multiple;
    Boolean persistent;
    Conditional conditional;
    String customConditional;
    Boolean unique;
    String id;
    String inputMask;
    Boolean allowMultipleMasks;
    Boolean showWordCount;
    Boolean showCharCount;
    Boolean spellcheck;
    @JsonProperty("protected")
    Boolean isProtected;
    @JsonProperty("case")
    String caseName;
    Boolean truncateMultipleSpaces;
    Boolean encrypted;
    String redrawOn;
    Boolean clearOnHide;
    String customDefaultValue;
    String calculateValue;
    Boolean calculateServer;
    Boolean allowCalculateOverride;
    Overlay overlay;
    String type;
    Boolean input;
    String refreshOn;
    Boolean dataGridLabel;
    String inputType;
    Object defaultValue;
    Boolean delimiter;
    Boolean requireDecimal;
    String provider;
    @JsonProperty("kickbox")
    KickBox kickBox;
    Boolean block;
    List<Component> components;
    LinkedHashMap providerOptions;
    String switchToManualModeLabel;
    Boolean tree;
    Boolean lazyLoad;
    List<Columns> columns;
    List<List<Internal>> rows;
    Component valueComponent;


//    public String getPrefix(String parentPrefix)
//    {
//
//    }

    public Map<String,Object> getData(Map<String,Object> parentData,String type)
    {
            if(type.equals("fieldset")||type.equals("well")
            ||type.equals("table")||type.equals("tabs"))
            {
                return parentData;
            }
            else if(type.equals("container")||type.equals("datamap"))
            {
                return (Map<String, Object>) parentData.get(this.label);
            }
            else if(type.equals("datagrid")||type.equals("editgrid"))
            {
               List<Map> mapList= (List<Map>) parentData.get(this.label);
               Map<String,Object> requiredMap=new HashMap<>();
               for(Map m:mapList)
               {
                   requiredMap.putAll(m);
               }
               return requiredMap;
            }
            else if(type.equals("tree"))
            {
              return (Map<String, Object>) parentData.get(DATA);
            }
        return parentData;
    }

    public boolean isContainer(Component component)
    {
        if(component.getComponents()!=null||component.getColumns()!=null||
                component.getRows()!=null||component.getValueComponent()!=null)
        {
            return true;
        }
        return false;
    }
}
