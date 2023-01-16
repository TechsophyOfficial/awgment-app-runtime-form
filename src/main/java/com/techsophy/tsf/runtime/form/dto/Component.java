package com.techsophy.tsf.runtime.form.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.*;

@Data
public class Component
{
    String label;
    String labelPosition;
    String placeholder;
    String description;
    String tooltip;
    String prefix;
    String suffix;
    Widget widget;
    String inputMask;
    String displayMask;
    Boolean allowMultipleMasks;
    String customClass;
    @JsonProperty("tabindex")
    String tabIndex;
    String editor;
    Boolean autoExpand;
    @JsonProperty("autocomplete")
    String autoComplete;
    Boolean hidden;
    Boolean hideLabel;
    Boolean showWordCount;
    Boolean showCharCount;
    Boolean mask;
    Boolean autofocus;
    @JsonProperty("spellcheck")
    Boolean spellCheck;
    Boolean disabled;
    Boolean tableView;
    Boolean modalEdit;
    DatePicker datePicker;
    Boolean enableTime;
    Boolean enableDate;
    Boolean multiple;
    Object defaultValue;
    String defaultDate;
    Boolean persistent;
    Object inputFormat;
    @JsonProperty("protected")
    Boolean isProtected;
    Boolean dbIndex;
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
    String validateOn;
    Validate validate;
    Boolean enableMinDateInput;
    Boolean enableMaxDateInput;
    Boolean unique;
    String errorLabel;
    String errors;
    String key;
    Conditional conditional;
    String customConditional;
    Overlay overlay;
    String type;
    String timezone;
    Object rows;
    Boolean wysiwyg;
    Boolean input;
    String refreshOn;
    Boolean dataGridLabel;
    String inputType;
    String inputMode;
    String datepickerMode;
    String id;
    Boolean fixedSize;
    Boolean enableManualMode;
    Boolean disableClearIcon;
    String manualModeViewString;
    Integer size;
    String delimiter;
    Integer maxTags;
    String storeas;
    Boolean requireDecimal;
    String provider;
    @JsonProperty("kickbox")
    KickBox kickBox;
    Boolean block;
    List<Component> components;
    String switchToManualModeLabel;
    Boolean tree;
    Boolean lazyLoad;
    Component valueComponent;
    List<Columns> columns;

    public Map<String,Object> getData(Map<String,Object> parentData)
    {
            if(this.type.equals("container")||this.type.equals("datamap")||this.type.equals("tree"))
            {
                return (Map<String, Object>) parentData.get(this.label);
            }
            else if(this.type.equals("datagrid")||this.type.equals("editgrid"))
            {
               return parentData;
            }
            return parentData;
    }

    public boolean isContainer()
    {
        if(type.equals("textarea")||type.equals("address"))
        {
            return false;
        }
        else return getComponents() != null || getColumns() != null || getRows() != null || getValueComponent() != null;
    }
}
