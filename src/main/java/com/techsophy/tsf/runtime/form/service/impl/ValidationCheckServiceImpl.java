package com.techsophy.tsf.runtime.form.service.impl;

import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.exception.InvalidInputException;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

import static com.techsophy.tsf.runtime.form.constants.ErrorConstants.INVALID_EMAIL_PATTERN;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;

@Service
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class ValidationCheckServiceImpl
{
    private GlobalMessageSource globalMessageSource;
    private MongoTemplate mongoTemplate;
    private static final Logger logger = LoggerFactory.getLogger(ValidationCheckServiceImpl.class);

    public List<String> allFieldsValidations(LinkedHashMap<String,LinkedHashMap<String,Object>> fieldsMap,Map<String, Object> data,String formId,String id)
    {
        return this.checkMissingUniqueLengthWordCount(fieldsMap,data,formId,id);
    }

//    public List<ValidationResult> validateInputFormData(List<FieldsValidation> fieldsValidationList, Map<String,Object> data,String formId,String id)
//    {
//        List<ValidationResult> validationResultList=new ArrayList<>();
//        for(FieldsValidation fieldsValidation:fieldsValidationList)
//        {
//            ValidationResult validationResult=new ValidationResult();
//            String key= fieldsValidation.getKey();
//            validationResult.setResponse(key);
//            ConstraintsList constraintsList=fieldsValidation.getConstraintsList();
//            if(constraintsList.getRequired()&&(!data.containsKey(key)||StringUtils.isEmpty(String.valueOf(data.get(key)))))
//            {
//                logger.info("field "+key+" is empty "+StringUtils.isEmpty(String.valueOf(data.get(key))));
//                validationResult.setResult(0);
//                validationResultList.add(validationResult);
//                return  validationResultList;
//            }
//            if(constraintsList.getMinLength()!=null)
//            {
//               if(String.valueOf(data.get(key)).length()<constraintsList.getMinLength())
//               {
//                   validationResult.setResult(2);
//                   validationResultList.add(validationResult);
//                   return validationResultList;
//               }
//            }
//            if(constraintsList.getMaxLength()!=null)
//            {
//                if(String.valueOf(data.get(key)).length()>constraintsList.getMaxLength())
//                {
//                    validationResult.setResult(3);
//                    validationResultList.add(validationResult);
//                    return validationResultList;
//                }
//            }
//            if(constraintsList.getMin()!=null)
//            {
//                if(String.valueOf(data.get(key)).matches(CONTAINS_ATLEAST_ONE_ALPHABET))
//                {
//                    validationResult.setResult(4);
//                    validationResultList.add(validationResult);
//                    return validationResultList;
//                }
//                if(Double.parseDouble(String.valueOf(data.get(key)))<constraintsList.getMin())
//                {
//                    validationResult.setResult(5);
//                    validationResultList.add(validationResult);
//                    return validationResultList;
//                }
//            }
//            if(constraintsList.getMax()!=null)
//            {
//                if(String.valueOf(data.get(key)).matches(CONTAINS_ATLEAST_ONE_ALPHABET))
//                {
//                    validationResult.setResult(6);
//                    validationResultList.add(validationResult);
//                    return validationResultList;
//                }
//                if(Double.parseDouble(String.valueOf(data.get(key)))>constraintsList.getMax())
//                {
//                    validationResult.setResult(7);
//                    validationResultList.add(validationResult);
//                    return validationResultList;
//                }
//            }
//            if(constraintsList.getMinWords()!=null)
//            {
//                if(Arrays.stream(String.valueOf(data.get(key)).split(COUNT_WORDS)).count()<constraintsList.getMinWords())
//                {
//                    validationResult.setResult(8);
//                    validationResultList.add(validationResult);
//                    return validationResultList;
//                }
//            }
//            if(constraintsList.getMaxWords()!=null)
//            {
//                if(Arrays.stream(String.valueOf(data.get(key)).split(COUNT_WORDS)).count()>constraintsList.getMaxWords())
//                {
//                    validationResult.setResult(9);
//                    validationResultList.add(validationResult);
//                    return validationResultList;
//                }
//            }
//        }
//        ValidationResult validationResult=new ValidationResult();
//        validationResult.setResult(-1);
//        validationResult.setResponse(EMPTY_STRING);
//        return validationResultList;
//    }

    public  List<String> checkMissingUniqueLengthWordCount(LinkedHashMap<String,LinkedHashMap<String,Object>> fieldsMap, Map<String,Object> data, String formId,String id)
    {
        if(data.containsKey(EMAIL))
        {
            String email =  data.get(EMAIL).toString();
            boolean isEmailMatched = Pattern.matches(EMAIL_PATTERN,email);
            if(!isEmailMatched)
            {
                throw new InvalidInputException(INVALID_EMAIL_PATTERN,globalMessageSource.get(INVALID_EMAIL_PATTERN));
            }
        }
        Iterator<String> keys=fieldsMap.keySet().iterator();
        List<Criteria> criteriaList=new LinkedList<>();
        while(keys.hasNext())
        {
            String key = keys.next();
            String when = EMPTY_STRING;
            Map<String,Object> conditionalMap=((Map)fieldsMap.get(key).get(CONDITIONAL));
            if(conditionalMap!=null)
            {
                String w;
                if(conditionalMap.get(WHEN)!=null)
                {
                    when = String.valueOf(conditionalMap.get(WHEN));
                    if(when.contains(DOT))
                    {
                        String[] stringArray=when.split("\\.");
                        w=stringArray[stringArray.length-1];
                        if (conditionalMap.get(SHOW).equals(true)&&!data.get(w).equals(String.valueOf(conditionalMap.get(EQ))))
                        {
                            continue;
                        }
                    }
                    if(data.get(when)!=null)
                    {
                        if (conditionalMap.get(SHOW).equals(true)&&!data.get(when).equals(String.valueOf(conditionalMap.get(EQ))))
                        {
                            continue;
                        }
                    }
                }
            }


//            component.getValidate().validate(data.get(component.getLabel()))
            if(fieldsMap.get(key).getOrDefault(REQUIRED,false).equals(Boolean.TRUE)&&(!data.containsKey(key)||data.get(key)==null||StringUtils.isEmpty(String.valueOf(data.get(key)))
                    ||StringUtils.isBlank(String.valueOf(data.get(key)))))
            {
                logger.info("field "+key+" is empty "+StringUtils.isEmpty(String.valueOf(data.get(key))));
                return List.of(String.valueOf(0),key);
            }
            if(fieldsMap.get(key).get(MIN_LENGTH)!=null&&!String.valueOf(fieldsMap.get(key).get(MIN_LENGTH)).isBlank())
            {
                int minLength = String.valueOf(fieldsMap.get(key).get(MIN_LENGTH)).isBlank()?0:Integer.parseInt(String.valueOf(fieldsMap.get(key).get(MIN_LENGTH)));
                if(minLength !=0&&String.valueOf(data.get(key)).length()< minLength)
                {
                    return List.of(String.valueOf(2),key);
                }
            }
            if(fieldsMap.get(key).get(MAX_LENGTH)!=null&&!String.valueOf(fieldsMap.get(key).get(MAX_LENGTH)).isBlank())
            {
                int maxLength =  String.valueOf(fieldsMap.get(key).get(MAX_LENGTH)).isBlank()?0:Integer.parseInt(String.valueOf(fieldsMap.get(key).get(MAX_LENGTH)));
                if(maxLength !=0&&String.valueOf(data.get(key)).length()>maxLength)
                {
                    return List.of(String.valueOf(3),key);
                }
            }
            if(fieldsMap.get(key).get(MIN)!=null&&!String.valueOf(fieldsMap.get(key).get(MIN)).isBlank())
            {
                if(String.valueOf(data.get(key)).matches(CONTAINS_ATLEAST_ONE_ALPHABET))
                {
                    return List.of(String.valueOf(4),key);
                }
                double minLimit = String.valueOf(fieldsMap.get(key).get(MIN)).isBlank()?0:Double.parseDouble(String.valueOf(fieldsMap.get(key).get(MIN)));
                double givenValue =String.valueOf(data.get(key)).isBlank()?-1:Double.parseDouble(String.valueOf(data.get(key)));
                if(givenValue < minLimit)
                {
                    return List.of(String.valueOf(5),key);
                }
            }
            if(fieldsMap.get(key).get(MAX)!=null&&!String.valueOf(fieldsMap.get(key).get(MAX)).isBlank())
            {
                if(String.valueOf(data.get(key)).matches(CONTAINS_ATLEAST_ONE_ALPHABET))
                {
                    return List.of(String.valueOf(6),key);
                }
                double maxLimit = String.valueOf(fieldsMap.get(key).get(MAX)).isBlank()?-1:Double.parseDouble(String.valueOf(fieldsMap.get(key).get(MAX)));
                double givenValue =String.valueOf(data.get(key)).isBlank()?0:Double.parseDouble(String.valueOf(data.get(key)));
                if(givenValue > maxLimit)
                {
                    return List.of(String.valueOf(7),key);
                }
            }
            if(fieldsMap.get(key).get(MIN_WORDS)!=null&&!String.valueOf(fieldsMap.get(key).get(MIN_WORDS)).isBlank())
            {
                int minWords = String.valueOf(fieldsMap.get(key).get(MIN_WORDS)).isBlank()?0:Integer.parseInt(String.valueOf(fieldsMap.get(key).get(MIN_WORDS)));
                String givenString= String.valueOf(data.get(key));
                long givenStringCount=Arrays.stream(givenString.split(COUNT_WORDS)).count();
                if(givenStringCount<minWords)
                {
                    return List.of(String.valueOf(8),key);
                }
            }
            if(fieldsMap.get(key).get(MAX_WORDS)!=null&&!String.valueOf(fieldsMap.get(key).get(MAX_WORDS)).isBlank())
            {
                int maxWords = String.valueOf(fieldsMap.get(key).get(MAX_WORDS)).isBlank()?0:Integer.parseInt(String.valueOf(fieldsMap.get(key).get(MAX_WORDS)));
                String givenString= String.valueOf(data.get(key));
                long givenStringCount=Arrays.stream(givenString.split(COUNT_WORDS)).count();
                if(givenStringCount>maxWords)
                {
                    return List.of(String.valueOf(9),key);
                }
            }
//            String insideEditGridKey;
//            if(((Map)fieldsMap.get(key)).get(INSIDE_EDIT_GRID)!=null&&fieldsMap.get(key).getOrDefault(UNIQUE,false).equals(Boolean.TRUE))
//            {
//                if(!String.valueOf(((Map)fieldsMap.get(key)).get(INSIDE_EDIT_GRID)).equals(key))
//                {
//                    insideEditGridKey=((Map)fieldsMap.get(key)).get(INSIDE_EDIT_GRID)+DOT+key;
//                    criteriaList.add(Criteria.where(FORM_DATA_DOT+insideEditGridKey).is(data.get(key)));
//                }
//                else
//                {
//                    criteriaList.add(Criteria.where(FORM_DATA_DOT+key).is(data.get(key)));
//                }
//                if(mongoTemplate.exists(Query.query(new Criteria().orOperator(criteriaList)), TP_RUNTIME_FORM_DATA_+formId))
//                {
//                    return  List.of(String.valueOf(1),key);
//                }
//            }
            if(fieldsMap.get(key).getOrDefault(UNIQUE,false).equals(Boolean.TRUE)&&StringUtils.isBlank(id)&&!when.contains("\\."))
            {
                criteriaList.add(Criteria.where(FORM_DATA_DOT+key).is(data.get(key)));
                if(mongoTemplate.exists(Query.query(new Criteria().orOperator(criteriaList)), TP_RUNTIME_FORM_DATA_+formId))
                {
                    return  List.of(String.valueOf(1),key);
                }
            }
        }
        return List.of(String.valueOf(-1),EMPTY_STRING);
    }
}