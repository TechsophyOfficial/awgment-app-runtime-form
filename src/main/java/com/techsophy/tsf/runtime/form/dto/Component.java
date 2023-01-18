package com.techsophy.tsf.runtime.form.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;

@Data
public class Component
{
    String html;
    String action;
    Boolean showValidations;
    String title;
    String theme;
    Boolean collapsible;
    String legend;
    String label;
    Boolean cloneRows;
    String cellAlignment;
    Boolean striped;
    Boolean bordered;
    Boolean hover;
    Boolean condensed;
    List<Columns> columns;
    Boolean autoAdjust;
    String tag;
    String className;
    List<Attributes> attrs;
    String content;
    String optionsLabelPosition;
    String footer;
    String width;
    String height;
    String backgroundColor;
    String penColor;
    Boolean hideInputLabels;
    String inputsLabelPosition;
    String labelPosition;
    String displayInTimezone;
    Boolean useLocaleSettings;
    Boolean allowInput;
    String format;
    String placeholder;
    String description;
    String tooltip;
    String conditionalAddButton;
    Boolean reorder;
    String addAnotherPosition;
    Boolean layoutFixed;
    Boolean enableRowGroups;
    Boolean initEmpty;
    String keyLabel;
    Boolean disableAddingRemovingRows;
    Boolean keyBeforeValue;
    String addAnother;
    String shortcut;
    String prefix;
    String suffix;
    Object widget;
    String inputMask;
    String displayMask;
    Boolean allowMultipleMasks;
    String minWidth;
    String maxWidth;
    Boolean keepOverlayRatio;
    String customClass;
    Boolean refreshOnChange;
    @JsonProperty("tabindex")
    String tabIndex;
    Boolean disableonInvalid;
    Boolean openWhenEmpty;
    String editor;
    Boolean autoExpand;
    @JsonProperty("autocomplete")
    String autoComplete;
    Boolean hidden;
    Boolean verticalLayout;
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
    String rowClass;
    Boolean modal;
    String saveRow;
    String removeRow;
    DatePicker datePicker;
    Boolean enableTime;
    TimePicker timePicker;
    Boolean enableDate;
    Boolean multiple;
    String dataSrc;
    DataType data;
    String dataType;
    String idPath;
    String valueProperty;
    String template;
    Object defaultValue;
    String defaultDate;
    Boolean persistent;
    Boolean inlineEdit;
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
    Boolean rowDrafts;
    Boolean displayAsTable;
    Boolean enableMinDateInput;
    Boolean enableMaxDateInput;
    Boolean unique;
    String errorLabel;
    String minSelectedCountMessage;
    String maxSelectedCountMessage;
    String errors;
    String key;
    Conditional conditional;
    String customConditional;
    Overlay overlay;
    String type;
    String endpoint;
    String requestType;
    Boolean isSubmissionDataRequestBody;
    Boolean isDisplayResponseMessage;
    String displayMessageType;
    Boolean mapResponseKey;
    Integer numCols;
    String selectFields;
    String searchField;
    Double searchDebounce;
    Integer minSearch;
    String filter;
    Integer limit;
    String name;
    String value;
    List<Values> values;
    String timezone;
    Boolean wysiwyg;
    Boolean input;
    String refreshOn;
    String refreshOnBlur;
    Boolean clearOnRefresh;
    Boolean searchEnabled;
    Double selectThreshold;
    Boolean readOnlyValue;
    Boolean useExactSearch;
    Boolean dataGridLabel;
    String inputType;
    String inputMode;
    String datepickerMode;
    String caption;
    Boolean defaultOpen;
    String id;
    Integer numRows;
    Boolean fixedSize;
    Boolean enableManualMode;
    Boolean disableClearIcon;
    String manualModeViewString;
    Object size;
    Boolean block;
    String leftIcon;
    String rightIcon;

    String delimiter;
    Integer maxTags;
    String storeas;
    Boolean requireDecimal;
    String provider;
    @JsonProperty("kickbox")
    KickBox kickBox;
    List<Component> components;
    String switchToManualModeLabel;
    Boolean tree;
    Boolean lazyLoad;
    Boolean authenticate;
    Boolean ignoreCache;
    FuseOptions fuseOptions;
    Component valueComponent;
    Integer rowsInteger;
    List<List<ComponentsListInsideTable>> rowsList;


    @JsonAnySetter()
    public void setRows(String name, Object value, @Autowired ObjectMapper objectMapper)
    {
        if(name.equals("rows"))
        {
            if (value instanceof Integer)
            {
                rowsInteger = (Integer) value;
            }
            // if value is map, it must contain 'val1',  'val2' entries
            if (value instanceof List)
            {
               rowsList= objectMapper.convertValue(value, new TypeReference<>(){});
            }
        }
    }

    public Map<String,Object> getData(Map<String,Object> parentData)
    {
            if(this.type.equals("container")||this.type.equals("datamap")||this.type.equals("tree"))
            {
                return (Map<String, Object>) parentData.get(this.label);
            }
            else{
                return parentData;
            }
    }

    public boolean isContainer()
    {
        if(type.equals("textarea")||type.equals("address"))
        {
            return false;
        }
        else return getComponents() != null || getColumns() != null ||getRowsList() != null || getValueComponent() != null;
    }
}
