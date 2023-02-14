package com.techsophy.tsf.runtime.form.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import static com.techsophy.tsf.runtime.form.constants.ErrorConstants.*;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.CONTAINS_ATLEAST_ONE_ALPHABET;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.COUNT_WORDS;

@Data
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class Validate
{
   Boolean required;
   String pattern;
   String customMessage;
   String custom;
   Boolean customPrivate;
   String json;
   Double min;
   Double max;
   Integer minLength;
   Integer maxLength;
   Boolean strictDateValidation;
   Boolean multiple;
   Boolean unique;
   Integer minWords;
   Integer maxWords;
   String step;
   Integer integer;

   public List<ValidationResult> validate(ComponentData compData)
   {
      List<ValidationResult> validationResultList=new ArrayList<>();
      Component component=compData.comp;
      ObjectMapper objectMapper=new ObjectMapper();
      Map<String,Object> dataMap=objectMapper.convertValue(compData.data, Map.class);
      String key=component.getKey();
      if(dataMap!=null)
      {
          String value = String.valueOf(dataMap.get(key)==null?"":dataMap.get(key));
          validateMisssingField(validationResultList, dataMap, key);
          validateMinLengthCondition(validationResultList, key, value);
          validateMaxLengthCondition(validationResultList, key, value);
          validateMinValueCondition(validationResultList, key, value);
          validateMaxValueCondition(validationResultList, key, value);
          validateMinWordsCondition(validationResultList, key, value);
          validateMaxWordsCondition(validationResultList, key, value);
          validatePatternCondition(validationResultList, component, key, value);
          validateNumberCondition(validationResultList, component, key, value);
          validationResultList.add(new ValidationResult(key));
      }
      return validationResultList;
   }

    private static void validateNumberCondition(List<ValidationResult> validationResultList, Component component, String key, String value) {
        if(component.getType().equals("number")&& value.contains(CONTAINS_ATLEAST_ONE_ALPHABET))
        {
            validationResultList.add(new ValidationResult(key,FORM_DATA_NUMBER_CONDITION_FAILED));
        }
    }

    private void validatePatternCondition(List<ValidationResult> validationResultList, Component component, String key, String value)
    {
        if(this.getPattern()!=null&&!StringUtils.isEmpty(this.getPattern()) && !Pattern.compile(component.getValidate().pattern).matcher(value).matches() )
       {
           validationResultList.add(new ValidationResult(key,FORM_DATA_REGEX_CONDITION_FAILED));
       }
    }

    private void validateMaxWordsCondition(List<ValidationResult> validationResultList, String key, String value) {
        if(this.getMaxWords()!=null && Arrays.stream(value.split(COUNT_WORDS)).count()>this.getMaxWords())
       {
             validationResultList.add(new ValidationResult(key,FORM_DATA_MAX_WORD_CONDITION_EXCEEDED));
       }
    }

    private void validateMinWordsCondition(List<ValidationResult> validationResultList, String key, String value) {
        if(this.getMinWords()!=null && Arrays.stream(value.split(COUNT_WORDS)).count()<this.getMinWords() )
       {
             validationResultList.add(new ValidationResult(key,FORM_DATA_MIN_WORD_CONDITION_FAILED));
       }
    }

    private void validateMaxValueCondition(List<ValidationResult> validationResultList, String key, String value) {
        if(this.getMax()!=null&&!StringUtils.isEmpty(value) && Double.parseDouble(value)>this.getMax())
       {
            validationResultList.add(new ValidationResult(key,FORM_DATA_MAX_VALUE_CONDITION_FAILED));
       }
    }

    private void validateMinValueCondition(List<ValidationResult> validationResultList, String key, String value) {
        if(this.getMin()!=null&&!StringUtils.isEmpty(value) && Double.parseDouble(value)<this.getMin() )
       {
             validationResultList.add(new ValidationResult(key,FORM_DATA_MIN_VALUE_CONDITION_FAILED));
       }
    }

    private void validateMaxLengthCondition(List<ValidationResult> validationResultList, String key, String value) {
        if(this.getMaxLength()!=null&& String.valueOf(value).length()>this.getMaxLength())
       {
           validationResultList.add(new ValidationResult(key,FORM_DATA_MAX_LENGTH_CONDITION_VIOLATED_BY_USER));
       }
    }

    private void validateMinLengthCondition(List<ValidationResult> validationResultList, String key, String value) {
        if(this.getMinLength()!=null && String.valueOf(value).length()<this.getMinLength())
       {
           validationResultList.add(new ValidationResult(key,FORM_DATA_MIN_LENGTH_CONDITION_FAILED));
       }
    }

    private void validateMisssingField(List<ValidationResult> validationResultList, Map<String, Object> dataMap, String key)
    {
        if(Boolean.TRUE.equals(required)&&(!dataMap.containsKey(key)))
        {
           validationResultList.add(new ValidationResult(key,FORM_DATA_MISSING_MANDATORY_FIELDS));
        }
    }

    @AllArgsConstructor
   @Setter
   @Getter
   public static class ComponentData
   {
       private Component comp;
       private Object data;
       private String formId;
   }
}
