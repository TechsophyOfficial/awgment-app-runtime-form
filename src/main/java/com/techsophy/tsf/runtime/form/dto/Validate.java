package com.techsophy.tsf.runtime.form.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.*;
import java.util.regex.Pattern;

import static com.techsophy.tsf.runtime.form.constants.ErrorConstants.*;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;

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
   String integer;


   public List<ValidationResult> validate(ComponentData compData,GlobalMessageSource globalMessageSource,MongoTemplate mongoTemplate)
   {
      List<ValidationResult> validationResultList=new ArrayList<>();
      Component component=compData.comp;
      ObjectMapper objectMapper=new ObjectMapper();
      Map<String,Object> dataMap=objectMapper.convertValue(compData.data, Map.class);
      String label=component.getLabel();
      String value = String.valueOf(dataMap.get(label)==null?"":dataMap.get(label));
      String prefix=compData.prefix;
      String formId=compData.formId;
      if(component.getValidate().required&&(!dataMap.containsKey(label)))
      {
         validationResultList.add(new ValidationResult(label,FORM_DATA_MISSING_MANDATORY_FIELDS,globalMessageSource.get(FORM_DATA_MISSING_MANDATORY_FIELDS,label)).addPrefix(prefix));
      }
      if(component.getValidate().getMinLength()!=null)
      {
         if(String.valueOf(value).length()<component.getValidate().getMinLength())
         {
            validationResultList.add(new ValidationResult(label,FORM_DATA_MIN_LENGTH_CONDITION_FAILED,globalMessageSource.get(FORM_DATA_MIN_LENGTH_CONDITION_FAILED,label)).addPrefix(prefix));
         }
      }
     if(component.getValidate().getMaxLength()!=null)
      {
         if(String.valueOf(value).length()>component.getValidate().getMaxLength())
         {
            validationResultList.add(new ValidationResult(label,FORM_DATA_MAX_LENGTH_CONDITION_VIOLATED_BY_USER,globalMessageSource.get(FORM_DATA_MAX_LENGTH_CONDITION_VIOLATED_BY_USER,label)).addPrefix(prefix));
         }
      }
     if(component.getValidate().getMin()!=null&&!StringUtils.isEmpty(value))
      {
         if(Double.parseDouble(value)<component.getValidate().getMin())
         {
            validationResultList.add(new ValidationResult(label,FORM_DATA_MIN_VALUE_CONDITION_FAILED,globalMessageSource.get(FORM_DATA_MIN_VALUE_CONDITION_FAILED,label)).addPrefix(prefix));
         }
      }
     if(component.getValidate().getMax()!=null&&!StringUtils.isEmpty(value))
      {
         if(Double.parseDouble(value)>component.getValidate().getMax())
         {
            validationResultList.add(new ValidationResult(label,FORM_DATA_MAX_VALUE_CONDITION_FAILED,globalMessageSource.get(FORM_DATA_MAX_VALUE_CONDITION_FAILED,label)).addPrefix(prefix));
         }
      }
      if(component.getValidate().getMinWords()!=null)
      {
         if(Arrays.stream(value.split(COUNT_WORDS)).count()<component.getValidate().getMinWords())
         {
            validationResultList.add(new ValidationResult(label,FORM_DATA_MIN_WORD_CONDITION_FAILED,globalMessageSource.get(FORM_DATA_MIN_WORD_CONDITION_FAILED,label)).addPrefix(prefix));
         }
      }
      if(component.getValidate().getMaxWords()!=null)
      {
         if(Arrays.stream(value.split(COUNT_WORDS)).count()>component.getValidate().getMaxWords())
         {
            validationResultList.add(new ValidationResult(label,FORM_DATA_MAX_WORD_CONDITION_EXCEEDED,globalMessageSource.get(FORM_DATA_MAX_WORD_CONDITION_EXCEEDED,label)).addPrefix(prefix));
         }
      }
      if(component.getValidate().getPattern()!=null&&!StringUtils.isEmpty(component.getValidate().getPattern()))
      {
          if(!Pattern.compile(component.getValidate().pattern).matcher(value).matches())
          {
              validationResultList.add(new ValidationResult(label,FORM_DATA_REGEX_CONDITION_FAILED,globalMessageSource.get(FORM_DATA_REGEX_CONDITION_FAILED,label)).addPrefix(prefix));
          }
      }
      if(component.getValidate().getUnique())
      {
          List<Criteria> criteriaList=new LinkedList<>();
          criteriaList.add(Criteria.where("formData."+prefix).is(value));
          if(mongoTemplate.exists(Query.query(new Criteria().orOperator(criteriaList)), TP_RUNTIME_FORM_DATA_+formId))
          {
              validationResultList.add(new ValidationResult(label,FORM_DATA_HAS_DUPLICATE,globalMessageSource.get(FORM_DATA_HAS_DUPLICATE,label)).addPrefix(prefix));
          }
      }
      if(component.getType().equals("number")&&value.contains(CONTAINS_ATLEAST_ONE_ALPHABET))
      {
          validationResultList.add(new ValidationResult(label,FORM_DATA_NUMBER_CONDITION_FAILED,globalMessageSource.get(FORM_DATA_NUMBER_CONDITION_FAILED,label)).addPrefix(prefix));
      }
       validationResultList.add(new ValidationResult(label).addPrefix(prefix));
      return validationResultList;
   }

   @AllArgsConstructor
   public static class ComponentData
   {
       Component comp;
       Object data;
       String prefix;
       String formId;
   }
}
