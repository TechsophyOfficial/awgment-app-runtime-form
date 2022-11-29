package com.techsophy.tsf.runtime.form.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.result.DeleteResult;
import com.techsophy.idgenerator.IdGeneratorImpl;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.*;
import com.techsophy.tsf.runtime.form.entity.FormDataDefinition;
import com.techsophy.tsf.runtime.form.exception.*;
import com.techsophy.tsf.runtime.form.service.FormDataAuditService;
import com.techsophy.tsf.runtime.form.service.FormDataService;
import com.techsophy.tsf.runtime.form.service.FormService;
import com.techsophy.tsf.runtime.form.utils.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.FacetOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import static com.techsophy.tsf.runtime.form.constants.ErrorConstants.*;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;

@Slf4j
@Service
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class FormDataServiceImpl implements FormDataService
{
    @Value(DEFAULT_PAGE_LIMIT)
    private int defaultPageLimit;
    @Value(GATEWAY_URI)
    String gatewayApi;
    @Value(ELASTIC_SOURCE)
    boolean elasticSource;
    @Value(ELASTIC_ENABLE)
    boolean elasticEnable;
    private MongoTemplate mongoTemplate;
    private UserDetails userDetails = null;
    private GlobalMessageSource globalMessageSource = null;
    private IdGeneratorImpl idGenerator = null;
    private WebClientWrapper webClientWrapper = null;
    private TokenUtils tokenUtils = null;
    private ObjectMapper objectMapper = null;
    private FormDataAuditService formDataAuditService = null;
    private static final Logger logger = LoggerFactory.getLogger(FormDataServiceImpl.class);
    private FormService formService = null;
    private ValidationCheckServiceImpl validationCheckServiceImpl;

    @Override
    public FormDataResponse saveFormData(FormDataSchema formDataSchema) throws JsonProcessingException
    {
        String formId=formDataSchema.getFormId();
        if (StringUtils.isEmpty(formId))
        {
            throw new InvalidInputException(FORM_ID_CANNOT_BE_EMPTY, globalMessageSource.get(FORM_ID_CANNOT_BE_EMPTY,formId));
        }
        boolean documentFlag = false;
        BigInteger id;
        Integer version = null;
        FindIterable<Document> documents;
        Map<String, Object> loggedInUserDetails = userDetails.getUserDetails().get(0);
        if (StringUtils.isEmpty(String.valueOf(loggedInUserDetails.get(ID))))
        {
            throw new UserDetailsIdNotFoundException(LOGGED_IN_USER_ID_NOT_FOUND,globalMessageSource.get(LOGGED_IN_USER_ID_NOT_FOUND,String.valueOf(loggedInUserDetails.get(ID))));
        }
        WebClient webClient;
        String token = tokenUtils.getTokenFromContext();
        if (StringUtils.isNotEmpty(token))
        {
            webClient = webClientWrapper.createWebClient(token);
        }
        else
        {
            throw new InvalidInputException(TOKEN_NOT_NULL,globalMessageSource.get(TOKEN_NOT_NULL, tokenUtils.getLoggedInUserId()));
        }
        BigInteger loggedInUserId = BigInteger.valueOf(Long.parseLong(String.valueOf(loggedInUserDetails.get(ID))));
        FormResponseSchema formResponseSchema = formService.getRuntimeFormById(formId);
        LinkedHashMap<String,LinkedHashMap<String,Object>> fieldsMap =ValidateFormUtils.getSchema(formResponseSchema.getComponents());
        Map<String, Object> data = null;
        if (formDataSchema.getFormData() != null)
        {
           data = formDataSchema.getFormData();
        }
        assert data != null;
        LinkedHashMap<String,Object> modifiedInputData =refineInputData(data,new LinkedHashMap<>());
        assert fieldsMap != null;
        List<String> result= validationCheckServiceImpl.allFieldsValidations(fieldsMap,modifiedInputData,formId,formDataSchema.getId());
        String key = EMPTY_STRING;
        if(result.get(1)!=null)
        {
            key=result.get(1);
        }
        switch (Integer.parseInt(result.get(0)))
         {
             case 0:   throw new InvalidInputException(FORM_DATA_MISSING_MANDATORY_FIELDS,globalMessageSource.get(FORM_DATA_MISSING_MANDATORY_FIELDS,key));
             case 1:   throw new InvalidInputException(FORM_DATA_HAS_DUPLICATE,globalMessageSource.get(FORM_DATA_HAS_DUPLICATE,key));
             case 2:   throw new InvalidInputException(FORM_DATA_MIN_LENGTH_CONDITION_FAILED,globalMessageSource.get(FORM_DATA_MIN_LENGTH_CONDITION_FAILED,key));
             case 3:   throw new InvalidInputException(FORM_DATA_MAX_LENGTH_CONDITION_VIOLATED_BY_USER,globalMessageSource.get(FORM_DATA_MAX_LENGTH_CONDITION_VIOLATED_BY_USER,key));
             case 4:
             case 6:   throw new InvalidInputException(FORM_DATA_INTEGER_FIELDS_CANNOT_CONTAIN_ALPHABETS,globalMessageSource.get(FORM_DATA_INTEGER_FIELDS_CANNOT_CONTAIN_ALPHABETS,key));
             case 5:   throw new InvalidInputException(FORM_DATA_MIN_VALUE_CONDITION_FAILED,globalMessageSource.get(FORM_DATA_MIN_VALUE_CONDITION_FAILED,key));
             case 7:   throw new InvalidInputException(FORM_DATA_MAX_VALUE_CONDITION_FAILED,globalMessageSource.get(FORM_DATA_MAX_VALUE_CONDITION_FAILED,key));
             case 8:   throw new InvalidInputException(FORM_DATA_MIN_WORD_CONDITION_FAILED,globalMessageSource.get(FORM_DATA_MIN_WORD_CONDITION_FAILED,key));
             case 9:   throw new InvalidInputException(FORM_DATA_MAX_WORD_CONDITION_EXCEEDED,globalMessageSource.get(FORM_DATA_MAX_WORD_CONDITION_EXCEEDED,key));
             default:
             {
                    Map<String, Object> formDataMap = new LinkedHashMap<>();
                    id = idGenerator.nextId();
                    formDataMap.put(UNDERSCORE_ID,Long.parseLong(String.valueOf(id)));
                    formDataMap.put(FORM_DATA, formDataSchema.getFormData());
                    formDataMap.put(FORM_META_DATA, formDataSchema.getFormMetadata());
                    formDataMap.put(VERSION,String.valueOf(1));
                    formDataMap.put(CREATED_BY_ID, String.valueOf(loggedInUserId));
                    formDataMap.put(CREATED_ON, Instant.now());
                    formDataMap.put(CREATED_BY_NAME, loggedInUserDetails.get(USER_DEFINITION_FIRST_NAME) +SPACE+loggedInUserDetails.get(USER_DEFINITION_LAST_NAME));
                    formDataMap.put(UPDATED_BY_ID, String.valueOf(loggedInUserId));
                    formDataMap.put(UPDATED_ON,Instant.now());
                    formDataMap.put(UPDATED_BY_NAME, loggedInUserDetails.get(USER_DEFINITION_FIRST_NAME) +SPACE+ loggedInUserDetails.get(USER_DEFINITION_LAST_NAME));
                    String uniqueDocumentId = formDataSchema.getId();
                    if (StringUtils.isEmpty(uniqueDocumentId))
                    {
                        uniqueDocumentId = String.valueOf(formDataSchema.getFormData().get(ID));
                    }
                    Map<String,Object> savedFormDataMap=new LinkedHashMap<>();
                    Map<String,Object> updatedFormData=new LinkedHashMap<>();
                        if (mongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ + formId))
                        {
                            if (StringUtils.isEmpty(uniqueDocumentId) || uniqueDocumentId.equals(NULL))
                            {
                                savedFormDataMap = mongoTemplate.save(formDataMap,TP_RUNTIME_FORM_DATA_+formId);
                                version = 1;
                                FormDataAuditSchema formDataAuditSchema = new FormDataAuditSchema(String.valueOf(idGenerator.nextId()), String.valueOf(formDataMap.get(UNDERSCORE_ID)),formId,version,
                                        (Map<String, Object>) savedFormDataMap.get(FORM_DATA), (Map<String, Object>) savedFormDataMap.get(FORM_META_DATA));
                                this.formDataAuditService.saveFormDataAudit(formDataAuditSchema);
                            }
                            else
                            {
                                 documents = mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_+formId).find();
                                 for (Document document : documents)
                                 {
                                    String underscoreId = String.valueOf(document.get(UNDERSCORE_ID));
                                    if (!document.isEmpty() && (underscoreId.equals(uniqueDocumentId)))
                                    {
                                        id = BigInteger.valueOf(Long.parseLong(uniqueDocumentId));
                                        formDataMap.replace(UNDERSCORE_ID, Long.parseLong(String.valueOf(id)));
                                        formDataMap.replace(VERSION, String.valueOf(Integer.parseInt(String.valueOf(document.get(VERSION))) + 1));
                                        formDataMap.replace(CREATED_BY_ID, String.valueOf(document.get(CREATED_BY_ID)));
                                        formDataMap.replace(CREATED_ON, ((Date) document.get(CREATED_ON)).toInstant());
                                        formDataMap.put(CREATED_BY_NAME, String.valueOf(document.get(CREATED_BY_NAME)));
                                        Document newDocument = new Document(formDataMap);
                                        Bson filter = Filters.eq(UNDERSCORE_ID, Long.valueOf(uniqueDocumentId));
                                        FindOneAndReplaceOptions findOneAndReplaceOptions = new FindOneAndReplaceOptions();
                                        findOneAndReplaceOptions.returnDocument(ReturnDocument.AFTER);
                                        updatedFormData = mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_+formId).findOneAndReplace(filter,newDocument,findOneAndReplaceOptions);
                                        documentFlag = true;
                                        version = Integer.valueOf(String.valueOf(formDataMap.get(VERSION)));
                                        assert updatedFormData != null;
                                        FormDataAuditSchema formDataAuditSchema = new FormDataAuditSchema(String.valueOf(idGenerator.nextId()), String.valueOf(formDataMap.get(UNDERSCORE_ID)),formId, version,
                                                (Map<String, Object>) updatedFormData.get(FORM_DATA),(Map<String, Object>) updatedFormData.get(FORM_META_DATA));
                                        this.formDataAuditService.saveFormDataAudit(formDataAuditSchema);
                                    }
                                }
                                if (!documentFlag)
                                {
                                    throw new InvalidInputException(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC,globalMessageSource.get(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC,uniqueDocumentId));
                                }
                            }
                        }
                        else
                        {
                            if (formDataSchema.getFormData().get(ID) != null || formDataSchema.getId()!= null)
                            {
                                uniqueDocumentId = formDataSchema.getId();
                                if (StringUtils.isEmpty(uniqueDocumentId))
                                {
                                    uniqueDocumentId = String.valueOf(formDataSchema.getFormData().get(ID));
                                }
                                throw new FormIdNotFoundException(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC,globalMessageSource.get(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC,uniqueDocumentId));
                            }
                            savedFormDataMap = mongoTemplate.save(formDataMap,TP_RUNTIME_FORM_DATA_+formId);
                            version = 1;
                            FormDataAuditSchema formDataAuditSchema = new
                                    FormDataAuditSchema(String.valueOf(idGenerator.nextId()), String.valueOf(formDataMap.get(UNDERSCORE_ID)),formId,version,
                                    (Map<String, Object>)savedFormDataMap.get(FORM_DATA),(Map<String, Object>) savedFormDataMap.get(FORM_META_DATA));
                            this.formDataAuditService.saveFormDataAudit(formDataAuditSchema);
                        }
                    if(elasticEnable)
                    {
                        String response;
                        if (StringUtils.isEmpty(uniqueDocumentId)|| uniqueDocumentId.equals(NULL))
                        {
                            savedFormDataMap.remove(UNDERSCORE_ID);
                            savedFormDataMap.put(ID,String.valueOf(id));
                            savedFormDataMap.replace(CREATED_ON,String.valueOf(Instant.now()));
                            savedFormDataMap.replace(UPDATED_ON,String.valueOf(Instant.now()));
                            try
                            {
                                response=webClientWrapper.webclientRequest(webClient,gatewayApi+ELASTIC_VERSION1+SLASH+TP_RUNTIME_FORM_DATA_+formId+PARAM_SOURCE+elasticSource,POST,savedFormDataMap);
                                logger.info(response);
                            }
                            catch (Exception e)
                            {
                                logger.error(e.getMessage());
                                Bson filter= Filters.eq(UNDERSCORE_ID,Long.valueOf(String.valueOf(id)));
                                mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_+formId).deleteMany(filter);
                                throw new RecordUnableToSaveException(UNABLE_TO_SAVE_IN_ELASTIC_AND_DB,globalMessageSource.get(UNABLE_TO_SAVE_IN_ELASTIC_AND_DB));
                            }
                        }
                        else
                        {
                            try
                            {
                                response=webClientWrapper.webclientRequest(webClient,gatewayApi+ELASTIC_VERSION1+SLASH+uniqueDocumentId+PARAM_INDEX_NAME+TP_RUNTIME_FORM_DATA_+formId,GET,null);
                                logger.info(response);
                            }
                            catch (Exception e)
                            {
                                logger.error(e.getMessage());
                                throw new InvalidInputException(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC,globalMessageSource.get(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC,uniqueDocumentId));
                            }
                            Map<String,Object> responseMap=this.objectMapper.readValue(response,Map.class);
                            if(responseMap.get(DATA)==null)
                            {
                                throw new InvalidInputException(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC,globalMessageSource.get(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC,uniqueDocumentId));
                            }
                            Map<String,Object> dataMap=this.objectMapper.convertValue(responseMap.get(DATA),LinkedHashMap.class);
                            version=Integer.valueOf(String.valueOf(dataMap.get(VERSION)));
                            version=version+1;
                            id = BigInteger.valueOf(Long.parseLong(uniqueDocumentId));
                            updatedFormData.remove(UNDERSCORE_ID);
                            updatedFormData.put(ID,String.valueOf(id));
                            updatedFormData.replace(VERSION,String.valueOf(version));
                            updatedFormData.replace(CREATED_BY_ID,String.valueOf(dataMap.get(CREATED_BY_ID)));
                            updatedFormData.replace(CREATED_ON,String.valueOf(dataMap.get(CREATED_ON)));
                            updatedFormData.replace(CREATED_BY_NAME,String.valueOf(dataMap.get(CREATED_BY_NAME)));
                            updatedFormData.replace(UPDATED_ON,String.valueOf(Instant.now()));
                            try
                            {
                                response=webClientWrapper.webclientRequest(webClient,gatewayApi+ELASTIC_VERSION1+SLASH+TP_RUNTIME_FORM_DATA_+formId+PARAM_SOURCE+elasticSource,POST,updatedFormData);
                                logger.info(response);
                            }
                            catch (Exception e)
                            {
                                Document newDocument = new Document(responseMap);
                                Bson filter= Filters.eq(UNDERSCORE_ID,Long.valueOf(String.valueOf(id)));
                                FindOneAndReplaceOptions findOneAndReplaceOptions = new FindOneAndReplaceOptions();
                                findOneAndReplaceOptions.returnDocument(ReturnDocument.AFTER);
                                mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_+formId).findOneAndReplace(filter,newDocument,findOneAndReplaceOptions);
                                throw new RecordUnableToSaveException(UNABLE_TO_SAVE_IN_ELASTIC_AND_DB,globalMessageSource.get(UNABLE_TO_SAVE_IN_ELASTIC_AND_DB));
                            }
                        }
                    }
             }
         }
        return new FormDataResponse(String.valueOf(id),version);
    }

    @Override
    public FormDataResponse updateFormData(FormDataSchema formDataSchema) throws JsonProcessingException
    {
        String formId=formDataSchema.getFormId();
        if (StringUtils.isEmpty(formDataSchema.getFormId()))
        {
            throw new InvalidInputException(FORM_ID_CANNOT_BE_EMPTY,globalMessageSource.get(FORM_ID_CANNOT_BE_EMPTY,formId));
        }
        if(StringUtils.isEmpty(formDataSchema.getId()))
        {
            throw new InvalidInputException(ID_CANNOT_BE_EMPTY,globalMessageSource.get(ID_CANNOT_BE_EMPTY));
        }
        if(!mongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ + formDataSchema.getFormId()))
        {
            throw new FormIdNotFoundException(FORM_DATA_DOES_NOT_EXISTS_WITH_GIVEN_FORMID,globalMessageSource.get(FORM_DATA_DOES_NOT_EXISTS_WITH_GIVEN_FORMID,formId));
        }
        Map<String, Object> loggedInUserDetails = userDetails.getUserDetails().get(0);
        if (StringUtils.isEmpty(String.valueOf(loggedInUserDetails.get(ID))))
        {
            throw new UserDetailsIdNotFoundException(LOGGED_IN_USER_ID_NOT_FOUND,globalMessageSource.get(LOGGED_IN_USER_ID_NOT_FOUND,String.valueOf(loggedInUserDetails.get(ID))));
        }
        BigInteger loggedInUserId = BigInteger.valueOf(Long.parseLong(String.valueOf(loggedInUserDetails.get(ID))));
        MongoCursor<Document> cursor;
        Map<String, Object> formDataMap = new LinkedHashMap<>();
        Map<String,Object> updatedFormData=new LinkedHashMap<>();
        Map<String,Object> updatedFormMetaData=new HashMap<>();
        Bson filter = Filters.eq(UNDERSCORE_ID, Long.valueOf(formDataSchema.getId()));
        int version = 2;
        Document document = null;
        FindOneAndReplaceOptions findOneAndReplaceOptions = new FindOneAndReplaceOptions();
        findOneAndReplaceOptions.returnDocument(ReturnDocument.AFTER);
        try
        {
            MongoCollection<Document> collection = mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_+formId);
            FindIterable<Document> documents = collection.find(filter);
            cursor = documents.iterator();
            while (cursor.hasNext())
            {
                 document = cursor.next();
                if (!document.isEmpty())
                {
                    version=Integer.parseInt(String.valueOf(document.get(VERSION)))+ 1;
                    Map<String,Object> documentFormData=objectMapper.convertValue(document.get(FORM_DATA),LinkedHashMap.class);
                    Map<String,Object> documentFormMetaData=objectMapper.convertValue(document.get(FORM_META_DATA), Map.class);
                    if(documentFormData!=null)
                    {
                        updatedFormData.putAll(documentFormData);
                    }
                    if (formDataSchema.getFormData() != null)
                    {
                        updatedFormData.putAll(formDataSchema.getFormData());
                    }
                    if (documentFormMetaData != null)
                    {
                        updatedFormMetaData.putAll(documentFormMetaData);
                    }
                    if (formDataSchema.getFormMetadata() != null)
                    {
                        updatedFormMetaData.putAll(formDataSchema.getFormMetadata());
                    }
                    formDataMap.put(UNDERSCORE_ID, Long.valueOf(String.valueOf(document.get(UNDERSCORE_ID))));
                    formDataMap.put(FORM_DATA, updatedFormData);
                    formDataMap.put(FORM_META_DATA, updatedFormMetaData);
                    formDataMap.put(VERSION, String.valueOf(version));
                    formDataMap.put(CREATED_BY_ID, String.valueOf(document.get(CREATED_BY_ID)));
                    formDataMap.put(CREATED_ON, ((Date) document.get(CREATED_ON)).toInstant());
                    formDataMap.put(CREATED_BY_NAME, String.valueOf(document.get(CREATED_BY_NAME)));
                    formDataMap.put(UPDATED_BY_ID, String.valueOf(loggedInUserId));
                    formDataMap.put(UPDATED_ON, Instant.now());
                    formDataMap.put(UPDATED_BY_NAME, loggedInUserDetails.get(USER_DEFINITION_FIRST_NAME) + SPACE + loggedInUserDetails.get(USER_DEFINITION_LAST_NAME));
                }
            }
        } catch (Exception e)
        {
            logger.error(e.getMessage());
        }
        Document updatedDocument = new Document(formDataMap);
        mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_+formId).findOneAndReplace(filter, updatedDocument,findOneAndReplaceOptions);
        if (elasticEnable)
        {
            WebClient webClient;
            String token = tokenUtils.getTokenFromContext();
            log.info(LOGGED_USER + loggedInUserId);
            log.info(TOKEN+token);
            if (StringUtils.isNotEmpty(token))
            {
                webClient = webClientWrapper.createWebClient(token);
            }
            else
            {
                throw new InvalidInputException(TOKEN_NOT_NULL, globalMessageSource.get(TOKEN_NOT_NULL, String.valueOf(loggedInUserId)));
            }
            String response;
            log.info(GATEWAY+gatewayApi);
            formDataMap.remove(UNDERSCORE_ID);
            assert document != null;
            formDataMap.put(ID, String.valueOf(document.get(UNDERSCORE_ID)));
            formDataMap.put(CREATED_ON, String.valueOf(formDataMap.get(CREATED_ON)));
            formDataMap.put(UPDATED_ON, String.valueOf(formDataMap.get(UPDATED_ON)));
            try
            {
                response = webClientWrapper.webclientRequest(webClient, gatewayApi + ELASTIC_VERSION1 + SLASH + TP_RUNTIME_FORM_DATA_ + formId+ PARAM_SOURCE + elasticSource, POST, formDataMap);
                logger.info(response);
            }
            catch (Exception e)
            {
                logger.error(e.getMessage());
                filter = Filters.eq(UNDERSCORE_ID, Long.valueOf(String.valueOf(formDataSchema.getId())));
                mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_+formId).findOneAndReplace(filter, document, findOneAndReplaceOptions);
                throw new RecordUnableToSaveException(UNABLE_TO_UPDATE_IN_MONGODB_AND_ELASTIC_DB, globalMessageSource.get(UNABLE_TO_UPDATE_IN_MONGODB_AND_ELASTIC_DB));
            }
        }
        return new FormDataResponse(formDataSchema.getId(),version);
    }

    public LinkedHashMap<String,Object> refineInputData(Map<String,Object> data,LinkedHashMap<String,Object> modifiedData)
    {
        for(String key:data.keySet())
        {
                Object obj=data.get(key);
                if((obj instanceof  ArrayList))
                {
                    ArrayList<Object> editGrid=(ArrayList<Object>) obj;
                        for(int i=0;i<editGrid.size();i++)
                        {
                            Object obj1=editGrid.get(i);
                            if(obj1 instanceof  Map)
                            {
                                LinkedHashMap<String,Object> map= (LinkedHashMap<String, Object>) obj1;
                                modifiedData.put(key,editGrid);
                                for(String innerKey:map.keySet())
                                {
                                    if(map.get(innerKey) instanceof  List)
                                    {
                                        refineInputData(map,modifiedData);
                                    }
                                    else
                                    {
                                        modifiedData.put(innerKey,map.get(innerKey));
                                    }
                                }
                            }
                        }
                    modifiedData.put(key,data.get(key));
                }
            else
            {
                modifiedData.put(key,data.get(key));
            }
        }
        return modifiedData;
    }

    @Override
    public List getAllFormDataByFormId(String formId,String relations,String filter,String sortBy,String sortOrder)
    {
        if (elasticEnable&&relations==null)
        {
            List<Map<String,Object>> contentList;
            List<Map<String,Object>> responseList=new ArrayList<>();
            if (StringUtils.isEmpty(sortBy) && !StringUtils.isEmpty(sortOrder) || !StringUtils.isEmpty(sortBy) && StringUtils.isEmpty(sortOrder))
            {
                throw new InvalidInputException(SORTBY_AND_SORTORDER_BOTH_SHOULD_BE_GIVEN, globalMessageSource.get(SORTBY_AND_SORTORDER_BOTH_SHOULD_BE_GIVEN));
            }
            WebClient webClient;
            String token = tokenUtils.getTokenFromContext();
            if (StringUtils.isNotEmpty(token))
            {
                webClient = webClientWrapper.createWebClient(token);
            }
            else
            {
                throw new InvalidInputException(TOKEN_NOT_NULL, globalMessageSource.get(TOKEN_NOT_NULL, tokenUtils.getLoggedInUserId()));
            }
            try
            {
                String response;
                if (sortBy == null && sortOrder == null)
                {
                    try
                    {
                        response=webClientWrapper.webclientRequest(webClient,gatewayApi +ELASTIC_VERSION1+PARAM_FILTER+filter+AND_INDEX_NAME+TP_RUNTIME_FORM_DATA_+formId+AND_SOURCE+elasticSource,GET,null);
                        logger.info(response);
                    }
                    catch (Exception e)
                    {
                        assert logger != null;
                        logger.error(e.getMessage());
                        throw new InvalidInputException(e.getMessage(),globalMessageSource.get(e.getMessage()));
                    }
                }
                else
                {
                    try
                    {
                        response=webClientWrapper.webclientRequest(webClient,gatewayApi +ELASTIC_VERSION1+PARAM_SORT_BY+sortBy+AND_SORT_ORDER+sortOrder+AND_FILTER+filter+AND_INDEX_NAME+TP_RUNTIME_FORM_DATA_+formId+AND_SOURCE+elasticSource,GET,null);
                        logger.info(response);
                    }
                    catch (Exception e)
                    {
                        logger.error(e.getMessage());
                        throw new InvalidInputException(e.getMessage(),globalMessageSource.get(e.getMessage()));
                    }
                }
                Map<String,Object> responseMap=this.objectMapper.readValue(response,Map.class);
                Map<String,Object> dataMap=this.objectMapper.convertValue(responseMap.get(DATA),Map.class);
                contentList = this.objectMapper.convertValue(dataMap.get(CONTENT),List.class);
                for (Map<String, Object> contentMap : contentList)
                {
                    Map<String,Object> modifiedFormDataResponse = new LinkedHashMap();
                    modifiedFormDataResponse.put(ID,String.valueOf(contentMap.get(ID)));
                    modifiedFormDataResponse.put(FORM_DATA, contentMap.get(FORM_DATA));
                    modifiedFormDataResponse.put(FORM_META_DATA, contentMap.get(FORM_META_DATA));
                    modifiedFormDataResponse.put(VERSION,String.valueOf(contentMap.get(VERSION)));
                    modifiedFormDataResponse.put(CREATED_BY_ID,String.valueOf(contentMap.get(CREATED_BY_ID)));
                    modifiedFormDataResponse.put(CREATED_ON, contentMap.get(CREATED_ON));
                    modifiedFormDataResponse.put(CREATED_BY_NAME,String.valueOf(contentMap.get(CREATED_BY_NAME)));
                    modifiedFormDataResponse.put(UPDATED_BY_ID,String.valueOf(contentMap.get(UPDATED_BY_ID)));
                    modifiedFormDataResponse.put(UPDATED_ON,contentMap.get(UPDATED_ON));
                    modifiedFormDataResponse.put(UPDATED_BY_NAME,String.valueOf(contentMap.get(UPDATED_BY_NAME)));
                    responseList.add(modifiedFormDataResponse);
                }
            }
            catch (Exception e)
            {
                logger.error(e.getMessage());
                throw new InvalidInputException(e.getMessage(), globalMessageSource.get(e.getMessage()));
            }
            return responseList;
        }
        if (!mongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_+formId))
        {
            throw new FormIdNotFoundException(FORM_DATA_DOES_NOT_EXISTS_WITH_GIVEN_FORMID, globalMessageSource.get(FORM_DATA_DOES_NOT_EXISTS_WITH_GIVEN_FORMID, formId));
        }
        Query query = new Query();
        ArrayList<String> keysList = new ArrayList<>();
        ArrayList<String> valuesList = new ArrayList<>();
        String[] parts = filter.split(COMMA);
        for (String part : parts)
        {
            String[] keyValue = part.split(COLON);
            keysList.add(keyValue[0].replaceAll(REGEX_PATTERN_1, EMPTY_STRING));
            valuesList.add(keyValue[1].replaceAll(REGEX_PATTERN_1, EMPTY_STRING));
        }
        Criteria criteria = new Criteria();
        ArrayList<Criteria> c1 = new ArrayList<>();
        for (int i = 0; i < keysList.size(); i++)
        {
            if(keysList.get(i).equals(ID))
            {
                c1.add(Criteria.where(UNDERSCORE_ID).is(Long.valueOf(valuesList.get(i))));
            }
            else
            {
                String searchString = valuesList.get(i);  //if SearchString contains any alphabets or any special character
                if(searchString.matches(CONTAINS_ATLEAST_ONE_ALPHABET)||searchString.matches(CONTAINS_ATLEAST_ONE_SPECIAL_CHARACTER))
                {
                    c1.add(new Criteria().orOperator(Criteria.where(keysList.get(i)).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE))));
                }
                else
                {
                    c1.add(new Criteria().orOperator(Criteria.where(keysList.get(i)).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                            Criteria.where(keysList.get(i)).is(Long.valueOf(searchString))));
                }
            }
        }
        criteria = criteria.andOperator(c1.toArray(new Criteria[0]));
        if (StringUtils.isNotEmpty(relations))
        {
            ArrayList<String> mappedArrayOfDocumentsName=new ArrayList<>();
            String[] relationsList = relations.split(COMMA);
            ArrayList<String> relationKeysList = new ArrayList<>();
            ArrayList<String> relationValuesList = new ArrayList<>();
            List<Map<String, Object>> relationalMapList = new ArrayList<>();
            List<AggregationOperation> aggregationOperationsList = new ArrayList<>();
            for (String relation : relationsList)
            {
                String[] keyValuePair = relation.split(COLON);
                mappedArrayOfDocumentsName.add(keyValuePair[0]);
                keyValuePair[0] = TP_RUNTIME_FORM_DATA_+keyValuePair[0];
                keyValuePair[1] = FORM_DATA + DOT + keyValuePair[1];
                relationKeysList.add(keyValuePair[0].replaceAll(REGEX_PATTERN_1,EMPTY_STRING));
                relationValuesList.add(keyValuePair[1].replaceAll(REGEX_PATTERN_1,EMPTY_STRING));
            }
            for(int j=0;j<relationKeysList.size();j++)
            {
                DocumentAggregationOperation documentAggregationOperation=new DocumentAggregationOperation(String.format(MONGO_AGGREGATION_STAGE_PIPELINE_1,relationKeysList.get(j),relationValuesList.get(j),relationValuesList.get(j),mappedArrayOfDocumentsName.get(j)));
                aggregationOperationsList.add(documentAggregationOperation);
            }
            aggregationOperationsList.add(Aggregation.match(criteria));
            if (StringUtils.isEmpty(sortBy) && StringUtils.isEmpty(sortOrder))
            {
                aggregationOperationsList.add(Aggregation.sort(Sort.by(Sort.Direction.DESC,CREATED_ON)));
                List<Map> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA_+formId,Map.class).getMappedResults();
                for (Map map : aggregateList)
                {
                    map.put(ID,String.valueOf(map.get(UNDERSCORE_ID)));
                    map.remove(UNDERSCORE_ID);
                    relationalMapList.add(map);
                }
                return relationalMapList;
            }
            aggregationOperationsList.add(Aggregation.sort(Sort.by(Sort.Direction.fromString(sortOrder), sortBy)));
            List<Map> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA_ + formId, Map.class).getMappedResults();
            for (Map map : aggregateList)
            {
                map.put(ID,String.valueOf(map.get(UNDERSCORE_ID)));
                map.remove(UNDERSCORE_ID);
                relationalMapList.add(map);
            }
            return relationalMapList;
        }
        query.addCriteria(criteria);
        if (StringUtils.isEmpty(sortBy) && StringUtils.isEmpty(sortOrder))
        {
            query.with(Sort.by(Sort.Direction.DESC, CREATED_ON));
            List<FormDataDefinition> formDataDefinitionsList = mongoTemplate.find(query, FormDataDefinition.class, TP_RUNTIME_FORM_DATA_ + formId);
            List<FormDataResponseSchema> formDataResponseSchemasList = new ArrayList<>();
            for (FormDataDefinition formDataDefinition : formDataDefinitionsList)
            {
                FormDataResponseSchema formDataResponseSchema = new FormDataResponseSchema(String.valueOf(formDataDefinition.getId()), formDataDefinition.getFormData(),
                        formDataDefinition.getFormMetadata(),String.valueOf(formDataDefinition.getVersion()), String.valueOf(formDataDefinition.getCreatedById()), formDataDefinition.getCreatedOn(), String.valueOf(formDataDefinition.getUpdatedById()), formDataDefinition.getUpdatedOn());
                formDataResponseSchemasList.add(formDataResponseSchema);
            }
            return formDataResponseSchemasList;
        }
        if (StringUtils.isEmpty(sortBy) && !StringUtils.isEmpty(sortOrder) || !StringUtils.isEmpty(sortBy) && StringUtils.isEmpty(sortOrder))
        {
            throw new InvalidInputException(SORTBY_AND_SORTORDER_BOTH_SHOULD_BE_GIVEN, globalMessageSource.get(SORTBY_AND_SORTORDER_BOTH_SHOULD_BE_GIVEN));
        }
        query.with(Sort.by(Sort.Direction.fromString(sortOrder), sortBy));
        List<FormDataDefinition> formDataDefinitionsList = mongoTemplate.find(query, FormDataDefinition.class, TP_RUNTIME_FORM_DATA_ + formId);
        List<FormDataResponseSchema> formDataResponseSchemasList = new ArrayList<>();
        for (FormDataDefinition formDataDefinition : formDataDefinitionsList)
        {
            FormDataResponseSchema formDataResponseSchema = new FormDataResponseSchema(String.valueOf(formDataDefinition.getId()),
                    formDataDefinition.getFormData(), formDataDefinition.getFormMetadata(),String.valueOf(formDataDefinition.getVersion()),
                    String.valueOf(formDataDefinition.getCreatedById()), formDataDefinition.getCreatedOn(),
                    String.valueOf(formDataDefinition.getUpdatedById()), formDataDefinition.getUpdatedOn());
            formDataResponseSchemasList.add(formDataResponseSchema);
        }
        return formDataResponseSchemasList;
    }

    @Override
    public PaginationResponsePayload getAllFormDataByFormId(String formId, String relations, String filter, String sortBy, String sortOrder, Pageable pageable)
    {
        PaginationResponsePayload paginationResponsePayload = new PaginationResponsePayload();
        if (elasticEnable&&relations==null)
        {
            List<Map<String,Object>> contentList;
            List<Map<String,Object>> responseList=new ArrayList<>();
            if (StringUtils.isEmpty(sortBy) && !StringUtils.isEmpty(sortOrder) || !StringUtils.isEmpty(sortBy) && StringUtils.isEmpty(sortOrder))
            {
                throw new InvalidInputException(SORTBY_AND_SORTORDER_BOTH_SHOULD_BE_GIVEN, globalMessageSource.get(SORTBY_AND_SORTORDER_BOTH_SHOULD_BE_GIVEN));
            }
            WebClient webClient;
            String token = tokenUtils.getTokenFromContext();
            if (StringUtils.isNotEmpty(token))
            {
                webClient = webClientWrapper.createWebClient(token);
            }
            else
            {
                throw new InvalidInputException(TOKEN_NOT_NULL, globalMessageSource.get(TOKEN_NOT_NULL, tokenUtils.getLoggedInUserId()));
            }
            try
            {
                String response;
                if (sortBy == null && sortOrder == null)
                {
                    try
                    {
                        response=webClientWrapper.webclientRequest(webClient,gatewayApi +ELASTIC_VERSION1+PARAM_FILTER+filter+AND_PAGE+pageable.getPageNumber()+AND_SIZE+pageable.getPageSize()+AND_INDEX_NAME+TP_RUNTIME_FORM_DATA_+formId+AND_SOURCE+elasticSource,GET,null);
                        logger.info(response);
                    }
                    catch (Exception e)
                    {
                        logger.error(e.getMessage());
                        throw new InvalidInputException(e.getMessage(),globalMessageSource.get(e.getMessage()));
                    }
                }
                else
                {
                    try
                    {
                        response=webClientWrapper.webclientRequest(webClient,gatewayApi +ELASTIC_VERSION1+PARAM_SORT_BY+sortBy+AND_SORT_ORDER+sortOrder+AND_FILTER+filter+AND_PAGE+pageable.getPageNumber()+AND_SIZE+pageable.getPageSize()+AND_INDEX_NAME+TP_RUNTIME_FORM_DATA_+formId+AND_SOURCE+elasticSource,GET,null);
                        logger.info(response);
                    }
                    catch (Exception e)
                    {
                        logger.error(e.getMessage());
                        throw new InvalidInputException(e.getMessage(),globalMessageSource.get(e.getMessage()));
                    }
                }
                Map<String,Object> responseMap=this.objectMapper.readValue(response,Map.class);
                Map<String,Object> dataMap=this.objectMapper.convertValue(responseMap.get(DATA),Map.class);
                contentList = this.objectMapper.convertValue(dataMap.get(CONTENT),List.class);
                for (Map<String, Object> contentMap : contentList)
                {
                    Map<String,Object> modifiedFormDataResponse = new LinkedHashMap();
                    modifiedFormDataResponse.put(ID,String.valueOf(contentMap.get(ID)));
                    modifiedFormDataResponse.put(FORM_DATA,contentMap.get(FORM_DATA));
                    modifiedFormDataResponse.put(FORM_META_DATA,contentMap.get(FORM_META_DATA));
                    modifiedFormDataResponse.put(VERSION, String.valueOf(contentMap.get(VERSION)));
                    modifiedFormDataResponse.put(CREATED_BY_ID,String.valueOf(contentMap.get(CREATED_BY_ID)));
                    modifiedFormDataResponse.put(CREATED_ON, contentMap.get(CREATED_ON));
                    modifiedFormDataResponse.put(CREATED_BY_NAME,String.valueOf(contentMap.get(CREATED_BY_NAME)));
                    modifiedFormDataResponse.put(UPDATED_BY_ID,String.valueOf(contentMap.get(UPDATED_BY_ID)));
                    modifiedFormDataResponse.put(UPDATED_ON, contentMap.get(UPDATED_ON));
                    modifiedFormDataResponse.put(UPDATED_BY_NAME,String.valueOf(contentMap.get(UPDATED_BY_NAME)));
                    responseList.add(modifiedFormDataResponse);
                }
                paginationResponsePayload.setContent(responseList);
                paginationResponsePayload.setPage(Integer.parseInt(String.valueOf(dataMap.get(PAGE))));
                paginationResponsePayload.setSize(Integer.parseInt(String.valueOf(dataMap.get(SIZE))));
                paginationResponsePayload.setTotalPages(Integer.parseInt(String.valueOf(dataMap.get(TOTAL_PAGES))));
                paginationResponsePayload.setTotalElements(Long.parseLong(String.valueOf(dataMap.get(TOTAL_ELEMENTS))));
                paginationResponsePayload.setNumberOfElements(Integer.parseInt(String.valueOf(dataMap.get(NUMBER_OF_ELEMENTS))));
            }
            catch (Exception e)
            {
                logger.error(e.getMessage());
                throw new InvalidInputException(e.getMessage(), globalMessageSource.get(e.getMessage()));
            }
            return paginationResponsePayload;
        }
        if (!mongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ + formId))
        {
            throw new FormIdNotFoundException(FORM_DATA_DOES_NOT_EXISTS_WITH_GIVEN_FORMID, globalMessageSource.get(FORM_DATA_DOES_NOT_EXISTS_WITH_GIVEN_FORMID, formId));
        }
        List<Map<String, Object>> content = new ArrayList<>();
        paginationResponsePayload.setPage(pageable.getPageNumber());
        paginationResponsePayload.setSize(pageable.getPageSize());
        Query query = new Query();
        ArrayList<String> keysList = new ArrayList<>();
        ArrayList<String> valuesList = new ArrayList<>();
        String[] parts = filter.split(COMMA);
        for (String part : parts)
        {
            String[] keyValue = part.split(COLON);
            keysList.add(keyValue[0].replaceAll(REGEX_PATTERN_1, EMPTY_STRING));
            valuesList.add(keyValue[1].replaceAll(REGEX_PATTERN_1, EMPTY_STRING));
        }
        Criteria criteria = new Criteria();
        ArrayList<Criteria> c1 = new ArrayList<>();
        for (int i = 0; i < keysList.size(); i++)
        {
            if(keysList.get(i).equals(ID))
            {
               c1.add(Criteria.where(UNDERSCORE_ID).is(Long.valueOf(valuesList.get(i))));
            }
            else
            {
                String searchString = valuesList.get(i);  //if SearchString contains any alphabets or any special character
                if(searchString.matches(CONTAINS_ATLEAST_ONE_ALPHABET)||searchString.matches(CONTAINS_ATLEAST_ONE_SPECIAL_CHARACTER))
                {
                    c1.add(new Criteria().orOperator(Criteria.where(keysList.get(i)).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE))));
                }
                else
                {
                    c1.add(new Criteria().orOperator(Criteria.where(keysList.get(i)).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                            Criteria.where(keysList.get(i)).is(Long.valueOf(searchString))));
                }
            }
        }
        criteria = criteria.andOperator(c1.toArray(new Criteria[0]));
        if (StringUtils.isNotEmpty(relations))
        {
            ArrayList<String> mappedArrayOfDocumentsName=new ArrayList<>();
            List<AggregationOperation> aggregationOperationsList = new ArrayList<>();
            String[] relationsList = relations.split(COMMA);
            ArrayList<String> relationKeysList = new ArrayList<>();
            ArrayList<String> relationValuesList = new ArrayList<>();
            for (String relation : relationsList)
            {
                String[] keyValuePair = relation.split(COLON);
                mappedArrayOfDocumentsName.add(keyValuePair[0]);
                keyValuePair[0] = TP_RUNTIME_FORM_DATA_ + keyValuePair[0];
                keyValuePair[1] = FORM_DATA + DOT + keyValuePair[1];
                relationKeysList.add(keyValuePair[0].replaceAll(REGEX_PATTERN_1, EMPTY_STRING));
                relationValuesList.add(keyValuePair[1].replaceAll(REGEX_PATTERN_1, EMPTY_STRING));
            }
            aggregationOperationsList.add(Aggregation.match(criteria));
            for(int j=0;j<relationKeysList.size();j++)
            {
                DocumentAggregationOperation documentAggregationOperation=new DocumentAggregationOperation(String.format(MONGO_AGGREGATION_STAGE_PIPELINE_2,relationKeysList.get(j),relationValuesList.get(j),relationValuesList.get(j),mappedArrayOfDocumentsName.get(j)));
                aggregationOperationsList.add(documentAggregationOperation);
            }
            if (StringUtils.isEmpty(sortBy) && StringUtils.isEmpty(sortOrder))
            {
                FacetOperation facetOperation=Aggregation.facet(Aggregation.count().as(COUNT)).as(METADATA).and(Aggregation.sort(Sort.by(Sort.Direction.DESC, CREATED_ON)),
                        Aggregation.skip(pageable.getOffset()),Aggregation.limit(pageable.getPageSize())).as(DATA);
                aggregationOperationsList.add(facetOperation);
                List<Map> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList),TP_RUNTIME_FORM_DATA_+formId,Map.class).getMappedResults();
                Map<String,Object> dataMap=aggregateList.get(0);
                List<Map<String,Object>> metaDataList= (List<Map<String, Object>>) dataMap.get(METADATA);
                List<Map<String,Object>> dataList= (List<Map<String,Object>>) dataMap.get(DATA);
                for (Map map : dataList)
                {
                    map.put(ID,String.valueOf(map.get(UNDERSCORE_ID)));
                    map.remove(UNDERSCORE_ID);
                    content.add(map);
                }
                Map<String,Object> metaData = null;
                if(!metaDataList.isEmpty())
                {
                    metaData=metaDataList.get(0);
                }
                long totalMatchedRecords = 0L;
                if(metaData!=null)
                {
                    totalMatchedRecords= Long.parseLong(String.valueOf(metaData.get(COUNT)));
                }
                int totalPages = (int) Math.ceil((float) (totalMatchedRecords) / pageable.getPageSize());
                paginationResponsePayload.setContent(content);
                paginationResponsePayload.setTotalPages(totalPages);
                paginationResponsePayload.setTotalElements(totalMatchedRecords);
                paginationResponsePayload.setNumberOfElements(content.size());
                return paginationResponsePayload;
            }
            FacetOperation facetOperation=Aggregation.facet(Aggregation.count().as(COUNT)).as(METADATA).and(Aggregation.sort(Sort.by(Sort.Direction.fromString(sortOrder), sortBy)),
                    Aggregation.skip(pageable.getOffset()),Aggregation.limit(pageable.getPageSize())).as(DATA);
            aggregationOperationsList.add(facetOperation);
            List<Map> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA_+formId, Map.class).getMappedResults();
            Map<String,Object> dataMap=aggregateList.get(0);
            List<Map<String,Object>> metaDataList= (List<Map<String, Object>>) dataMap.get(METADATA);
            List<Map<String,Object>> dataList= (List<Map<String,Object>>) dataMap.get(DATA);
            for (Map map : dataList)
            {
                map.put(ID,String.valueOf(map.get(UNDERSCORE_ID)));
                map.remove(UNDERSCORE_ID);
                content.add(map);
            }
            Map<String,Object> metaData = null;
            if(!metaDataList.isEmpty())
            {
                metaData=metaDataList.get(0);
            }
            long totalMatchedRecords = 0L;
            if(metaData!=null)
            {
                totalMatchedRecords= Long.parseLong(String.valueOf(metaData.get(COUNT)));
            }
            int totalPages = (int) Math.ceil((float) (totalMatchedRecords) / pageable.getPageSize());
            paginationResponsePayload.setContent(content);
            paginationResponsePayload.setTotalPages(totalPages);
            paginationResponsePayload.setTotalElements(totalMatchedRecords);
            paginationResponsePayload.setNumberOfElements(content.size());
            return paginationResponsePayload;
        }
        if (StringUtils.isEmpty(sortBy) && StringUtils.isEmpty(sortOrder))
        {
            query.addCriteria(criteria);
            query.with(Sort.by(Sort.Direction.DESC, CREATED_ON));
            long totalMatchedRecords=mongoTemplate.count(query,TP_RUNTIME_FORM_DATA_+formId);
            query.with(pageable);
            List<FormDataDefinition> formDataDefinitionsList = mongoTemplate.find(query, FormDataDefinition.class, TP_RUNTIME_FORM_DATA_+formId);
            int totalPages = (int) Math.ceil((float) (totalMatchedRecords) / pageable.getPageSize());
            for (FormDataDefinition formDataDefinition : formDataDefinitionsList)
            {
                Map<String, Object> singleDocumentContent = new LinkedHashMap<>();
                singleDocumentContent.put(ID, String.valueOf(formDataDefinition.getId()));
                singleDocumentContent.put(FORM_DATA, formDataDefinition.getFormData());
                singleDocumentContent.put(FORM_META_DATA, formDataDefinition.getFormMetadata());
                singleDocumentContent.put(VERSION, String.valueOf(formDataDefinition.getVersion()));
                singleDocumentContent.put(CREATED_BY_ID, String.valueOf(formDataDefinition.getCreatedById()));
                singleDocumentContent.put(CREATED_ON, formDataDefinition.getCreatedOn());
                singleDocumentContent.put(UPDATED_BY_ID, String.valueOf(formDataDefinition.getUpdatedById()));
                singleDocumentContent.put(UPDATED_ON, formDataDefinition.getUpdatedOn());
                content.add(singleDocumentContent);
            }
            paginationResponsePayload.setContent(content);
            paginationResponsePayload.setTotalPages(totalPages);
            paginationResponsePayload.setTotalElements(totalMatchedRecords);
            paginationResponsePayload.setNumberOfElements(content.size());
            return paginationResponsePayload;
        }
        if (StringUtils.isEmpty(sortBy) && !StringUtils.isEmpty(sortOrder) || !StringUtils.isEmpty(sortBy) && StringUtils.isEmpty(sortOrder))
        {
            throw new InvalidInputException(SORTBY_AND_SORTORDER_BOTH_SHOULD_BE_GIVEN,globalMessageSource.get(SORTBY_AND_SORTORDER_BOTH_SHOULD_BE_GIVEN));
        }
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.fromString(sortOrder),sortBy));
        long totalMatchedRecords=mongoTemplate.count(query,TP_RUNTIME_FORM_DATA_+formId);
        query.with(pageable);
        List<FormDataDefinition> formDataDefinitionsList=mongoTemplate.find(query,FormDataDefinition.class,TP_RUNTIME_FORM_DATA_+formId);
        int totalPages = (int) Math.ceil((float)(totalMatchedRecords) / pageable.getPageSize());
        for(FormDataDefinition formDataDefinition:formDataDefinitionsList)
        {
            Map<String,Object> singleDocumentContent =new LinkedHashMap<>();
            singleDocumentContent.put(ID, String.valueOf(formDataDefinition.getId()));
            singleDocumentContent.put(FORM_DATA, formDataDefinition.getFormData());
            singleDocumentContent.put(FORM_META_DATA,formDataDefinition.getFormMetadata());
            singleDocumentContent.put(VERSION, String.valueOf(formDataDefinition.getVersion()));
            singleDocumentContent.put(CREATED_BY_ID,String.valueOf(formDataDefinition.getCreatedById()));
            singleDocumentContent.put(CREATED_ON,formDataDefinition.getCreatedOn());
            singleDocumentContent.put(UPDATED_BY_ID,String.valueOf(formDataDefinition.getUpdatedById()));
            singleDocumentContent.put(UPDATED_ON,formDataDefinition.getUpdatedOn());
            content.add(singleDocumentContent);
        }
        paginationResponsePayload.setContent(content);
        paginationResponsePayload.setTotalPages(totalPages);
        paginationResponsePayload.setTotalElements(totalMatchedRecords);
        paginationResponsePayload.setNumberOfElements(content.size());
        return paginationResponsePayload;
    }

    public List getAllFormDataByFormIdAndQ(String formId, String relations, String q, String sortBy, String sortOrder)
    {
        if (elasticEnable&&relations==null)
        {
            List<Map<String,Object>> contentList;
            List<Map<String,Object>> responseList=new ArrayList<>();
            if (StringUtils.isEmpty(sortBy) && !StringUtils.isEmpty(sortOrder) || !StringUtils.isEmpty(sortBy) && StringUtils.isEmpty(sortOrder))
            {
                throw new InvalidInputException(SORTBY_AND_SORTORDER_BOTH_SHOULD_BE_GIVEN, globalMessageSource.get(SORTBY_AND_SORTORDER_BOTH_SHOULD_BE_GIVEN));
            }
            WebClient webClient;
            String token = tokenUtils.getTokenFromContext();
            if (StringUtils.isNotEmpty(token))
            {
                webClient = webClientWrapper.createWebClient(token);
            }
            else
            {
                throw new InvalidInputException(TOKEN_NOT_NULL, globalMessageSource.get(TOKEN_NOT_NULL, tokenUtils.getLoggedInUserId()));
            }
            try
            {
                String response = null;
                if (StringUtils.isEmpty(q) && StringUtils.isBlank(sortBy) && StringUtils.isBlank(sortOrder))
                {
                    try
                    {
                        response = webClientWrapper.webclientRequest(webClient, gatewayApi + ELASTIC_VERSION1 + PARAM_INDEX_NAME + TP_RUNTIME_FORM_DATA_ + formId + AND_SOURCE + elasticSource, GET, null);
                        logger.info(response);
                    }
                    catch (Exception e)
                    {
                        logger.error(e.getMessage());
                        throw new InvalidInputException(e.getMessage(),globalMessageSource.get(e.getMessage()));
                    }
                }
                if (StringUtils.isEmpty(q) && StringUtils.isNotBlank(sortBy) && StringUtils.isNotBlank(sortOrder))
                {
                    try
                    {
                        response = webClientWrapper.webclientRequest(webClient, gatewayApi + ELASTIC_VERSION1 + PARAM_SORT_BY + sortBy + AND_SORT_ORDER + sortOrder + AND_INDEX_NAME + TP_RUNTIME_FORM_DATA_ + formId + AND_SOURCE + elasticSource, GET, null);
                    }
                    catch (Exception e)
                    {
                        logger.error(e.getMessage());
                        throw new InvalidInputException(e.getMessage(),globalMessageSource.get(e.getMessage()));
                    }
                }
                if (StringUtils.isNotEmpty(q) && StringUtils.isBlank(sortBy) && StringUtils.isBlank(sortOrder))
                {
                    try
                    {
                        response = webClientWrapper.webclientRequest(webClient, gatewayApi + ELASTIC_VERSION1 + PARAM_Q + q + AND_INDEX_NAME + TP_RUNTIME_FORM_DATA_ + formId + AND_SOURCE + elasticSource, GET, null);
                        logger.info(response);
                    }
                    catch (Exception e)
                    {
                        logger.error(e.getMessage());
                        throw new InvalidInputException(e.getMessage(),globalMessageSource.get(e.getMessage()));
                    }
                }
                if (StringUtils.isNotEmpty(q) && StringUtils.isNotBlank(sortBy) && StringUtils.isNotBlank(sortOrder))
                {
                    try
                    {
                        response = webClientWrapper.webclientRequest(webClient, gatewayApi + ELASTIC_VERSION1 + PARAM_SORT_BY + sortBy + AND_SORT_ORDER + sortOrder + AND_Q + q + AND_INDEX_NAME + TP_RUNTIME_FORM_DATA_ + formId + AND_SOURCE + elasticSource, GET, null);
                    }
                    catch (Exception e)
                    {
                        logger.error(e.getMessage());
                        throw new InvalidInputException(e.getMessage(),globalMessageSource.get(e.getMessage()));
                    }
                }
                Map<String, Object> responseMap = this.objectMapper.readValue(response, Map.class);
                Map<String, Object> dataMap = this.objectMapper.convertValue(responseMap.get(DATA), Map.class);
                contentList = this.objectMapper.convertValue(dataMap.get(CONTENT),List.class);
                for (Map<String, Object> contentMap : contentList)
                {
                    Map<String,Object> modifiedFormDataResponse = new LinkedHashMap();
                    modifiedFormDataResponse.put(ID,String.valueOf(contentMap.get(ID)));
                    modifiedFormDataResponse.put(FORM_DATA, contentMap.get(FORM_DATA));
                    modifiedFormDataResponse.put(FORM_META_DATA, contentMap.get(FORM_META_DATA));
                    modifiedFormDataResponse.put(VERSION,String.valueOf(contentMap.get(VERSION)));
                    modifiedFormDataResponse.put(CREATED_BY_ID,String.valueOf(contentMap.get(CREATED_BY_ID)));
                    modifiedFormDataResponse.put(CREATED_ON,contentMap.get(CREATED_ON));
                    modifiedFormDataResponse.put(CREATED_BY_NAME,String.valueOf(contentMap.get(CREATED_BY_NAME)));
                    modifiedFormDataResponse.put(UPDATED_BY_ID,String.valueOf(contentMap.get(UPDATED_BY_ID)));
                    modifiedFormDataResponse.put(UPDATED_ON, contentMap.get(UPDATED_ON));
                    modifiedFormDataResponse.put(UPDATED_BY_NAME,String.valueOf(contentMap.get(UPDATED_BY_NAME)));
                    responseList.add(modifiedFormDataResponse);
                }
            }
            catch (Exception e)
            {
                logger.error(e.getMessage());
                throw new InvalidInputException(e.getMessage(), globalMessageSource.get(e.getMessage()));
            }
            return responseList;
        }
        if (!mongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_+formId))
        {
            throw new FormIdNotFoundException(FORM_DATA_DOES_NOT_EXISTS_WITH_GIVEN_FORMID, globalMessageSource.get(FORM_DATA_DOES_NOT_EXISTS_WITH_GIVEN_FORMID, formId));
        }
        if (StringUtils.isNotEmpty(relations))
        {
            ArrayList<String> mappedArrayOfDocumentsName=new ArrayList<>();
            String[] relationsList = relations.split(COMMA);
            ArrayList<String> relationKeysList = new ArrayList<>();
            ArrayList<String> relationValuesList = new ArrayList<>();
            for (String relation : relationsList)
            {
                String[] keyValuePair = relation.split(COLON);
                mappedArrayOfDocumentsName.add(keyValuePair[0]);
                keyValuePair[0] = TP_RUNTIME_FORM_DATA_ + keyValuePair[0];
                keyValuePair[1] = FORM_DATA + DOT + keyValuePair[1];
                relationKeysList.add(keyValuePair[0].replaceAll(REGEX_PATTERN_1, EMPTY_STRING));
                relationValuesList.add(keyValuePair[1].replaceAll(REGEX_PATTERN_1, EMPTY_STRING));
            }
            List<Map<String, Object>> relationalMapList = new ArrayList<>();
            List<AggregationOperation> aggregationOperationsList = new ArrayList<>();
            for(int j=0;j<relationKeysList.size();j++)
            {
                DocumentAggregationOperation documentAggregationOperation=new DocumentAggregationOperation(String.format(MONGO_AGGREGATION_STAGE_PIPELINE_3,relationKeysList.get(j),relationValuesList.get(j),relationValuesList.get(j),mappedArrayOfDocumentsName.get(j)));
                aggregationOperationsList.add(documentAggregationOperation);
            }
            if (StringUtils.isEmpty(sortBy) && StringUtils.isEmpty(sortOrder))
            {
                aggregationOperationsList.add(Aggregation.sort(Sort.by(Sort.Direction.DESC,CREATED_ON)));
                List<Map> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA_+formId,Map.class).getMappedResults();
                for (Map map : aggregateList)
                {
                    map.put(ID,String.valueOf(map.get(UNDERSCORE_ID)));
                    map.remove(UNDERSCORE_ID);
                    relationalMapList.add(map);
                }
                return relationalMapList;
            }
            aggregationOperationsList.add(Aggregation.sort(Sort.by(Sort.Direction.fromString(sortOrder), sortBy)));
            List<Map> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA_ + formId, Map.class).getMappedResults();
            for (Map map : aggregateList)
            {
                map.put(ID,String.valueOf(map.get(UNDERSCORE_ID)));
                map.remove(UNDERSCORE_ID);
                relationalMapList.add(map);
            }
            return relationalMapList;
        }
        Query query = new Query();
        String searchString;
        if(q!=null)
        {
             searchString= URLDecoder.decode(q,StandardCharsets.UTF_8);
        }
        else
        {
            throw new InvalidInputException(FILTER_SHOULD_BE_GIVEN_ALONG_WITH_SORT_BY_SORT_ORDER,globalMessageSource.get(FILTER_SHOULD_BE_GIVEN_ALONG_WITH_SORT_BY_SORT_ORDER));
        }
        List<FormDataResponseSchema> formDataResponseSchemasList = new ArrayList<>();
        if (StringUtils.isEmpty(sortBy) && StringUtils.isEmpty(sortOrder))
        {
            List<FormDataDefinition> formDataDefinitionsList;
            query.addCriteria(new Criteria().orOperator(Criteria.where(UNDERSCORE_ID).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                    Criteria.where(VERSION).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                    Criteria.where(CREATED_ON).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                    Criteria.where(CREATED_BY_ID).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                    Criteria.where(CREATED_BY_NAME).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                    Criteria.where(UPDATED_ON).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                    Criteria.where(UPDATED_BY_ID).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                    Criteria.where(UPDATED_BY_NAME).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE))));
            query.with(Sort.by(Sort.Direction.DESC, CREATED_ON));
            formDataDefinitionsList = mongoTemplate.find(query, FormDataDefinition.class, TP_RUNTIME_FORM_DATA_ + formId);
            for (FormDataDefinition formDataDefinition : formDataDefinitionsList)
            {
                FormDataResponseSchema formDataResponseSchema =
                        new FormDataResponseSchema(String.valueOf(formDataDefinition.getId()),
                                formDataDefinition.getFormData(), formDataDefinition.getFormMetadata(),String.valueOf(formDataDefinition.getVersion()),String.valueOf(formDataDefinition.getCreatedById()),
                                formDataDefinition.getCreatedOn(), String.valueOf(formDataDefinition.getUpdatedById()), formDataDefinition.getUpdatedOn());
                formDataResponseSchemasList.add(formDataResponseSchema);
            }
            return formDataResponseSchemasList;
        }
        if (StringUtils.isEmpty(sortBy) && !StringUtils.isEmpty(sortOrder) || !StringUtils.isEmpty(sortBy) && StringUtils.isEmpty(sortOrder))
        {
            throw new InvalidInputException(SORTBY_AND_SORTORDER_BOTH_SHOULD_BE_GIVEN, globalMessageSource.get(SORTBY_AND_SORTORDER_BOTH_SHOULD_BE_GIVEN));
        }
        List<FormDataDefinition> formDataDefinitionsList;
        query.addCriteria(new Criteria().orOperator(Criteria.where(UNDERSCORE_ID).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                Criteria.where(VERSION).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                Criteria.where(CREATED_ON).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                Criteria.where(CREATED_BY_ID).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                Criteria.where(CREATED_BY_NAME).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                Criteria.where(UPDATED_ON).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                Criteria.where(UPDATED_BY_ID).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                Criteria.where(UPDATED_BY_NAME).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE))));
        query.with(Sort.by(Sort.Direction.fromString(sortOrder), sortBy));
        formDataDefinitionsList = mongoTemplate.find(query, FormDataDefinition.class, TP_RUNTIME_FORM_DATA_ + formId);
        for (FormDataDefinition formDataDefinition : formDataDefinitionsList)
        {
            FormDataResponseSchema formDataResponseSchema =
                    new FormDataResponseSchema(String.valueOf(formDataDefinition.getId()), formDataDefinition.getFormData(),
                            formDataDefinition.getFormMetadata(),String.valueOf(formDataDefinition.getVersion()), String.valueOf(formDataDefinition.getCreatedById()), formDataDefinition.getCreatedOn(),
                            String.valueOf(formDataDefinition.getUpdatedById()), formDataDefinition.getUpdatedOn());
            formDataResponseSchemasList.add(formDataResponseSchema);
        }
        return formDataResponseSchemasList;
    }

    @Override
    public PaginationResponsePayload getAllFormDataByFormIdAndQ(String formId, String relations, String q, String sortBy, String sortOrder, Pageable pageable)
    {
        PaginationResponsePayload paginationResponsePayload = new PaginationResponsePayload();
        if (elasticEnable&&relations==null)
        {
            List<Map<String,Object>> contentList;
            List<Map<String,Object>> responseList=new ArrayList<>();
            if (StringUtils.isEmpty(sortBy) && !StringUtils.isEmpty(sortOrder) || !StringUtils.isEmpty(sortBy) && StringUtils.isEmpty(sortOrder))
            {
                throw new InvalidInputException(FILTER_SHOULD_BE_GIVEN_ALONG_WITH_SORT_BY_SORT_ORDER_PAGINATION, globalMessageSource.get(FILTER_SHOULD_BE_GIVEN_ALONG_WITH_SORT_BY_SORT_ORDER_PAGINATION));
            }
            WebClient webClient;
            String token = tokenUtils.getTokenFromContext();
            if (StringUtils.isNotEmpty(token))
            {
                webClient = webClientWrapper.createWebClient(token);
            }
            else
            {
                throw new InvalidInputException(TOKEN_NOT_NULL, globalMessageSource.get(TOKEN_NOT_NULL, tokenUtils.getLoggedInUserId()));
            }
            try
            {
                String response = null;
                if (StringUtils.isEmpty(q) && StringUtils.isBlank(sortBy) && StringUtils.isBlank(sortOrder))
                {
                    try
                    {
                        response=webClientWrapper.webclientRequest(webClient,gatewayApi +ELASTIC_VERSION1+PARAM_PAGE+pageable.getPageNumber()+AND_SIZE+pageable.getPageSize()+AND_INDEX_NAME+TP_RUNTIME_FORM_DATA_+formId+AND_SOURCE+elasticSource,GET,null);
                        logger.info(response);
                    }
                    catch (Exception e)
                    {
                        logger.error(e.getMessage());
                        throw new InvalidInputException(e.getMessage(),globalMessageSource.get(e.getMessage()));
                    }
                }
                if(StringUtils.isEmpty(q)&&StringUtils.isNotBlank(sortBy)&&StringUtils.isNotBlank(sortOrder))
                {
                    try
                    {
                        response=webClientWrapper.webclientRequest(webClient,gatewayApi +ELASTIC_VERSION1+PARAM_SORT_BY+sortBy+AND_SORT_ORDER+sortOrder+AND_PAGE+pageable.getPageNumber()+AND_SIZE+pageable.getPageSize()+AND_INDEX_NAME+TP_RUNTIME_FORM_DATA_+formId+AND_SOURCE+elasticSource,GET,null);
                    }
                    catch (Exception e)
                    {
                        logger.error(e.getMessage());
                        throw new InvalidInputException(e.getMessage(),globalMessageSource.get(e.getMessage()));
                    }
                }
                if(StringUtils.isNotEmpty(q)&&StringUtils.isBlank(sortBy)&&StringUtils.isBlank(sortOrder))
                {
                    try
                    {
                        response=webClientWrapper.webclientRequest(webClient,gatewayApi +ELASTIC_VERSION1+PARAM_Q+q+AND_PAGE+pageable.getPageNumber()+AND_SIZE+pageable.getPageSize()+AND_INDEX_NAME+TP_RUNTIME_FORM_DATA_+formId+AND_SOURCE+elasticSource,GET,null);
                        logger.info(response);
                    }
                    catch (Exception e)
                    {
                        logger.error(e.getMessage());
                        throw new InvalidInputException(e.getMessage(),globalMessageSource.get(e.getMessage()));
                    }
                }
                if(StringUtils.isNotEmpty(q)&&StringUtils.isNotBlank(sortBy)&&StringUtils.isNotBlank(sortOrder))
                {
                    try
                    {
                        response=webClientWrapper.webclientRequest(webClient,gatewayApi +ELASTIC_VERSION1+PARAM_SORT_BY+sortBy+AND_SORT_ORDER+sortOrder+AND_Q+q+AND_PAGE+pageable.getPageNumber()+AND_SIZE+pageable.getPageSize()+AND_INDEX_NAME+TP_RUNTIME_FORM_DATA_+formId+AND_SOURCE+elasticSource,GET,null);
                        logger.info(response);
                    }
                    catch (Exception e)
                    {
                        logger.error(e.getMessage());
                        throw new InvalidInputException(e.getMessage(),globalMessageSource.get(e.getMessage()));
                    }
                }
                Map<String,Object> responseMap=this.objectMapper.readValue(response,Map.class);
                Map<String,Object> dataMap=this.objectMapper.convertValue(responseMap.get(DATA),Map.class);
                contentList = this.objectMapper.convertValue(dataMap.get(CONTENT),List.class);
                for (Map<String, Object> contentMap : contentList)
                {
                    Map<String,Object> modifiedFormDataResponse = new LinkedHashMap();
                    modifiedFormDataResponse.put(ID,String.valueOf(contentMap.get(ID)));
                    modifiedFormDataResponse.put(FORM_DATA,contentMap.get(FORM_DATA));
                    modifiedFormDataResponse.put(FORM_META_DATA, contentMap.get(FORM_META_DATA));
                    modifiedFormDataResponse.put(VERSION,String.valueOf(contentMap.get(VERSION)));
                    modifiedFormDataResponse.put(CREATED_BY_ID,String.valueOf(contentMap.get(CREATED_BY_ID)));
                    modifiedFormDataResponse.put(CREATED_ON,contentMap.get(CREATED_ON));
                    modifiedFormDataResponse.put(CREATED_BY_NAME,String.valueOf(contentMap.get(CREATED_BY_NAME)));
                    modifiedFormDataResponse.put(UPDATED_BY_ID,String.valueOf(contentMap.get(UPDATED_BY_ID)));
                    modifiedFormDataResponse.put(UPDATED_ON,contentMap.get(UPDATED_ON));
                    modifiedFormDataResponse.put(UPDATED_BY_NAME,String.valueOf(contentMap.get(UPDATED_BY_NAME)));
                    responseList.add(modifiedFormDataResponse);
                }
                paginationResponsePayload.setContent(responseList);
                paginationResponsePayload.setPage(Integer.parseInt(String.valueOf(dataMap.get(PAGE))));
                paginationResponsePayload.setSize(Integer.parseInt(String.valueOf(dataMap.get(SIZE))));
                paginationResponsePayload.setTotalPages(Integer.parseInt(String.valueOf(dataMap.get(TOTAL_PAGES))));
                paginationResponsePayload.setTotalElements(Long.parseLong(String.valueOf(dataMap.get(TOTAL_ELEMENTS))));
                paginationResponsePayload.setNumberOfElements(Integer.parseInt(String.valueOf(dataMap.get(NUMBER_OF_ELEMENTS))));
            }
            catch (Exception e)
            {
                logger.error(e.getMessage());
                throw new InvalidInputException(e.getMessage(), globalMessageSource.get(e.getMessage(), formId));
            }
            return paginationResponsePayload;
        }
        if (!mongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ + formId))
        {
            throw new FormIdNotFoundException(FORM_DATA_DOES_NOT_EXISTS_WITH_GIVEN_FORMID, globalMessageSource.get(FORM_DATA_DOES_NOT_EXISTS_WITH_GIVEN_FORMID, formId));
        }
        paginationResponsePayload.setPage(pageable.getPageNumber());
        paginationResponsePayload.setSize(pageable.getPageSize());
        List<Map<String, Object>> content = new ArrayList<>();
        if (StringUtils.isNotEmpty(relations))
        {
            ArrayList<String> mappedArrayOfDocumentsName=new ArrayList<>();
            String[] relationsList = relations.split(COMMA);
            ArrayList<String> relationKeysList = new ArrayList<>();
            ArrayList<String> relationValuesList = new ArrayList<>();
            for (String relation : relationsList)
            {
                String[] keyValuePair = relation.split(COLON);
                mappedArrayOfDocumentsName.add(keyValuePair[0]);
                keyValuePair[0] = TP_RUNTIME_FORM_DATA_ + keyValuePair[0];
                keyValuePair[1] = FORM_DATA + DOT + keyValuePair[1];
                relationKeysList.add(keyValuePair[0].replaceAll(REGEX_PATTERN_1, EMPTY_STRING));
                relationValuesList.add(keyValuePair[1].replaceAll(REGEX_PATTERN_1, EMPTY_STRING));
            }
            List<AggregationOperation> aggregationOperationsList = new ArrayList<>();
            for(int j=0;j<relationKeysList.size();j++)
            {
                DocumentAggregationOperation documentAggregationOperation=new DocumentAggregationOperation(String.format(MONGO_AGGREGATION_STAGE_PIPELINE_4,relationKeysList.get(j),relationValuesList.get(j),relationValuesList.get(j),mappedArrayOfDocumentsName.get(j)));
                aggregationOperationsList.add(documentAggregationOperation);
            }
            if (StringUtils.isEmpty(sortBy) && StringUtils.isEmpty(sortOrder))
            {
                FacetOperation facetOperation=Aggregation.facet(Aggregation.count().as(COUNT)).as(METADATA).and(Aggregation.sort(Sort.by(Sort.Direction.DESC, CREATED_ON)),
                        Aggregation.skip(pageable.getOffset()),Aggregation.limit(pageable.getPageSize())).as(DATA);
                aggregationOperationsList.add(facetOperation);
                List<Map> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA_ + formId, Map.class).getMappedResults();
                Map<String,Object> dataMap=aggregateList.get(0);
                List<Map<String,Object>> metaDataList= (List<Map<String, Object>>) dataMap.get(METADATA);
                List<Map<String,Object>> dataList= (List<Map<String,Object>>) dataMap.get(DATA);
                for (Map map : dataList)
                {
                    map.put(ID,String.valueOf(map.get(UNDERSCORE_ID)));
                    map.remove(UNDERSCORE_ID);
                    content.add(map);
                }
                Map<String,Object> metaData = null;
                if(!metaDataList.isEmpty())
                {
                    metaData=metaDataList.get(0);
                }
                long totalMatchedRecords = 0L;
                if(metaData!=null)
                {
                    totalMatchedRecords= Long.parseLong(String.valueOf(metaData.get(COUNT)));
                }
                int totalPages = (int) Math.ceil((float)(totalMatchedRecords) / pageable.getPageSize());
                paginationResponsePayload.setContent(content);
                paginationResponsePayload.setTotalPages(totalPages);
                paginationResponsePayload.setTotalElements(totalMatchedRecords);
                paginationResponsePayload.setNumberOfElements(content.size());
                return paginationResponsePayload;
            }
            FacetOperation facetOperation=Aggregation.facet(Aggregation.count().as(COUNT)).as(METADATA).and(Aggregation.sort(Sort.by(Sort.Direction.fromString(sortOrder), sortBy)),
                    Aggregation.skip(pageable.getOffset()),Aggregation.limit(pageable.getPageSize())).as(DATA);
            aggregationOperationsList.add(facetOperation);
            List<Map> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA_ + formId, Map.class).getMappedResults();
            Map<String,Object> dataMap=aggregateList.get(0);
            List<Map<String,Object>> metaDataList= (List<Map<String, Object>>) dataMap.get(METADATA);
            List<Map<String,Object>> dataList= (List<Map<String,Object>>) dataMap.get(DATA);
            for (Map map : dataList)
            {
                map.put(ID,String.valueOf(map.get(UNDERSCORE_ID)));
                map.remove(UNDERSCORE_ID);
                content.add(map);
            }
            Map<String,Object> metaData = null;
            if(!metaDataList.isEmpty())
            {
                metaData=metaDataList.get(0);
            }
            long totalMatchedRecords = 0L;
            if(metaData!=null)
            {
                totalMatchedRecords= Long.parseLong(String.valueOf(metaData.get(COUNT)));
            }
            int totalPages = (int) Math.ceil((float) (totalMatchedRecords) / pageable.getPageSize());
            paginationResponsePayload.setContent(content);
            paginationResponsePayload.setTotalPages(totalPages);
            paginationResponsePayload.setTotalElements(totalMatchedRecords);
            paginationResponsePayload.setNumberOfElements(content.size());
            return paginationResponsePayload;
        }
        Query query = new Query();
        String searchString;
        if(q!=null)
        {
            searchString= URLDecoder.decode(q,StandardCharsets.UTF_8);
        }
        else
        {
            throw new InvalidInputException(FILTER_SHOULD_BE_GIVEN_ALONG_WITH_SORT_BY_SORT_ORDER_PAGINATION,globalMessageSource.get(FILTER_SHOULD_BE_GIVEN_ALONG_WITH_SORT_BY_SORT_ORDER_PAGINATION));
        }
        if (StringUtils.isEmpty(sortBy) && StringUtils.isEmpty(sortOrder))
        {
            query.addCriteria(new Criteria().orOperator(Criteria.where(UNDERSCORE_ID).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                    Criteria.where(VERSION).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                    Criteria.where(CREATED_ON).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                    Criteria.where(CREATED_BY_ID).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                    Criteria.where(CREATED_BY_NAME).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                    Criteria.where(UPDATED_ON).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                    Criteria.where(UPDATED_BY_ID).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                    Criteria.where(UPDATED_BY_NAME).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE))));
            query.with(Sort.by(Sort.Direction.DESC, CREATED_ON));
            long totalMatchedRecords=mongoTemplate.count(query,TP_RUNTIME_FORM_DATA_ + formId);
            query.with(pageable);
            List<FormDataDefinition> formDataDefinitionsList = mongoTemplate.find(query, FormDataDefinition.class, TP_RUNTIME_FORM_DATA_ + formId);
            int totalPages = (int) Math.ceil((float) (totalMatchedRecords) / pageable.getPageSize());
            for (FormDataDefinition formDataDefinition : formDataDefinitionsList)
            {
                Map<String, Object> singleDocumentContent = new LinkedHashMap<>();
                singleDocumentContent.put(ID, String.valueOf(formDataDefinition.getId()));
                singleDocumentContent.put(FORM_DATA, formDataDefinition.getFormData());
                singleDocumentContent.put(FORM_META_DATA, formDataDefinition.getFormMetadata());
                singleDocumentContent.put(VERSION, String.valueOf(formDataDefinition.getVersion()));
                singleDocumentContent.put(CREATED_BY_ID, String.valueOf(formDataDefinition.getCreatedById()));
                singleDocumentContent.put(CREATED_ON, formDataDefinition.getCreatedOn());
                singleDocumentContent.put(UPDATED_BY_ID, String.valueOf(formDataDefinition.getUpdatedById()));
                singleDocumentContent.put(UPDATED_ON, formDataDefinition.getUpdatedOn());
                content.add(singleDocumentContent);
            }
            paginationResponsePayload.setContent(content);
            paginationResponsePayload.setTotalPages(totalPages);
            paginationResponsePayload.setTotalElements(totalMatchedRecords);
            paginationResponsePayload.setNumberOfElements(content.size());
            return paginationResponsePayload;
        }
        if (StringUtils.isEmpty(sortBy) && !StringUtils.isEmpty(sortOrder) || !StringUtils.isEmpty(sortBy) && StringUtils.isEmpty(sortOrder))
        {
            throw new InvalidInputException(SORTBY_AND_SORTORDER_BOTH_SHOULD_BE_GIVEN, globalMessageSource.get(SORTBY_AND_SORTORDER_BOTH_SHOULD_BE_GIVEN));
        }
        query.addCriteria(new Criteria().orOperator(Criteria.where(UNDERSCORE_ID).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                Criteria.where(VERSION).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                Criteria.where(CREATED_ON).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                Criteria.where(CREATED_BY_ID).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                Criteria.where(CREATED_BY_NAME).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                Criteria.where(UPDATED_ON).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                Criteria.where(UPDATED_BY_ID).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                Criteria.where(UPDATED_BY_NAME).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE))));
        query.with(Sort.by(Sort.Direction.fromString(sortOrder), sortBy));
        long totalMatchedRecords=mongoTemplate.count(query,TP_RUNTIME_FORM_DATA_ + formId);
        query.with(pageable);
        List<FormDataDefinition> formDataDefinitionsList = mongoTemplate.find(query, FormDataDefinition.class, TP_RUNTIME_FORM_DATA_ + formId);
        int totalPages = (int) Math.ceil((float) (totalMatchedRecords) / pageable.getPageSize());
        for (FormDataDefinition formDataDefinition : formDataDefinitionsList)
        {
            Map<String, Object> singleDocumentContent = new LinkedHashMap<>();
            singleDocumentContent.put(ID, String.valueOf(formDataDefinition.getId()));
            singleDocumentContent.put(FORM_DATA, formDataDefinition.getFormData());
            singleDocumentContent.put(FORM_META_DATA, formDataDefinition.getFormMetadata());
            singleDocumentContent.put(VERSION, String.valueOf(formDataDefinition.getVersion()));
            singleDocumentContent.put(CREATED_BY_ID, String.valueOf(formDataDefinition.getCreatedById()));
            singleDocumentContent.put(CREATED_ON, formDataDefinition.getCreatedOn());
            singleDocumentContent.put(UPDATED_BY_ID, String.valueOf(formDataDefinition.getUpdatedById()));
            singleDocumentContent.put(UPDATED_ON, formDataDefinition.getUpdatedOn());
            content.add(singleDocumentContent);
        }
        paginationResponsePayload.setContent(content);
        paginationResponsePayload.setTotalPages(totalPages);
        paginationResponsePayload.setTotalElements(totalMatchedRecords);
        paginationResponsePayload.setNumberOfElements(content.size());
        return paginationResponsePayload;
    }

    public PaginationResponsePayload getAllFormDataByFormId(String formId,String relations)
    {
        PaginationResponsePayload paginationResponsePayload = new PaginationResponsePayload();
        if (elasticEnable&&relations==null)
        {
            List<Map<String,Object>> contentList;
            List<Map<String,Object>> responseList=new ArrayList<>();
            WebClient webClient;
            String token = tokenUtils.getTokenFromContext();
            if (StringUtils.isNotEmpty(token))
            {
                webClient = webClientWrapper.createWebClient(token);
            }
            else
            {
                throw new InvalidInputException(TOKEN_NOT_NULL, globalMessageSource.get(TOKEN_NOT_NULL, tokenUtils.getLoggedInUserId()));
            }
            try
            {
                String response;
                try
                {
                    response= webClientWrapper.webclientRequest(webClient, gatewayApi + ELASTIC_VERSION1 + PARAM_INDEX_NAME + TP_RUNTIME_FORM_DATA_ + formId + AND_SOURCE + elasticSource + AND_PAGE_AND_SIZE + defaultPageLimit, GET, null);
                    logger.info(response);
                }
                catch (Exception e)
                {
                    logger.error(e.getMessage());
                    throw new InvalidInputException(e.getMessage(),globalMessageSource.get(e.getMessage()));
                }
                Map<String, Object> responseMap = this.objectMapper.readValue(response, Map.class);
                Map<String, Object> dataMap = this.objectMapper.convertValue(responseMap.get(DATA), Map.class);
                contentList = this.objectMapper.convertValue(dataMap.get(CONTENT),List.class);
                for (Map<String, Object> contentMap : contentList)
                {
                    Map<String,Object> modifiedFormDataResponse = new LinkedHashMap();
                    modifiedFormDataResponse.put(ID,String.valueOf(contentMap.get(ID)));
                    modifiedFormDataResponse.put(FORM_DATA, contentMap.get(FORM_DATA));
                    modifiedFormDataResponse.put(FORM_META_DATA, contentMap.get(FORM_META_DATA));
                    modifiedFormDataResponse.put(VERSION, contentMap.get(VERSION));
                    modifiedFormDataResponse.put(CREATED_BY_ID, contentMap.get(CREATED_BY_ID));
                    modifiedFormDataResponse.put(CREATED_ON, contentMap.get(CREATED_ON));
                    modifiedFormDataResponse.put(CREATED_BY_NAME, contentMap.get(CREATED_BY_NAME));
                    modifiedFormDataResponse.put(UPDATED_BY_ID, contentMap.get(UPDATED_BY_ID));
                    modifiedFormDataResponse.put(UPDATED_ON, contentMap.get(UPDATED_ON));
                    modifiedFormDataResponse.put(UPDATED_BY_NAME, contentMap.get(UPDATED_BY_NAME));
                    responseList.add(modifiedFormDataResponse);
                }
                paginationResponsePayload.setContent(responseList);
                paginationResponsePayload.setPage(Integer.parseInt(String.valueOf(dataMap.get(PAGE))));
                paginationResponsePayload.setSize(Integer.parseInt(String.valueOf(dataMap.get(SIZE))));
                paginationResponsePayload.setTotalPages(Integer.parseInt(String.valueOf(dataMap.get(TOTAL_PAGES))));
                paginationResponsePayload.setTotalElements(Long.parseLong(String.valueOf(dataMap.get(TOTAL_ELEMENTS))));
                paginationResponsePayload.setNumberOfElements(Integer.parseInt(String.valueOf(dataMap.get(NUMBER_OF_ELEMENTS))));
            }
            catch (Exception e)
            {
                logger.error(e.getMessage());
                throw new InvalidInputException(e.getMessage(), globalMessageSource.get(e.getMessage()));
            }
            return paginationResponsePayload;
        }
        if (!mongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ +formId))
        {
            throw new FormIdNotFoundException(FORM_DATA_DOES_NOT_EXISTS_WITH_GIVEN_FORMID, globalMessageSource.get(FORM_DATA_DOES_NOT_EXISTS_WITH_GIVEN_FORMID, formId));
        }
        List<Map<String,Object>> content = new ArrayList<>();
        Pageable pageable = PageRequest.of(0, defaultPageLimit);
        paginationResponsePayload.setPage(pageable.getPageNumber());
        paginationResponsePayload.setSize(pageable.getPageSize());
        if (StringUtils.isNotEmpty(relations))
        {
            ArrayList<String> mappedArrayOfDocumentsName=new ArrayList<>();
            String[] relationsList = relations.split(COMMA);
            ArrayList<String> relationKeysList = new ArrayList<>();
            ArrayList<String> relationValuesList = new ArrayList<>();
            for (String relation : relationsList)
            {
                String[] keyValuePair = relation.split(COLON);
                mappedArrayOfDocumentsName.add(keyValuePair[0]);
                keyValuePair[0] = TP_RUNTIME_FORM_DATA_ + keyValuePair[0];
                keyValuePair[1] = FORM_DATA + DOT + keyValuePair[1];
                relationKeysList.add(keyValuePair[0].replaceAll(REGEX_PATTERN_1, EMPTY_STRING));
                relationValuesList.add(keyValuePair[1].replaceAll(REGEX_PATTERN_1, EMPTY_STRING));
            }
            List<AggregationOperation> aggregationOperationsList = new ArrayList<>();
            for(int j=0;j<relationKeysList.size();j++)
            {
                DocumentAggregationOperation documentAggregationOperation=new DocumentAggregationOperation(String.format(MONGO_AGGREGATION_STAGE_PIPELINE_5,relationKeysList.get(j),relationValuesList.get(j),relationValuesList.get(j),mappedArrayOfDocumentsName.get(j)));
                aggregationOperationsList.add(documentAggregationOperation);
            }
            FacetOperation facetOperation=Aggregation.facet(Aggregation.count().as(COUNT)).as(METADATA).and(Aggregation.sort(Sort.by(Sort.Direction.DESC, CREATED_ON)),
                    Aggregation.skip(pageable.getOffset()),Aggregation.limit(pageable.getPageSize())).as(DATA);
            aggregationOperationsList.add(facetOperation);
            List<Map> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA_ + formId, Map.class).getMappedResults();
            Map<String,Object> dataMap=aggregateList.get(0);
            List<Map<String,Object>> metaDataList= (List<Map<String, Object>>) dataMap.get(METADATA);
            List<Map<String,Object>> dataList= (List<Map<String,Object>>) dataMap.get(DATA);
            for (Map map : dataList)
            {
                map.put(ID,String.valueOf(map.get(UNDERSCORE_ID)));
                map.remove(UNDERSCORE_ID);
                content.add(map);
            }
            Map<String,Object> metaData = null;
            if(!metaDataList.isEmpty())
            {
                metaData=metaDataList.get(0);
            }
            long totalMatchedRecords = 0L;
            if(metaData!=null)
            {
                totalMatchedRecords= Long.parseLong(String.valueOf(metaData.get(COUNT)));
            }
            int totalPages = (int) Math.ceil((float) (totalMatchedRecords) / pageable.getPageSize());
            paginationResponsePayload.setContent(content);
            paginationResponsePayload.setTotalPages(totalPages);
            paginationResponsePayload.setTotalElements(totalMatchedRecords);
            paginationResponsePayload.setNumberOfElements(content.size());
            return paginationResponsePayload;
        }
        Query query = new Query();
        List<FormDataDefinition> formDataDefinitionsList;
        query.with(Sort.by(Sort.Direction.DESC, CREATED_ON));
        long totalMatchedRecords=mongoTemplate.count(query,TP_RUNTIME_FORM_DATA_ + formId);
        query.with(pageable);
        formDataDefinitionsList = mongoTemplate.find(query, FormDataDefinition.class, TP_RUNTIME_FORM_DATA_ + formId);
        int totalPages = (int) Math.ceil((float) (totalMatchedRecords) / pageable.getPageSize());
        for (FormDataDefinition formDataDefinition : formDataDefinitionsList)
        {
            Map<String, Object> singleDocumentContent = new LinkedHashMap<>();
            singleDocumentContent.put(ID, String.valueOf(formDataDefinition.getId()));
            singleDocumentContent.put(FORM_DATA, formDataDefinition.getFormData());
            singleDocumentContent.put(FORM_META_DATA, formDataDefinition.getFormMetadata());
            singleDocumentContent.put(VERSION, String.valueOf(formDataDefinition.getVersion()));
            singleDocumentContent.put(CREATED_BY_ID, String.valueOf(formDataDefinition.getCreatedById()));
            singleDocumentContent.put(CREATED_ON, formDataDefinition.getCreatedOn());
            singleDocumentContent.put(UPDATED_BY_ID, String.valueOf(formDataDefinition.getUpdatedById()));
            singleDocumentContent.put(UPDATED_ON, formDataDefinition.getUpdatedOn());
            content.add(singleDocumentContent);
        }
        paginationResponsePayload.setPage(0);
        paginationResponsePayload.setContent(content);
        paginationResponsePayload.setSize(defaultPageLimit);
        paginationResponsePayload.setTotalPages(totalPages);
        paginationResponsePayload.setTotalElements(totalMatchedRecords);
        paginationResponsePayload.setNumberOfElements(content.size());
        return paginationResponsePayload;
    }

    @Override
    public List getFormDataByFormIdAndId(String formId, String id,String relations)
    {
        if (elasticEnable&&relations==null)
        {
            List<Map<String,Object>> responseList=new ArrayList<>();
            WebClient webClient;
            String token = tokenUtils.getTokenFromContext();
            if (StringUtils.isNotEmpty(token))
            {
                webClient = webClientWrapper.createWebClient(token);
            }
            else
            {
                throw new InvalidInputException(TOKEN_NOT_NULL, globalMessageSource.get(TOKEN_NOT_NULL, tokenUtils.getLoggedInUserId()));
            }
            try
            {
                String response;
                try
                {
                    response = webClientWrapper.webclientRequest(webClient, gatewayApi + ELASTIC_VERSION1 + SLASH + id + PARAM_INDEX_NAME + TP_RUNTIME_FORM_DATA_ + formId, GET, null);
                    logger.info(response);
                }
                catch (Exception e)
                {
                    logger.error(e.getMessage());
                    throw new InvalidInputException(e.getMessage(),globalMessageSource.get(e.getMessage()));
                }
                Map<String,Object> responseMap = this.objectMapper.readValue(response,Map.class);
                Map<String,Object> dataMap = this.objectMapper.convertValue(responseMap.get(DATA), Map.class);
                Map<String,Object> modifiedFormDataResponse = new LinkedHashMap();
                modifiedFormDataResponse.put(ID, dataMap.get(ID));
                modifiedFormDataResponse.put(FORM_DATA, dataMap.get(FORM_DATA));
                modifiedFormDataResponse.put(FORM_META_DATA, dataMap.get(FORM_META_DATA));
                modifiedFormDataResponse.put(VERSION, dataMap.get(VERSION));
                modifiedFormDataResponse.put(CREATED_BY_ID, dataMap.get(CREATED_BY_ID));
                modifiedFormDataResponse.put(CREATED_ON, dataMap.get(CREATED_ON));
                modifiedFormDataResponse.put(CREATED_BY_NAME, dataMap.get(CREATED_BY_NAME));
                modifiedFormDataResponse.put(UPDATED_BY_ID, dataMap.get(UPDATED_BY_ID));
                modifiedFormDataResponse.put(UPDATED_ON, dataMap.get(UPDATED_ON));
                modifiedFormDataResponse.put(UPDATED_BY_NAME, dataMap.get(UPDATED_BY_NAME));
                responseList.add(modifiedFormDataResponse);
            }
            catch (Exception e)
            {
                logger.error(e.getMessage());
                throw new InvalidInputException(e.getMessage(), globalMessageSource.get(e.getMessage()));
            }
            return responseList;
        }
        if (StringUtils.isNotEmpty(relations))
        {
            ArrayList<String> mappedArrayOfDocumentsName=new ArrayList<>();
            String[] relationsList = relations.split(COMMA);
            ArrayList<String> relationKeysList = new ArrayList<>();
            ArrayList<String> relationValuesList = new ArrayList<>();
            for (String relation : relationsList)
            {
                String[] keyValuePair = relation.split(COLON);
                mappedArrayOfDocumentsName.add(keyValuePair[0]);
                keyValuePair[0] = TP_RUNTIME_FORM_DATA_ + keyValuePair[0];
                keyValuePair[1] = FORM_DATA + DOT + keyValuePair[1];
                relationKeysList.add(keyValuePair[0].replaceAll(REGEX_PATTERN_1, EMPTY_STRING));
                relationValuesList.add(keyValuePair[1].replaceAll(REGEX_PATTERN_1, EMPTY_STRING));
            }
            List<Map<String, Object>> relationalMapList = new ArrayList<>();
            List<AggregationOperation> aggregationOperationsList = new ArrayList<>();
            aggregationOperationsList.add(Aggregation.match(Criteria.where(UNDERSCORE_ID).is(Long.valueOf(id))));
            for(int j=0;j<relationKeysList.size();j++)
            {
                DocumentAggregationOperation documentAggregationOperation=new DocumentAggregationOperation(String.format(MONGO_AGGREGATION_STAGE_PIPELINE_6,relationKeysList.get(j),relationValuesList.get(j),relationValuesList.get(j),mappedArrayOfDocumentsName.get(j)));
                aggregationOperationsList.add(documentAggregationOperation);
            }
            aggregationOperationsList.add(Aggregation.sort(Sort.by(Sort.Direction.DESC, CREATED_ON)));
            List<Map> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA_ + formId, Map.class).getMappedResults();
            for (Map map : aggregateList)
            {
                map.put(ID,String.valueOf(map.get(UNDERSCORE_ID)));
                map.remove(UNDERSCORE_ID);
                relationalMapList.add(map);
            }
            return relationalMapList;
        }
        boolean documentFlag = false;
        Map<String, Object> formData;
        Map<String, Object> formMetaData;
        FormDataResponseSchema formDataResponseSchema = null;
        MongoCursor<Document> cursor;
        Bson filter = Filters.eq(UNDERSCORE_ID, Long.valueOf(id));
        if (!mongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_+formId))
        {
            throw new FormIdNotFoundException(FORM_DATA_DOES_NOT_EXISTS_WITH_GIVEN_FORMID, globalMessageSource.get(FORM_DATA_DOES_NOT_EXISTS_WITH_GIVEN_FORMID, formId));
        }
        try
        {
            MongoCollection<Document> collection = mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_ + formId);
            FindIterable<Document> documents = collection.find(filter);
            cursor = documents.iterator();
            while (cursor.hasNext())
            {
                Document document = cursor.next();
                documentFlag = true;
                if (!document.isEmpty())
                {
                    formData = objectMapper.convertValue(document.get(FORM_DATA), Map.class);
                    formMetaData = objectMapper.convertValue(document.get(FORM_META_DATA), Map.class);
                    formDataResponseSchema = new FormDataResponseSchema(String.valueOf(document.get(UNDERSCORE_ID)),formData,formMetaData,
                            String.valueOf(document.get(VERSION)),
                            String.valueOf(document.get(CREATED_BY_ID)), ((Date) document.get(CREATED_ON)).toInstant(),
                            String.valueOf(document.get(UPDATED_BY_ID)), ((Date) document.get(UPDATED_ON)).toInstant());
                }
            }
        }
        catch(Exception e)
        {
            logger.error(e.getMessage());
            throw new InvalidInputException(e.getMessage(), globalMessageSource.get(e.getMessage()));
        }
        if(!documentFlag)
        {
            throw new FormIdNotFoundException(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC,globalMessageSource.get(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC,id));
        }
        assert formDataResponseSchema != null;
        return  List.of(formDataResponseSchema);
    }

    @Override
    public void deleteAllFormDataByFormId(String formId)
    {
        boolean flag;
        try
        {
            mongoTemplate.dropCollection(TP_RUNTIME_FORM_DATA_+formId+AUDIT);
            if(!mongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_+formId))
            {
                throw new FormIdNotFoundException(FORM_DATA_DOES_NOT_EXISTS_WITH_GIVEN_FORMID,globalMessageSource.get(FORM_DATA_DOES_NOT_EXISTS_WITH_GIVEN_FORMID,formId));
            }
            mongoTemplate.dropCollection(TP_RUNTIME_FORM_DATA_+formId);
            flag=true;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage());
            throw new InvalidInputException(e.getMessage(), globalMessageSource.get(e.getMessage()));
        }
        if(elasticEnable)
        {
            WebClient webClient;
            String token = tokenUtils.getTokenFromContext();
            if (StringUtils.isNotEmpty(token))
            {
                webClient = webClientWrapper.createWebClient(token);
            }
            else
            {
                throw new InvalidInputException(TOKEN_NOT_NULL,globalMessageSource.get(TOKEN_NOT_NULL,tokenUtils.getLoggedInUserId()));
            }
            try
            {
                if(flag)
                {
                         String response;
                         try
                         {
                             response= webClientWrapper.webclientRequest(webClient, gatewayApi + ELASTIC_VERSION1 + SLASH+PARAM_INDEX_NAME+TP_RUNTIME_FORM_DATA_+formId,DELETE,null);
                             logger.info(response);
                         }
                         catch (Exception e)
                         {
                             logger.error(e.getMessage());
                             throw new InvalidInputException(e.getMessage(),globalMessageSource.get(e.getMessage()));
                         }
                }
                else
                {
                    throw new EntityIdNotFoundException(UNABLE_TO_DELETE_FORM_DATA_IN_ELASTIC_DB,globalMessageSource.get(UNABLE_TO_DELETE_FORM_DATA_IN_ELASTIC_DB));
                }
            }
            catch (Exception e)
            {
                logger.error(e.getMessage());
                throw new InvalidInputException(e.getMessage(),globalMessageSource.get(e.getMessage()));
            }
        }
    }

    @Override
    public void deleteFormDataByFormIdAndId(String formId, String id)
    {
        boolean flag;
        long count;
        Bson filter= Filters.eq(UNDERSCORE_ID,Long.valueOf(id));
        if(!mongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_+formId))
        {
            throw new FormIdNotFoundException(FORM_DATA_DOES_NOT_EXISTS_WITH_GIVEN_FORMID,globalMessageSource.get(FORM_DATA_DOES_NOT_EXISTS_WITH_GIVEN_FORMID,formId));
        }
        try
        {
                DeleteResult deleteResult= mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_+formId).deleteMany(filter);
                count=deleteResult.getDeletedCount();
                filter= Filters.eq(FORM_DATA_ID,Long.valueOf(id));
                mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA_+formId+AUDIT).deleteMany(filter);
                flag=true;
        }
        catch(Exception e)
        {
            logger.error(e.getMessage());
            throw new InvalidInputException(e.getMessage(),globalMessageSource.get(e.getMessage()));
        }
        if(count==0)
        {
            throw new FormIdNotFoundException(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC,globalMessageSource.get(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC,id));
        }
        if(elasticEnable)
        {
            WebClient webClient;
            String token = tokenUtils.getTokenFromContext();
            if (StringUtils.isNotEmpty(token))
            {
                webClient = webClientWrapper.createWebClient(token);
            }
            else
            {
                throw new InvalidInputException(TOKEN_NOT_NULL,globalMessageSource.get(TOKEN_NOT_NULL,tokenUtils.getLoggedInUserId()));
            }
            try
            {
                if(flag)
                {
                    String response;
                    try
                    {
                        response = webClientWrapper.webclientRequest(webClient, gatewayApi + ELASTIC_VERSION1 + SLASH + id + PARAM_INDEX_NAME + TP_RUNTIME_FORM_DATA_ + formId, DELETE, null);
                        logger.info(response);
                    }
                    catch(Exception e)
                    {
                        logger.info(e.getMessage());
                        throw new InvalidInputException(e.getMessage(),globalMessageSource.get(e.getMessage()));
                    }
                }
                else
                {
                    throw new EntityIdNotFoundException(UNABLE_TO_DELETE_FORM_DATA_IN_ELASTIC_DB,globalMessageSource.get(UNABLE_TO_DELETE_FORM_DATA_IN_ELASTIC_DB));
                }
            }
            catch (Exception e)
            {
                logger.error(e.getMessage());
                throw new InvalidInputException(e.getMessage(),globalMessageSource.get(e.getMessage()));
            }
        }
    }

    @Override
    public String validateFormDataByFormId(FormDataSchema formDataSchema)
    {
        String formId=formDataSchema.getFormId();
        Map<String,Object> data=formDataSchema.getFormData();
        FormResponseSchema formResponseSchema= formService.getRuntimeFormById(formId);
        LinkedHashMap<String,LinkedHashMap<String,Object>> fieldsMap = ValidateFormUtils.getSchema(formResponseSchema.getComponents());
        LinkedHashMap<String,Object> modifiedInputData =refineInputData(data,new LinkedHashMap<>());
        assert fieldsMap != null;
        List<String> result= validationCheckServiceImpl.allFieldsValidations(fieldsMap,modifiedInputData,formId,formDataSchema.getId());
        String key = EMPTY_STRING;
        if(result.get(1)!=null)
        {
            key=result.get(1);
        }
        switch (Integer.parseInt(result.get(0)))
        {
            case 0:   throw new InvalidInputException(FORM_DATA_MISSING_MANDATORY_FIELDS,globalMessageSource.get(FORM_DATA_MISSING_MANDATORY_FIELDS,key));
            case 1:   throw new InvalidInputException(FORM_DATA_HAS_DUPLICATE,globalMessageSource.get(FORM_DATA_HAS_DUPLICATE,key));
            case 2:   throw new InvalidInputException(FORM_DATA_MIN_LENGTH_CONDITION_FAILED,globalMessageSource.get(FORM_DATA_MIN_LENGTH_CONDITION_FAILED,key));
            case 3:   throw new InvalidInputException(FORM_DATA_MAX_LENGTH_CONDITION_VIOLATED_BY_USER,globalMessageSource.get(FORM_DATA_MAX_LENGTH_CONDITION_VIOLATED_BY_USER,key));
            case 4:
            case 6:   throw new InvalidInputException(FORM_DATA_INTEGER_FIELDS_CANNOT_CONTAIN_ALPHABETS,globalMessageSource.get(FORM_DATA_INTEGER_FIELDS_CANNOT_CONTAIN_ALPHABETS,key));
            case 5:   throw new InvalidInputException(FORM_DATA_MIN_VALUE_CONDITION_FAILED,globalMessageSource.get(FORM_DATA_MIN_VALUE_CONDITION_FAILED,key));
            case 7:   throw new InvalidInputException(FORM_DATA_MAX_VALUE_CONDITION_FAILED,globalMessageSource.get(FORM_DATA_MAX_VALUE_CONDITION_FAILED,key));
            case 8:   throw new InvalidInputException(FORM_DATA_MIN_WORD_CONDITION_FAILED,globalMessageSource.get(FORM_DATA_MIN_WORD_CONDITION_FAILED,key));
            case 9:   throw new InvalidInputException(FORM_DATA_MAX_WORD_CONDITION_EXCEEDED,globalMessageSource.get(FORM_DATA_MAX_WORD_CONDITION_EXCEEDED,key));
            default:  return FORM_DATA_MANDATORY_FIELDS_SUCCESS;
        }
    }

    @Override
    public AggregationResponse aggregateByFormIdFilterGroupBy(String formId, String filter, String groupBy, String operation)
    {
        if (!mongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA_ + formId))
        {
            throw new FormIdNotFoundException(FORM_DATA_DOES_NOT_EXISTS_WITH_GIVEN_FORMID, globalMessageSource.get(FORM_DATA_DOES_NOT_EXISTS_WITH_GIVEN_FORMID, formId));
        }
        Criteria criteria = new Criteria();
        ArrayList<Criteria> c1 = new ArrayList<>();
        List<AggregationOperation> aggregationOperationsList = new ArrayList<>();
        if(StringUtils.isNotEmpty(filter))
        {
            ArrayList<String> keysList = new ArrayList<>();
            ArrayList<String> valuesList = new ArrayList<>();
            String[] parts = filter.split(COMMA);
            for (String part : parts)
            {
                String[] keyValue = part.split(COLON);
                keysList.add(keyValue[0].replaceAll(REGEX_PATTERN_1, EMPTY_STRING));
                valuesList.add(keyValue[1].replaceAll(REGEX_PATTERN_1, EMPTY_STRING));
            }
            for (int i = 0; i < keysList.size(); i++)
            {
                if(keysList.get(i).equals(ID))
                {
                    c1.add(Criteria.where(UNDERSCORE_ID).is(Long.valueOf(valuesList.get(i))));
                }
                else
                {
                    String searchString = valuesList.get(i);  //if SearchString contains any alphabets or any special character
                    if(searchString.matches(CONTAINS_ATLEAST_ONE_ALPHABET)||searchString.matches(CONTAINS_ATLEAST_ONE_SPECIAL_CHARACTER))
                    {
                        c1.add(new Criteria().orOperator(Criteria.where(keysList.get(i)).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE))));
                    }
                    else
                    {
                        c1.add(new Criteria().orOperator(Criteria.where(keysList.get(i)).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                                Criteria.where(keysList.get(i)).is(Long.valueOf(searchString))));
                    }
                }
            }
            criteria = criteria.andOperator(c1.toArray(new Criteria[0]));
            aggregationOperationsList.add(Aggregation.match(criteria));
        }
        switch (operation)
        {
            case  COUNT:
                aggregationOperationsList.add(Aggregation.group(groupBy).count().as(COUNT));
                break;
            default:
        }
        List<Map> aggregateList= mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA_+formId,Map.class).getMappedResults();
        List<Map<String,String>> responseAggregationList=new ArrayList<>();
        for(Map map:aggregateList)
        {
           Map<String,String> aggregationMap=new HashMap<>();
           aggregationMap.put(UNDERSCORE_ID,String.valueOf(map.get(UNDERSCORE_ID)));
           aggregationMap.put(COUNT,String.valueOf(map.get(COUNT)));
           responseAggregationList.add(aggregationMap);
        }
        return new AggregationResponse(responseAggregationList);
    }
}
