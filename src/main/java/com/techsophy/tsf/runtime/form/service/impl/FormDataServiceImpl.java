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
import com.techsophy.tsf.runtime.form.utils.DocumentAggregationOperation;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import com.techsophy.tsf.runtime.form.utils.UserDetails;
import com.techsophy.tsf.runtime.form.utils.WebClientWrapper;
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
import java.io.IOException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import static com.techsophy.tsf.runtime.form.constants.ErrorConstants.*;
import static com.techsophy.tsf.runtime.form.constants.FormDataConstants.SEMICOLON;
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
    private FormValidationServiceImpl formValidationServiceImpl;

    @Override
    public FormDataResponse saveFormData(FormDataSchema formDataSchema) throws IOException
    {
        String formId=formDataSchema.getFormId();
        boolean documentFlag = false;
        BigInteger id;
        Integer version = null;
        FindIterable<Document> documents;
        Map<String, Object> loggedInUserDetails = userDetails.getUserDetails().get(0);
        checkUserDetailsPresentOrNot(loggedInUserDetails);
        WebClient webClient;
        String token = tokenUtils.getTokenFromContext();
        webClient = webClientWrapper.createWebClient(token);
        BigInteger loggedInUserId = BigInteger.valueOf(Long.parseLong(String.valueOf(loggedInUserDetails.get(ID))));
        FormResponseSchema formResponseSchema = formService.getRuntimeFormById(formId);
        List<ValidationResult> validationResultList= formValidationServiceImpl.validateData(formResponseSchema,formDataSchema,formId);
        StringBuilder completeMessage= new StringBuilder();
        validationResultList.stream().filter(v->!v.isValid()).forEach(v->{
            completeMessage.append(v.getErrorCode()).append(SEMICOLON).append(v.getErrorMessage(globalMessageSource)).append(SEMICOLON);
            throw new InvalidInputException(String.valueOf(completeMessage),String.valueOf(completeMessage));
        });
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
        uniqueDocumentId = extractUniqueDocumentId(formDataSchema, uniqueDocumentId);
        Map<String,Object> savedFormDataMap=new LinkedHashMap<>();
            Map<String,Object> updatedFormData=new LinkedHashMap<>();
            if (mongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA + formId))
            {
                if (StringUtils.isEmpty(uniqueDocumentId) || uniqueDocumentId.equals(NULL))
                {
                    savedFormDataMap = mongoTemplate.save(formDataMap, TP_RUNTIME_FORM_DATA +formId);
                    version = 1;
                    FormDataAuditSchema formDataAuditSchema = new FormDataAuditSchema(String.valueOf(idGenerator.nextId()), String.valueOf(formDataMap.get(UNDERSCORE_ID)),formId,version,
                            (Map<String, Object>) savedFormDataMap.get(FORM_DATA), (Map<String, Object>) savedFormDataMap.get(FORM_META_DATA));
                    this.formDataAuditService.saveFormDataAudit(formDataAuditSchema);
                }
                else
                {
                     documents = mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA +formId).find();
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
                            updatedFormData = mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA +formId).findOneAndReplace(filter,newDocument,findOneAndReplaceOptions);
                            documentFlag = true;
                            version = Integer.valueOf(String.valueOf(formDataMap.get(VERSION)));
                            assert updatedFormData != null;
                            FormDataAuditSchema formDataAuditSchema = new FormDataAuditSchema(String.valueOf(idGenerator.nextId()), String.valueOf(formDataMap.get(UNDERSCORE_ID)),formId, version,
                                    (Map<String, Object>) updatedFormData.get(FORM_DATA),(Map<String, Object>) updatedFormData.get(FORM_META_DATA));
                            this.formDataAuditService.saveFormDataAudit(formDataAuditSchema);
                        }
                    }
                    checkDocumentFlag(documentFlag, uniqueDocumentId);
                }
            }
            else
            {
                checkFormDataId(formDataSchema, uniqueDocumentId);
                savedFormDataMap = mongoTemplate.save(formDataMap, TP_RUNTIME_FORM_DATA +formId);
                version = 1;
                FormDataAuditSchema formDataAuditSchema = new
                        FormDataAuditSchema(String.valueOf(idGenerator.nextId()), String.valueOf(formDataMap.get(UNDERSCORE_ID)),formId,version,
                        (Map<String, Object>)savedFormDataMap.get(FORM_DATA),(Map<String, Object>) savedFormDataMap.get(FORM_META_DATA));
                this.formDataAuditService.saveFormDataAudit(formDataAuditSchema);
            }
            if(elasticEnable)
            {
                prepareSavedFormDataMap(savedFormDataMap,formId,id,webClient,uniqueDocumentId);
                prepareUpdatedFormDataMap(formId,webClient,uniqueDocumentId,updatedFormData);
            }
        return new FormDataResponse(String.valueOf(id),version);
    }

    private void prepareUpdatedFormDataMap(String formId,WebClient webClient,String uniqueDocumentId,Map<String,Object> updatedFormData) throws JsonProcessingException
    {
        if(StringUtils.isNotEmpty(uniqueDocumentId))
        {
            String response = fetchResponseFromElasticDB(formId, webClient, uniqueDocumentId);
            Map<String,Object> responseMap=this.objectMapper.readValue(response,Map.class);
            checkResponseMapData(uniqueDocumentId, responseMap);
            Map<String,Object> dataMap=this.objectMapper.convertValue(responseMap.get(DATA),LinkedHashMap.class);
            Integer version=Integer.valueOf(String.valueOf(dataMap.get(VERSION)));
            version=version+1;
            BigInteger id = BigInteger.valueOf(Long.parseLong(uniqueDocumentId));
            updatedFormData.remove(UNDERSCORE_ID);
            updatedFormData.put(ID,String.valueOf(id));
            updatedFormData.replace(VERSION,String.valueOf(version));
            updatedFormData.replace(CREATED_BY_ID,String.valueOf(dataMap.get(CREATED_BY_ID)));
            updatedFormData.replace(CREATED_ON,String.valueOf(dataMap.get(CREATED_ON)));
            updatedFormData.replace(CREATED_BY_NAME,String.valueOf(dataMap.get(CREATED_BY_NAME)));
            updatedFormData.replace(UPDATED_ON,String.valueOf(Instant.now()));
            updateElasticDocument(formId, id, webClient, updatedFormData, responseMap);
        }
    }
    private void prepareSavedFormDataMap(Map<String,Object> savedFormDataMap,String formId,BigInteger id,WebClient webClient,String uniqueDocumentId)
    {
        if (StringUtils.isEmpty(uniqueDocumentId)|| uniqueDocumentId.equals(NULL))
        {
            savedFormDataMap.remove(UNDERSCORE_ID);
            savedFormDataMap.put(ID,String.valueOf(id));
            savedFormDataMap.replace(CREATED_ON,String.valueOf(Instant.now()));
            savedFormDataMap.replace(UPDATED_ON,String.valueOf(Instant.now()));
            emptyIdSaveToElasticDB(formId, id, webClient, savedFormDataMap);
        }
    }

    private void updateElasticDocument(String formId, BigInteger id, WebClient webClient, Map<String, Object> updatedFormData, Map<String, Object> responseMap) {
        String response;
        try
        {
            response=webClientWrapper.webclientRequest(webClient,gatewayApi+ELASTIC_VERSION1+SLASH+ TP_RUNTIME_FORM_DATA + formId +PARAM_SOURCE+elasticSource,POST, updatedFormData);
            logger.info(response);
        }
        catch (Exception e)
        {
            Document newDocument = new Document(responseMap);
            Bson filter= Filters.eq(UNDERSCORE_ID,Long.valueOf(String.valueOf(id)));
            FindOneAndReplaceOptions findOneAndReplaceOptions = new FindOneAndReplaceOptions();
            findOneAndReplaceOptions.returnDocument(ReturnDocument.AFTER);
            mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA + formId).findOneAndReplace(filter,newDocument,findOneAndReplaceOptions);
            throw new RecordUnableToSaveException(UNABLE_TO_SAVE_IN_ELASTIC_AND_DB,globalMessageSource.get(UNABLE_TO_SAVE_IN_ELASTIC_AND_DB));
        }
    }

    private String fetchResponseFromElasticDB(String formId, WebClient webClient, String uniqueDocumentId) {
        String response;
        try
        {
            response=webClientWrapper.webclientRequest(webClient,gatewayApi+ELASTIC_VERSION1+SLASH+ uniqueDocumentId +PARAM_INDEX_NAME+ TP_RUNTIME_FORM_DATA + formId,GET,null);
            logger.info(response);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage());
            throw new InvalidInputException(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC,globalMessageSource.get(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC, uniqueDocumentId));
        }
        return response;
    }

    private void emptyIdSaveToElasticDB(String formId, BigInteger id, WebClient webClient, Map<String, Object> savedFormDataMap)
    {
        String response;
        try
        {
            response=webClientWrapper.webclientRequest(webClient,gatewayApi+ELASTIC_VERSION1+SLASH+ TP_RUNTIME_FORM_DATA + formId +PARAM_SOURCE+elasticSource,POST, savedFormDataMap);
            logger.info(response);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage());
            Bson filter= Filters.eq(UNDERSCORE_ID,Long.valueOf(String.valueOf(id)));
            mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA + formId).deleteMany(filter);
            throw new RecordUnableToSaveException(UNABLE_TO_SAVE_IN_ELASTIC_AND_DB,globalMessageSource.get(UNABLE_TO_SAVE_IN_ELASTIC_AND_DB));
        }
    }

    private void checkResponseMapData(String uniqueDocumentId, Map<String, Object> responseMap)
    {
        if(responseMap.get(DATA)==null)
        {
            throw new InvalidInputException(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC,globalMessageSource.get(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC, uniqueDocumentId));
        }
    }

    private String checkFormDataId(FormDataSchema formDataSchema, String uniqueDocumentId)
    {
        if (formDataSchema.getFormData().get(ID) != null || formDataSchema.getId()!= null)
        {
            uniqueDocumentId = formDataSchema.getId();
            uniqueDocumentId = extractUniqueDocumentId(formDataSchema, uniqueDocumentId);
            throw new FormIdNotFoundException(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC,globalMessageSource.get(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC, uniqueDocumentId));
        }
        return uniqueDocumentId;
    }

    private void checkDocumentFlag(boolean documentFlag, String uniqueDocumentId)
    {
        if (!documentFlag)
        {
            throw new InvalidInputException(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC,globalMessageSource.get(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC, uniqueDocumentId));
        }
    }

    private static String extractUniqueDocumentId(FormDataSchema formDataSchema, String uniqueDocumentId)
    {
        if (StringUtils.isEmpty(uniqueDocumentId))
        {
            uniqueDocumentId = String.valueOf(formDataSchema.getFormData().get(ID));
        }
        return uniqueDocumentId;
    }

    private void checkUserDetailsPresentOrNot(Map<String, Object> loggedInUserDetails)
    {
        if (StringUtils.isEmpty(String.valueOf(loggedInUserDetails.get(ID))))
        {
            throw new UserDetailsIdNotFoundException(LOGGED_IN_USER_ID_NOT_FOUND,globalMessageSource.get(LOGGED_IN_USER_ID_NOT_FOUND,String.valueOf(loggedInUserDetails.get(ID))));
        }
    }

    @Override
    public FormDataResponse updateFormData(FormDataSchema formDataSchema) throws JsonProcessingException
    {
        String formId=formDataSchema.getFormId();
        checkIfFormIdIsEmpty(formDataSchema, formId);
        checkIfIdIsEmpty(formDataSchema);
        checkMongoCollectionIfExistsOrNot(formDataSchema.getFormId());
        Map<String, Object> loggedInUserDetails = userDetails.getUserDetails().get(0);
        checkUserDetailsPresentOrNot(loggedInUserDetails);
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
            MongoCollection<Document> collection = mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA +formId);
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
                    extractDocumentFormMetaData(updatedFormData, documentFormData);
                    extractDocumentFormMetaData(updatedFormData, formDataSchema.getFormData());
                    extractDocumentFormMetaData(updatedFormMetaData, documentFormMetaData);
                    extractDocumentFormMetaData( updatedFormMetaData,formDataSchema.getFormMetadata());
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
        mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA +formId).findOneAndReplace(filter, updatedDocument,findOneAndReplaceOptions);
        if (elasticEnable)
        {
            String token = tokenUtils.getTokenFromContext();
            log.info(LOGGED_USER + loggedInUserId);
            log.info(TOKEN+token);
            WebClient webClient= checkEmptyToken(token);
            String response;
            log.info(GATEWAY+gatewayApi);
            formDataMap.remove(UNDERSCORE_ID);
            assert document != null;
            formDataMap.put(ID, String.valueOf(document.get(UNDERSCORE_ID)));
            formDataMap.put(CREATED_ON, String.valueOf(formDataMap.get(CREATED_ON)));
            formDataMap.put(UPDATED_ON, String.valueOf(formDataMap.get(UPDATED_ON)));
            try
            {
                response = webClientWrapper.webclientRequest(webClient, gatewayApi + ELASTIC_VERSION1 + SLASH + TP_RUNTIME_FORM_DATA + formId+ PARAM_SOURCE + elasticSource, POST, formDataMap);
                logger.info(response);
            }
            catch (Exception e)
            {
                logger.error(e.getMessage());
                filter = Filters.eq(UNDERSCORE_ID, Long.valueOf(String.valueOf(formDataSchema.getId())));
                mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA +formId).findOneAndReplace(filter, document, findOneAndReplaceOptions);
                throw new RecordUnableToSaveException(UNABLE_TO_UPDATE_IN_MONGODB_AND_ELASTIC_DB, globalMessageSource.get(UNABLE_TO_UPDATE_IN_MONGODB_AND_ELASTIC_DB));
            }
        }
        return new FormDataResponse(formDataSchema.getId(),version);
    }

    private static void extractDocumentFormMetaData(Map<String, Object> updatedFormMetaData, Map<String, Object> documentFormMetaData)
    {
        if (documentFormMetaData != null)
        {
            updatedFormMetaData.putAll(documentFormMetaData);
        }
    }

    private void checkIfIdIsEmpty(FormDataSchema formDataSchema)
    {
        if(StringUtils.isEmpty(formDataSchema.getId()))
        {
            throw new InvalidInputException(ID_CANNOT_BE_EMPTY,globalMessageSource.get(ID_CANNOT_BE_EMPTY));
        }
    }

    private void checkIfFormIdIsEmpty(FormDataSchema formDataSchema, String formId)
    {
        if (StringUtils.isEmpty(formDataSchema.getFormId()))
        {
            throw new InvalidInputException(FORM_ID_CANNOT_BE_EMPTY,globalMessageSource.get(FORM_ID_CANNOT_BE_EMPTY, formId));
        }
    }

    @Override
    public List getAllFormDataByFormId(String formId,String relations,String filter,String sortBy,String sortOrder)
    {
        if (elasticEnable&&relations==null)
        {
            List<Map<String,Object>> contentList;
            List<Map<String,Object>> responseList=new ArrayList<>();
            checkIfBothSortByAndSortOrderGivenAsInput(sortBy, sortOrder);
            String token = tokenUtils.getTokenFromContext();
            WebClient webClient = checkEmptyToken(token);
            try
            {
                String response;
                if (sortBy == null && sortOrder == null)
                {
                        response=webClientWrapper.webclientRequest(webClient,gatewayApi +ELASTIC_VERSION1+PARAM_FILTER+filter+AND_INDEX_NAME+ TP_RUNTIME_FORM_DATA +formId+AND_SOURCE+elasticSource,GET,null);
                        logger.info(response);
                }
                else
                {
                        response=webClientWrapper.webclientRequest(webClient,gatewayApi +ELASTIC_VERSION1+PARAM_SORT_BY+sortBy+AND_SORT_ORDER+sortOrder+AND_FILTER+filter+AND_INDEX_NAME+ TP_RUNTIME_FORM_DATA +formId+AND_SOURCE+elasticSource,GET,null);
                        logger.info(response);
                }
                Map<String,Object> responseMap=this.objectMapper.readValue(response,Map.class);
                Map<String,Object> dataMap=this.objectMapper.convertValue(responseMap.get(DATA),Map.class);
                contentList = this.objectMapper.convertValue(dataMap.get(CONTENT),List.class);
                prepareResponseList(contentList, responseList);
            }
            catch (Exception e)
            {
                throw new InvalidInputException(e.getMessage(), globalMessageSource.get(e.getMessage()));
            }
            return responseList;
        }
        checkMongoCollectionIfExistsOrNot(formId);
        Query query = new Query();
        ArrayList<String> keysList = new ArrayList<>();
        ArrayList<String> valuesList = new ArrayList<>();
        String[] parts = filter.split(COMMA);
        extractKeyValuesList(keysList, valuesList, parts);
        Criteria criteria = new Criteria();
        ArrayList<Criteria> c1 = new ArrayList<>();
        prepareCriteriaList(keysList, valuesList, c1);
        criteria = criteria.andOperator(c1.toArray(new Criteria[0]));
        if (StringUtils.isNotEmpty(relations))
        {
            ArrayList<String> mappedArrayOfDocumentsName=new ArrayList<>();
            String[] relationsList = relations.split(COMMA);
            ArrayList<String> relationKeysList = new ArrayList<>();
            ArrayList<String> relationValuesList = new ArrayList<>();
            List<Map<String, Object>> relationalMapList = new ArrayList<>();
            List<AggregationOperation> aggregationOperationsList = new ArrayList<>();
            prepareRelationList(mappedArrayOfDocumentsName, relationsList, relationKeysList, relationValuesList);
            prepareDocumentAggregateList(mappedArrayOfDocumentsName, relationKeysList, relationValuesList, aggregationOperationsList);
            aggregationOperationsList.add(Aggregation.match(criteria));
            List<Map<String, Object>> relationalMapList1 = getMapsEmptySort(formId, sortBy, sortOrder, relationalMapList, aggregationOperationsList);
            if (relationalMapList1 != null&&!relationalMapList1.isEmpty()) return relationalMapList1;
            aggregationOperationsList.add(Aggregation.sort(Sort.by(Sort.Direction.fromString(sortOrder), sortBy)));
            List<Document> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA + formId, Document.class).getMappedResults();
            prepareRelationsMap(relationalMapList, aggregateList);
            return relationalMapList;
        }
        query.addCriteria(criteria);
        List<FormDataResponseSchema> formDataResponseSchemasList1 = getFormDataResponseSchemasEmptySort(formId, sortBy, sortOrder, query);
        if (formDataResponseSchemasList1 != null&&!formDataResponseSchemasList1.isEmpty()) return formDataResponseSchemasList1;
        checkIfBothSortByAndSortOrderGivenAsInput(sortBy, sortOrder);
        query.with(Sort.by(Sort.Direction.fromString(sortOrder), sortBy));
        List<FormDataDefinition> formDataDefinitionsList = mongoTemplate.find(query, FormDataDefinition.class, TP_RUNTIME_FORM_DATA + formId);
        List<FormDataResponseSchema> formDataResponseSchemasList = new ArrayList<>();
        prepareFormDataResponseSchemaList(formDataResponseSchemasList, formDataDefinitionsList);
        return formDataResponseSchemasList;
    }

    private List<FormDataResponseSchema> getFormDataResponseSchemasEmptySort(String formId, String sortBy, String sortOrder, Query query)
    {
        if (StringUtils.isEmpty(sortBy) && StringUtils.isEmpty(sortOrder))
        {
            query.with(Sort.by(Sort.Direction.DESC, CREATED_ON));
            List<FormDataDefinition> formDataDefinitionsList = mongoTemplate.find(query, FormDataDefinition.class, TP_RUNTIME_FORM_DATA + formId);
            List<FormDataResponseSchema> formDataResponseSchemasList = new ArrayList<>();
            prepareFormDataResponseSchemaList(formDataResponseSchemasList, formDataDefinitionsList);
            return formDataResponseSchemasList;
        }
        return Collections.emptyList();
    }

    private List<Map<String, Object>> getMapsEmptySort(String formId, String sortBy, String sortOrder, List<Map<String, Object>> relationalMapList, List<AggregationOperation> aggregationOperationsList)
    {
        if (StringUtils.isEmpty(sortBy) && StringUtils.isEmpty(sortOrder))
        {
            aggregationOperationsList.add(Aggregation.sort(Sort.by(Sort.Direction.DESC,CREATED_ON)));
            List<Document> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList),
                    TP_RUNTIME_FORM_DATA + formId,Document.class).getMappedResults();
            prepareRelationsMap(relationalMapList, aggregateList);
            return relationalMapList;
        }
        return Collections.emptyList();
    }

    private static void prepareDocumentAggregateList(ArrayList<String> mappedArrayOfDocumentsName, ArrayList<String> relationKeysList, ArrayList<String> relationValuesList, List<AggregationOperation> aggregationOperationsList)
    {
        for(int j = 0; j< relationKeysList.size(); j++)
        {
            DocumentAggregationOperation documentAggregationOperation=new DocumentAggregationOperation(String.format(MONGO_AGGREGATION_STAGE_PIPELINE_1, relationKeysList.get(j), relationValuesList.get(j), relationValuesList.get(j), mappedArrayOfDocumentsName.get(j)));
            aggregationOperationsList.add(documentAggregationOperation);
        }
    }

    private static void extractKeyValuesList(ArrayList<String> keysList, ArrayList<String> valuesList, String[] parts)
    {
        for (String part : parts)
        {
            String[] keyValue = part.split(COLON);
            keysList.add(keyValue[0].replaceAll(REGEX_PATTERN_1, EMPTY_STRING));
            valuesList.add(keyValue[1].replaceAll(REGEX_PATTERN_1, EMPTY_STRING));
        }
    }

    private WebClient checkEmptyToken(String token)
    {
        WebClient webClient;
        if (StringUtils.isNotEmpty(token))
        {
            webClient = webClientWrapper.createWebClient(token);
        }
        else
        {
            throw new InvalidInputException(TOKEN_NOT_NULL, globalMessageSource.get(TOKEN_NOT_NULL, tokenUtils.getLoggedInUserId()));
        }
        return webClient;
    }

    @Override
    public PaginationResponsePayload getAllFormDataByFormId(String formId, String relations, String filter, String sortBy, String sortOrder, Pageable pageable)
    {
        PaginationResponsePayload paginationResponsePayload = new PaginationResponsePayload();
        if(elasticEnable&&relations==null)
        {
            List<Map<String,Object>> contentList;
            List<Map<String,Object>> responseList=new ArrayList<>();
            checkIfBothSortByAndSortOrderGivenAsInput(sortBy, sortOrder);
            String token = tokenUtils.getTokenFromContext();
            WebClient webClient = checkEmptyToken(token);
            try
            {
                String response=checkSortByAndSortOrder(formId, filter, sortBy, sortOrder, pageable, webClient);
                Map<String,Object> responseMap=this.objectMapper.readValue(response,Map.class);
                Map<String,Object> dataMap=this.objectMapper.convertValue(responseMap.get(DATA),Map.class);
                contentList = this.objectMapper.convertValue(dataMap.get(CONTENT),List.class);
                prepareResponseList(contentList, responseList);
                paginationResponsePayload.setContent(responseList);
                paginationResponsePayload.setPage(Integer.parseInt(String.valueOf(dataMap.get(PAGE))));
                paginationResponsePayload.setSize(Integer.parseInt(String.valueOf(dataMap.get(SIZE))));
                paginationResponsePayload.setTotalPages(Integer.parseInt(String.valueOf(dataMap.get(TOTAL_PAGES))));
                paginationResponsePayload.setTotalElements(Long.parseLong(String.valueOf(dataMap.get(TOTAL_ELEMENTS))));
                paginationResponsePayload.setNumberOfElements(Integer.parseInt(String.valueOf(dataMap.get(NUMBER_OF_ELEMENTS))));
            }
            catch (Exception e)
            {
                throw new InvalidInputException(e.getMessage(), globalMessageSource.get(e.getMessage()));
            }
            return paginationResponsePayload;
        }
        checkMongoCollectionIfExistsOrNot(formId);
        List<Map<String, Object>> content = new ArrayList<>();
        paginationResponsePayload.setPage(pageable.getPageNumber());
        paginationResponsePayload.setSize(pageable.getPageSize());
        Query query = new Query();
        ArrayList<String> keysList = new ArrayList<>();
        ArrayList<String> valuesList = new ArrayList<>();
        String[] parts = filter.split(COMMA);
        extractKeyValuesList(keysList, valuesList, parts);
        Criteria criteria = new Criteria();
        ArrayList<Criteria> c1 = new ArrayList<>();
        prepareCriteriaList(keysList, valuesList, c1);
        criteria = criteria.andOperator(c1.toArray(new Criteria[0]));
        PaginationResponsePayload paginationResponsePayload11 = getPaginationResponsePayloadIfReltionsExists(formId, relations, sortBy+";"+sortOrder, pageable, paginationResponsePayload, content, criteria);
        if (paginationResponsePayload11 != null) return paginationResponsePayload11;
        PaginationResponsePayload paginationResponsePayload1 = sortByAndSortOrderIsEmpty(formId, sortBy, sortOrder, pageable, paginationResponsePayload, content, criteria);
        if (paginationResponsePayload1 != null) return paginationResponsePayload1;
        checkIfBothSortByAndSortOrderGivenAsInput(sortBy, sortOrder);
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.fromString(sortOrder),sortBy));
        long totalMatchedRecords=mongoTemplate.count(query, TP_RUNTIME_FORM_DATA +formId);
        query.with(pageable);
        List<FormDataDefinition> formDataDefinitionsList=mongoTemplate.find(query,FormDataDefinition.class, TP_RUNTIME_FORM_DATA +formId);
        int totalPages = (int) Math.ceil((float)(totalMatchedRecords) / pageable.getPageSize());
        prepareContentList(content, formDataDefinitionsList);
        paginationResponsePayload.setContent(content);
        paginationResponsePayload.setTotalPages(totalPages);
        paginationResponsePayload.setTotalElements(totalMatchedRecords);
        paginationResponsePayload.setNumberOfElements(content.size());
        return paginationResponsePayload;
    }

    private static void prepareCriteriaList(ArrayList<String> keysList, ArrayList<String> valuesList, ArrayList<Criteria> c1)
    {
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
    }

    private PaginationResponsePayload getPaginationResponsePayloadIfReltionsExists(String formId, String relations,String sort, Pageable pageable, PaginationResponsePayload paginationResponsePayload, List<Map<String, Object>> content, Criteria criteria)
    {
        String sortBy=EMPTY_STRING;
        String sortOrder=EMPTY_STRING;
        if(sort.split(";").length!=0)
        {
            sortBy=sort.split(";")[0];
            sortOrder=sort.split(";")[1];
        }
        if (StringUtils.isNotEmpty(relations))
        {
            ArrayList<String> mappedArrayOfDocumentsName=new ArrayList<>();
            List<AggregationOperation> aggregationOperationsList = new ArrayList<>();
            String[] relationsList = relations.split(COMMA);
            ArrayList<String> relationKeysList = new ArrayList<>();
            ArrayList<String> relationValuesList = new ArrayList<>();
            prepareRelationList(mappedArrayOfDocumentsName, relationsList, relationKeysList, relationValuesList);
            aggregationOperationsList.add(Aggregation.match(criteria));
            prepareDocumentAggregateList(mappedArrayOfDocumentsName,  relationKeysList, relationValuesList,aggregationOperationsList);
            PaginationResponsePayload paginationResponsePayload1 = getPaginationWithMongoAndEmptySort(formId, sortBy, sortOrder, pageable, paginationResponsePayload, content, aggregationOperationsList);
            if (checkPaginationResponse(paginationResponsePayload1)) return paginationResponsePayload1;
            FacetOperation facetOperation=Aggregation.facet(Aggregation.count().as(COUNT)).as(METADATA).and(Aggregation.sort(Sort.by(Sort.Direction.fromString(sortOrder), sortBy)),
                    Aggregation.skip(pageable.getOffset()),Aggregation.limit(pageable.getPageSize())).as(DATA);
            aggregationOperationsList.add(facetOperation);
            List<Document> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA + formId, Document.class).getMappedResults();
            Map<String,Object> dataMap=aggregateList.get(0);
            List<Map<String,Object>> metaDataList= (List<Map<String, Object>>) dataMap.get(METADATA);
            List<Map<String,Object>> dataList= (List<Map<String,Object>>) dataMap.get(DATA);
            prepareContentListFromData(content, dataList);
            Map<String,Object> metaData = new HashMap<>();
            metaData = getMetaDataMap(metaDataList, metaData);
            long totalMatchedRecords = 0L;
            totalMatchedRecords = extractCountOfMatchedRecords(metaData, totalMatchedRecords);
            int totalPages = (int) Math.ceil((float) (totalMatchedRecords) / pageable.getPageSize());
            paginationResponsePayload.setContent(content);
            paginationResponsePayload.setTotalPages(totalPages);
            paginationResponsePayload.setTotalElements(totalMatchedRecords);
            paginationResponsePayload.setNumberOfElements(content.size());
            return paginationResponsePayload;
        }
        return null;
    }

    private String checkSortByAndSortOrder(String formId, String filter, String sortBy, String sortOrder, Pageable pageable, WebClient webClient)
    {
        String response;
        if (sortBy == null && sortOrder == null)
        {
                response=webClientWrapper.webclientRequest(webClient,gatewayApi +ELASTIC_VERSION1+PARAM_FILTER+ filter +AND_PAGE+ pageable.getPageNumber()+AND_SIZE+ pageable.getPageSize()+AND_INDEX_NAME+ TP_RUNTIME_FORM_DATA + formId +AND_SOURCE+elasticSource,GET,null);
                logger.info(response);
        }
        else
        {
                response=webClientWrapper.webclientRequest(webClient,gatewayApi +ELASTIC_VERSION1+PARAM_SORT_BY+ sortBy +AND_SORT_ORDER+ sortOrder +AND_FILTER+ filter +AND_PAGE+ pageable.getPageNumber()+AND_SIZE+ pageable.getPageSize()+AND_INDEX_NAME+ TP_RUNTIME_FORM_DATA + formId +AND_SOURCE+elasticSource,GET,null);
                logger.info(response);
        }
        return response;
    }

    private static void prepareResponseList(List<Map<String, Object>> contentList, List<Map<String, Object>> responseList)
    {
        for (Map<String, Object> contentMap : contentList)
        {
            Map<String,Object> modifiedFormDataResponse = new LinkedHashMap<>();
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
    }

    private PaginationResponsePayload sortByAndSortOrderIsEmpty(String formId, String sortBy, String sortOrder, Pageable pageable, PaginationResponsePayload paginationResponsePayload, List<Map<String, Object>> content, Criteria criteria)
    {
        if (StringUtils.isEmpty(sortBy) && StringUtils.isEmpty(sortOrder))
        {
            Query query=new Query();
            query.addCriteria(criteria);
            query.with(Sort.by(Sort.Direction.DESC, CREATED_ON));
            long totalMatchedRecords=mongoTemplate.count(query, TP_RUNTIME_FORM_DATA + formId);
            query.with(pageable);
            List<FormDataDefinition> formDataDefinitionsList = mongoTemplate.find(query, FormDataDefinition.class, TP_RUNTIME_FORM_DATA + formId);
            int totalPages = (int) Math.ceil((float) (totalMatchedRecords) / pageable.getPageSize());
            prepareContentList(content, formDataDefinitionsList);
            paginationResponsePayload.setContent(content);
            paginationResponsePayload.setTotalPages(totalPages);
            paginationResponsePayload.setTotalElements(totalMatchedRecords);
            paginationResponsePayload.setNumberOfElements(content.size());
            return paginationResponsePayload;
        }
        return null;
    }

    private static void prepareContentList(List<Map<String, Object>> content, List<FormDataDefinition> formDataDefinitionsList)
    {
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
    }

    private static long extractCountOfMatchedRecords(Map<String, Object> metaData, long totalMatchedRecords)
    {
        if(metaData !=null)
        {
            totalMatchedRecords = Long.parseLong(String.valueOf(metaData.get(COUNT)));
        }
        return totalMatchedRecords;
    }

    private static boolean checkPaginationResponse(PaginationResponsePayload paginationResponsePayload1)
    {
        return paginationResponsePayload1 != null;
    }

    private static void prepareRelationList(ArrayList<String> mappedArrayOfDocumentsName, String[] relationsList, ArrayList<String> relationKeysList, ArrayList<String> relationValuesList)
    {
        for (String relation : relationsList)
        {
            String[] keyValuePair = relation.split(COLON);
            mappedArrayOfDocumentsName.add(keyValuePair[0]);
            keyValuePair[0] = TP_RUNTIME_FORM_DATA + keyValuePair[0];
            keyValuePair[1] = FORM_DATA + DOT + keyValuePair[1];
            relationKeysList.add(keyValuePair[0].replaceAll(REGEX_PATTERN_1, EMPTY_STRING));
            relationValuesList.add(keyValuePair[1].replaceAll(REGEX_PATTERN_1, EMPTY_STRING));
        }
    }

    private static void prepareContentListFromData(List<Map<String, Object>> content, List<Map<String, Object>> dataList)
    {
        for (Map<String,Object> map : dataList)
        {
            map.put(ID,String.valueOf(map.get(UNDERSCORE_ID)));
            map.remove(UNDERSCORE_ID);
            content.add(map);
        }
    }

    private static Map<String, Object> getMetaDataMap(List<Map<String, Object>> metaDataList, Map<String, Object> metaData)
    {
        if(!metaDataList.isEmpty())
        {
            metaData = metaDataList.get(0);
        }
        return metaData;
    }

    public List getAllFormDataByFormIdAndQ(String formId, String relations, String q, String sortBy, String sortOrder)
    {
        List<Map<String, Object>> formDataList = getListWithElasticAndEmptyRelations(formId, relations, q, sortBy, sortOrder);
        if(checkFormDataList(formDataList)) return formDataList;
        checkMongoCollectionIfExistsOrNot(formId);
        List<Map<String, Object>> relationalMapList1 = checkIfRelationsExists(formId, relations, sortBy, sortOrder);
        if (relationalMapList1 != null&&!relationalMapList1.isEmpty()) return relationalMapList1;
        Query query = new Query();
        String searchString;
        searchString = checkValueOfQ(q);
        List<FormDataResponseSchema> formDataResponseSchemasList = new ArrayList<>();
        List<FormDataResponseSchema> formDataResponseSchemasList1 = ifSortEmpty(formId, sortBy, sortOrder, query, searchString, formDataResponseSchemasList);
        if (formDataResponseSchemasList1 != null&&!formDataResponseSchemasList1.isEmpty()) return formDataResponseSchemasList1;
        checkIfBothSortByAndSortOrderGivenAsInput(sortBy, sortOrder);
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
        formDataDefinitionsList = mongoTemplate.find(query, FormDataDefinition.class, TP_RUNTIME_FORM_DATA + formId);
        prepareFormDataResponseSchemaList(formDataResponseSchemasList, formDataDefinitionsList);
        return formDataResponseSchemasList;
    }

    private List<FormDataResponseSchema> ifSortEmpty(String formId, String sortBy, String sortOrder, Query query, String searchString, List<FormDataResponseSchema> formDataResponseSchemasList)
    {
        if(StringUtils.isEmpty(sortBy) && StringUtils.isEmpty(sortOrder))
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
            formDataDefinitionsList = mongoTemplate.find(query, FormDataDefinition.class, TP_RUNTIME_FORM_DATA + formId);
            prepareFormDataResponseSchemaList(formDataResponseSchemasList, formDataDefinitionsList);
            return formDataResponseSchemasList;
        }
        return Collections.emptyList();
    }

    private List<Map<String, Object>> checkIfRelationsExists(String formId, String relations, String sortBy, String sortOrder)
    {
        if (StringUtils.isNotEmpty(relations))
        {
            ArrayList<String> mappedArrayOfDocumentsName=new ArrayList<>();
            String[] relationsList = relations.split(COMMA);
            ArrayList<String> relationKeysList = new ArrayList<>();
            ArrayList<String> relationValuesList = new ArrayList<>();
            prepareRelationList(mappedArrayOfDocumentsName, relationsList, relationKeysList, relationValuesList);
            List<Map<String, Object>> relationalMapList = new ArrayList<>();
            List<AggregationOperation> aggregationOperationsList = new ArrayList<>();
            prepareDocumentAggregateList(mappedArrayOfDocumentsName, relationKeysList, relationValuesList, aggregationOperationsList);
            List<Map<String, Object>> relationalMapList1 = getMapsEmptySort(formId, sortBy, sortOrder, relationalMapList, aggregationOperationsList);
            if (relationalMapList1 != null&&!relationalMapList1.isEmpty()) return relationalMapList1;
            aggregationOperationsList.add(Aggregation.sort(Sort.by(Sort.Direction.fromString(sortOrder), sortBy)));
            List<Document> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA + formId, Document.class).getMappedResults();
            prepareRelationsMap(relationalMapList, aggregateList);
            return relationalMapList;
        }
        return Collections.emptyList();
    }

    private static boolean checkFormDataList(List<Map<String, Object>> formDataList)
    {
        return formDataList != null && !formDataList.isEmpty();
    }

    private static void prepareFormDataResponseSchemaList(List<FormDataResponseSchema> formDataResponseSchemasList, List<FormDataDefinition> formDataDefinitionsList)
    {
        for (FormDataDefinition formDataDefinition : formDataDefinitionsList)
        {
            FormDataResponseSchema formDataResponseSchema =
                    new FormDataResponseSchema(String.valueOf(formDataDefinition.getId()),
                            formDataDefinition.getFormData(), formDataDefinition.getFormMetadata(),String.valueOf(formDataDefinition.getVersion()),String.valueOf(formDataDefinition.getCreatedById()),
                            formDataDefinition.getCreatedOn(), String.valueOf(formDataDefinition.getUpdatedById()), formDataDefinition.getUpdatedOn());
            formDataResponseSchemasList.add(formDataResponseSchema);
        }
    }


    private static void prepareRelationsMap(List<Map<String, Object>> relationalMapList, List<Document> aggregateList)
    {
        for (Map<String,Object> map : aggregateList)
        {
            map.put(ID,String.valueOf(map.get(UNDERSCORE_ID)));
            map.remove(UNDERSCORE_ID);
            relationalMapList.add(map);
        }
    }

    private String checkValueOfQ(String q)
    {
        String searchString;
        if(q !=null)
        {
             searchString= URLDecoder.decode(q,StandardCharsets.UTF_8);
        }
        else
        {
            throw new InvalidInputException(FILTER_SHOULD_BE_GIVEN_ALONG_WITH_SORT_BY_SORT_ORDER,globalMessageSource.get(FILTER_SHOULD_BE_GIVEN_ALONG_WITH_SORT_BY_SORT_ORDER));
        }
        return searchString;
    }

    private List<Map<String, Object>> getListWithElasticAndEmptyRelations(String formId, String relations, String q, String sortBy, String sortOrder)
    {
        if (elasticEnable&& relations ==null)
        {
            return getFormDataList(formId, q, sortBy, sortOrder);
        }
        return Collections.emptyList();
    }

    private List<Map<String, Object>> getFormDataList(String formId, String q, String sortBy, String sortOrder)
    {
        List<Map<String,Object>> responseList=new ArrayList<>();
        checkIfBothSortByAndSortOrderGivenAsInput(sortBy, sortOrder);
        String token = tokenUtils.getTokenFromContext();
        WebClient webClient=checkEmptyToken(token);
        try
        {
            extractFromElastic(formId, q, sortBy, sortOrder, responseList, webClient);
        }
        catch (Exception e)
        {
            throw new InvalidInputException(e.getMessage(), globalMessageSource.get(e.getMessage()));
        }
        return responseList;
    }

    private void extractFromElastic(String formId, String q, String sortBy, String sortOrder, List<Map<String, Object>> responseList, WebClient webClient) throws JsonProcessingException
    {
        List<Map<String, Object>> contentList;
        String response = null;
        if (StringUtils.isEmpty(q) && StringUtils.isBlank(sortBy) && StringUtils.isBlank(sortOrder))
        {
               response = webClientWrapper.webclientRequest(webClient, gatewayApi + ELASTIC_VERSION1 + PARAM_INDEX_NAME + TP_RUNTIME_FORM_DATA + formId + AND_SOURCE + elasticSource, GET, null);
                logger.info(response);
        }
        if (StringUtils.isEmpty(q) && StringUtils.isNotBlank(sortBy) && StringUtils.isNotBlank(sortOrder))
        {
                response = webClientWrapper.webclientRequest(webClient, gatewayApi + ELASTIC_VERSION1 + PARAM_SORT_BY + sortBy + AND_SORT_ORDER + sortOrder + AND_INDEX_NAME + TP_RUNTIME_FORM_DATA + formId + AND_SOURCE + elasticSource, GET, null);
        }
        if (StringUtils.isNotEmpty(q) && StringUtils.isBlank(sortBy) && StringUtils.isBlank(sortOrder))
        {
                response = webClientWrapper.webclientRequest(webClient, gatewayApi + ELASTIC_VERSION1 + PARAM_Q + q + AND_INDEX_NAME + TP_RUNTIME_FORM_DATA + formId + AND_SOURCE + elasticSource, GET, null);
                logger.info(response);
        }
        if (StringUtils.isNotEmpty(q) && StringUtils.isNotBlank(sortBy) && StringUtils.isNotBlank(sortOrder))
        {
                response = webClientWrapper.webclientRequest(webClient, gatewayApi + ELASTIC_VERSION1 + PARAM_SORT_BY + sortBy + AND_SORT_ORDER + sortOrder + AND_Q + q + AND_INDEX_NAME + TP_RUNTIME_FORM_DATA + formId + AND_SOURCE + elasticSource, GET, null);
        }
        Map<String, Object> responseMap = this.objectMapper.readValue(response, Map.class);
        Map<String, Object> dataMap = this.objectMapper.convertValue(responseMap.get(DATA), Map.class);
        contentList = this.objectMapper.convertValue(dataMap.get(CONTENT),List.class);
        prepareResponseList(contentList, responseList);
    }

    @Override
    public PaginationResponsePayload getAllFormDataByFormIdAndQ(String formId, String relations, String q, String sortBy, String sortOrder, Pageable pageable)
    {
        PaginationResponsePayload paginationResponsePayload = new PaginationResponsePayload();
        PaginationResponsePayload formId1 = getPaginationWithElasticAndNoRelations(formId, relations, q, sortBy, sortOrder, pageable, paginationResponsePayload);
        if (checkPaginationResponse(formId1)) return formId1;
        checkMongoCollectionIfExistsOrNot(formId);
        paginationResponsePayload.setPage(pageable.getPageNumber());
        paginationResponsePayload.setSize(pageable.getPageSize());
        List<Map<String, Object>> content = new ArrayList<>();
        PaginationResponsePayload paginationResponsePayload1 = getPaginationWithMongoAndRelations(formId, relations, sortBy, sortOrder, pageable, paginationResponsePayload, content);
        if (checkPaginationResponse(paginationResponsePayload1)) return paginationResponsePayload1;
        Query query = new Query();
        String searchString= checkValueOfQ(q);
        PaginationResponsePayload paginationResponsePayload2 = sortByAndSortOrderIsEmpty(formId, sortBy, sortOrder, pageable, paginationResponsePayload, content, new Criteria().orOperator(Criteria.where(UNDERSCORE_ID).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                Criteria.where(VERSION).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                Criteria.where(CREATED_ON).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                Criteria.where(CREATED_BY_ID).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                Criteria.where(CREATED_BY_NAME).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                Criteria.where(UPDATED_ON).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                Criteria.where(UPDATED_BY_ID).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                Criteria.where(UPDATED_BY_NAME).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE))));
        if (paginationResponsePayload2 != null) return paginationResponsePayload2;
        checkIfBothSortByAndSortOrderGivenAsInput(sortBy, sortOrder);
        query.addCriteria(new Criteria().orOperator(Criteria.where(UNDERSCORE_ID).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                Criteria.where(VERSION).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                Criteria.where(CREATED_ON).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                Criteria.where(CREATED_BY_ID).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                Criteria.where(CREATED_BY_NAME).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                Criteria.where(UPDATED_ON).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                Criteria.where(UPDATED_BY_ID).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                Criteria.where(UPDATED_BY_NAME).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE))));
        query.with(Sort.by(Sort.Direction.fromString(sortOrder), sortBy));
        long totalMatchedRecords=mongoTemplate.count(query, TP_RUNTIME_FORM_DATA + formId);
        query.with(pageable);
        List<FormDataDefinition> formDataDefinitionsList = mongoTemplate.find(query, FormDataDefinition.class, TP_RUNTIME_FORM_DATA + formId);
        int totalPages = (int) Math.ceil((float) (totalMatchedRecords) / pageable.getPageSize());
        prepareContentList(content, formDataDefinitionsList);
        paginationResponsePayload.setContent(content);
        paginationResponsePayload.setTotalPages(totalPages);
        paginationResponsePayload.setTotalElements(totalMatchedRecords);
        paginationResponsePayload.setNumberOfElements(content.size());
        return paginationResponsePayload;
    }

    private void checkIfBothSortByAndSortOrderGivenAsInput(String sortBy, String sortOrder)
    {
        if (StringUtils.isEmpty(sortBy) && !StringUtils.isEmpty(sortOrder) || !StringUtils.isEmpty(sortBy) && StringUtils.isEmpty(sortOrder))
        {
            throw new InvalidInputException(SORTBY_AND_SORTORDER_BOTH_SHOULD_BE_GIVEN, globalMessageSource.get(SORTBY_AND_SORTORDER_BOTH_SHOULD_BE_GIVEN));
        }
    }

    private PaginationResponsePayload getPaginationWithMongoAndRelations(String formId, String relations, String sortBy, String sortOrder, Pageable pageable, PaginationResponsePayload paginationResponsePayload, List<Map<String, Object>> content)
    {
        if (StringUtils.isNotEmpty(relations))
        {
            ArrayList<String> mappedArrayOfDocumentsName=new ArrayList<>();
            String[] relationsList = relations.split(COMMA);
            ArrayList<String> relationKeysList = new ArrayList<>();
            ArrayList<String> relationValuesList = new ArrayList<>();
            prepareRelationList(mappedArrayOfDocumentsName, relationsList, relationKeysList, relationValuesList);
            List<AggregationOperation> aggregationOperationsList = new ArrayList<>();
            prepareDocumentAggregateList(mappedArrayOfDocumentsName, relationKeysList, relationValuesList, aggregationOperationsList);
            PaginationResponsePayload paginationResponsePayload1 = getPaginationWithMongoAndEmptySort(formId, sortBy, sortOrder, pageable, paginationResponsePayload, content, aggregationOperationsList);
            if (checkPaginationResponse(paginationResponsePayload1)) return paginationResponsePayload1;
            FacetOperation facetOperation=Aggregation.facet(Aggregation.count().as(COUNT)).as(METADATA).and(Aggregation.sort(Sort.by(Sort.Direction.fromString(sortOrder), sortBy)),
                    Aggregation.skip(pageable.getOffset()),Aggregation.limit(pageable.getPageSize())).as(DATA);
            aggregationOperationsList.add(facetOperation);
            List<Document> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA + formId, Document.class).getMappedResults();
            Map<String,Object> dataMap=aggregateList.get(0);
            List<Map<String,Object>> metaDataList= (List<Map<String, Object>>) dataMap.get(METADATA);
            List<Map<String,Object>> dataList= (List<Map<String,Object>>) dataMap.get(DATA);
            prepareContentListFromData(content, dataList);
            Map<String,Object> metaData = new HashMap<>();
            metaData = getMetaDataMap(metaDataList, metaData);
            long totalMatchedRecords = 0L;
            totalMatchedRecords = extractCountOfMatchedRecords(metaData, totalMatchedRecords);
            int totalPages = (int) Math.ceil((float) (totalMatchedRecords) / pageable.getPageSize());
            paginationResponsePayload.setContent(content);
            paginationResponsePayload.setTotalPages(totalPages);
            paginationResponsePayload.setTotalElements(totalMatchedRecords);
            paginationResponsePayload.setNumberOfElements(content.size());
            return paginationResponsePayload;
        }
        return null;
    }

    private PaginationResponsePayload getPaginationWithMongoAndEmptySort(String formId, String sortBy, String sortOrder, Pageable pageable, PaginationResponsePayload paginationResponsePayload, List<Map<String, Object>> content, List<AggregationOperation> aggregationOperationsList)
    {
        if (StringUtils.isEmpty(sortBy) && StringUtils.isEmpty(sortOrder))
        {
            FacetOperation facetOperation=Aggregation.facet(Aggregation.count().as(COUNT)).as(METADATA).and(Aggregation.sort(Sort.by(Sort.Direction.DESC, CREATED_ON)),
                    Aggregation.skip(pageable.getOffset()),Aggregation.limit(pageable.getPageSize())).as(DATA);
            aggregationOperationsList.add(facetOperation);
            List<Document> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA + formId, Document.class).getMappedResults();
            Map<String,Object> dataMap=aggregateList.get(0);
            List<Map<String,Object>> metaDataList= (List<Map<String, Object>>) dataMap.get(METADATA);
            List<Map<String,Object>> dataList= (List<Map<String,Object>>) dataMap.get(DATA);
            prepareContentListFromData(content, dataList);
            Map<String,Object> metaData = new HashMap<>();
            metaData = getMetaDataMap(metaDataList, metaData);
            long totalMatchedRecords = 0L;
            totalMatchedRecords = extractCountOfMatchedRecords(metaData, totalMatchedRecords);
            int totalPages = (int) Math.ceil((float)(totalMatchedRecords) / pageable.getPageSize());
            paginationResponsePayload.setContent(content);
            paginationResponsePayload.setTotalPages(totalPages);
            paginationResponsePayload.setTotalElements(totalMatchedRecords);
            paginationResponsePayload.setNumberOfElements(content.size());
            return paginationResponsePayload;
        }
        return null;
    }

    private void checkMongoCollectionIfExistsOrNot(String formId)
    {
        if (!mongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA + formId))
        {
            throw new FormIdNotFoundException(FORM_DATA_DOES_NOT_EXISTS_WITH_GIVEN_FORMID, globalMessageSource.get(FORM_DATA_DOES_NOT_EXISTS_WITH_GIVEN_FORMID, formId));
        }
    }

    private PaginationResponsePayload getPaginationWithElasticAndNoRelations(String formId, String relations, String q, String sortBy, String sortOrder, Pageable pageable, PaginationResponsePayload paginationResponsePayload)
    {
        if (elasticEnable&& relations ==null)
        {
            return getPaginationResponsePayload(formId, q, sortBy, sortOrder, pageable, paginationResponsePayload);
        }
        return null;
    }

    private PaginationResponsePayload getPaginationResponsePayload(String formId, String q, String sortBy, String sortOrder, Pageable pageable, PaginationResponsePayload paginationResponsePayload)
    {
        List<Map<String,Object>> contentList;
        List<Map<String,Object>> responseList=new ArrayList<>();
        checkIfBothSortByAndSortOrderGivenAsInput(sortBy,sortOrder);
        String token = tokenUtils.getTokenFromContext();
        WebClient webClient=checkEmptyToken(token);
        try
        {
            String response = EMPTY_STRING;
            response = getString(formId, q, sortBy, sortOrder, pageable, webClient, response);
            Map<String,Object> responseMap=this.objectMapper.readValue(response,Map.class);
            Map<String,Object> dataMap=this.objectMapper.convertValue(responseMap.get(DATA),Map.class);
            contentList = this.objectMapper.convertValue(dataMap.get(CONTENT),List.class);
            prepareResponseList(contentList, responseList);
            paginationResponsePayload.setContent(responseList);
            paginationResponsePayload.setPage(Integer.parseInt(String.valueOf(dataMap.get(PAGE))));
            paginationResponsePayload.setSize(Integer.parseInt(String.valueOf(dataMap.get(SIZE))));
            paginationResponsePayload.setTotalPages(Integer.parseInt(String.valueOf(dataMap.get(TOTAL_PAGES))));
            paginationResponsePayload.setTotalElements(Long.parseLong(String.valueOf(dataMap.get(TOTAL_ELEMENTS))));
            paginationResponsePayload.setNumberOfElements(Integer.parseInt(String.valueOf(dataMap.get(NUMBER_OF_ELEMENTS))));
        }
        catch (Exception e)
        {
            throw new InvalidInputException(e.getMessage(), globalMessageSource.get(e.getMessage(), formId));
        }
        return paginationResponsePayload;
    }

    private String getString(String formId, String q, String sortBy, String sortOrder, Pageable pageable, WebClient webClient, String response)
    {
        if (StringUtils.isEmpty(q) && StringUtils.isBlank(sortBy) && StringUtils.isBlank(sortOrder))
        {
                response =webClientWrapper.webclientRequest(webClient,gatewayApi +ELASTIC_VERSION1+PARAM_PAGE+ pageable.getPageNumber()+AND_SIZE+ pageable.getPageSize()+AND_INDEX_NAME+ TP_RUNTIME_FORM_DATA + formId +AND_SOURCE+elasticSource,GET,null);
                logger.info(response);
        }
        if(StringUtils.isEmpty(q)&&StringUtils.isNotBlank(sortBy)&&StringUtils.isNotBlank(sortOrder))
        {
            response =webClientWrapper.webclientRequest(webClient,gatewayApi +ELASTIC_VERSION1+PARAM_SORT_BY+ sortBy +AND_SORT_ORDER+ sortOrder +AND_PAGE+ pageable.getPageNumber()+AND_SIZE+ pageable.getPageSize()+AND_INDEX_NAME+ TP_RUNTIME_FORM_DATA + formId +AND_SOURCE+elasticSource,GET,null);
        }
        if(StringUtils.isNotEmpty(q)&&StringUtils.isBlank(sortBy)&&StringUtils.isBlank(sortOrder))
        {
                response =webClientWrapper.webclientRequest(webClient,gatewayApi +ELASTIC_VERSION1+PARAM_Q+ q +AND_PAGE+ pageable.getPageNumber()+AND_SIZE+ pageable.getPageSize()+AND_INDEX_NAME+ TP_RUNTIME_FORM_DATA + formId +AND_SOURCE+elasticSource,GET,null);
                logger.info(response);
        }
        if(StringUtils.isNotEmpty(q)&&StringUtils.isNotBlank(sortBy)&&StringUtils.isNotBlank(sortOrder))
        {
              response =webClientWrapper.webclientRequest(webClient,gatewayApi +ELASTIC_VERSION1+PARAM_SORT_BY+ sortBy +AND_SORT_ORDER+ sortOrder +AND_Q+ q +AND_PAGE+ pageable.getPageNumber()+AND_SIZE+ pageable.getPageSize()+AND_INDEX_NAME+ TP_RUNTIME_FORM_DATA + formId +AND_SOURCE+elasticSource,GET,null);
              logger.info(response);
        }
        return response;
    }

    public PaginationResponsePayload getAllFormDataByFormId(String formId,String relations)
    {
        PaginationResponsePayload paginationResponsePayload = new PaginationResponsePayload();
        if (elasticEnable&&relations==null)
        {
            return getPaginationResponsePayload(formId, paginationResponsePayload);
        }
        checkMongoCollectionIfExistsOrNot(formId);
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
            prepareRelationList(mappedArrayOfDocumentsName, relationsList, relationKeysList, relationValuesList);
            List<AggregationOperation> aggregationOperationsList = new ArrayList<>();
            prepareDocumentAggregateList(mappedArrayOfDocumentsName, relationKeysList, relationValuesList, aggregationOperationsList);
            FacetOperation facetOperation=Aggregation.facet(Aggregation.count().as(COUNT)).as(METADATA).and(Aggregation.sort(Sort.by(Sort.Direction.DESC, CREATED_ON)),
                    Aggregation.skip(pageable.getOffset()),Aggregation.limit(pageable.getPageSize())).as(DATA);
            aggregationOperationsList.add(facetOperation);
            List<Document> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA + formId, Document.class).getMappedResults();
            Map<String,Object> dataMap=aggregateList.get(0);
            List<Map<String,Object>> metaDataList= (List<Map<String, Object>>) dataMap.get(METADATA);
            List<Map<String,Object>> dataList= (List<Map<String,Object>>) dataMap.get(DATA);
            prepareContentListFromData(content, dataList);
            Map<String,Object> metaData = new HashMap<>();
            metaData = getMetaDataMap(metaDataList, metaData);
            long totalMatchedRecords = 0L;
            totalMatchedRecords = extractCountOfMatchedRecords(metaData, totalMatchedRecords);
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
        long totalMatchedRecords=mongoTemplate.count(query, TP_RUNTIME_FORM_DATA + formId);
        query.with(pageable);
        formDataDefinitionsList = mongoTemplate.find(query, FormDataDefinition.class, TP_RUNTIME_FORM_DATA + formId);
        int totalPages = (int) Math.ceil((float) (totalMatchedRecords) / pageable.getPageSize());
        prepareContentList(content, formDataDefinitionsList);
        paginationResponsePayload.setPage(0);
        paginationResponsePayload.setContent(content);
        paginationResponsePayload.setSize(defaultPageLimit);
        paginationResponsePayload.setTotalPages(totalPages);
        paginationResponsePayload.setTotalElements(totalMatchedRecords);
        paginationResponsePayload.setNumberOfElements(content.size());
        return paginationResponsePayload;
    }

    private PaginationResponsePayload getPaginationResponsePayload(String formId, PaginationResponsePayload paginationResponsePayload)
    {
        List<Map<String,Object>> contentList;
        List<Map<String,Object>> responseList=new ArrayList<>();
        String token = tokenUtils.getTokenFromContext();
        WebClient webClient= checkEmptyToken(token);
        try
        {
            String response= webClientWrapper.webclientRequest(webClient, gatewayApi + ELASTIC_VERSION1 + PARAM_INDEX_NAME + TP_RUNTIME_FORM_DATA + formId + AND_SOURCE + elasticSource + AND_PAGE_AND_SIZE + defaultPageLimit, GET, null);
            logger.info(response);
            Map<String, Object> responseMap = this.objectMapper.readValue(response, Map.class);
            Map<String, Object> dataMap = this.objectMapper.convertValue(responseMap.get(DATA), Map.class);
            contentList = this.objectMapper.convertValue(dataMap.get(CONTENT),List.class);
            prepareResponseList(contentList,responseList);
            paginationResponsePayload.setContent(responseList);
            paginationResponsePayload.setPage(Integer.parseInt(String.valueOf(dataMap.get(PAGE))));
            paginationResponsePayload.setSize(Integer.parseInt(String.valueOf(dataMap.get(SIZE))));
            paginationResponsePayload.setTotalPages(Integer.parseInt(String.valueOf(dataMap.get(TOTAL_PAGES))));
            paginationResponsePayload.setTotalElements(Long.parseLong(String.valueOf(dataMap.get(TOTAL_ELEMENTS))));
            paginationResponsePayload.setNumberOfElements(Integer.parseInt(String.valueOf(dataMap.get(NUMBER_OF_ELEMENTS))));
        }
        catch (Exception e)
        {
            throw new InvalidInputException(e.getMessage(), globalMessageSource.get(e.getMessage()));
        }
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
            webClient =checkEmptyToken(token);
            try
            {
                String response = webClientWrapper.webclientRequest(webClient, gatewayApi + ELASTIC_VERSION1 + SLASH + id + PARAM_INDEX_NAME + TP_RUNTIME_FORM_DATA + formId, GET, null);
                logger.info(response);
                Map<String,Object> responseMap = this.objectMapper.readValue(response,Map.class);
                Map<String,Object> dataMap = this.objectMapper.convertValue(responseMap.get(DATA), Map.class);
                Map<String,Object> modifiedFormDataResponse = new LinkedHashMap<>();
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
                throw new InvalidInputException(e.getMessage(), globalMessageSource.get(e.getMessage()));
            }
            return responseList;
        }
        if (StringUtils.isNotEmpty(relations))
        {
            return getFormDataList(formId, id, relations);
        }
        boolean documentFlag = false;
        Map<String, Object> formData;
        Map<String, Object> formMetaData;
        FormDataResponseSchema formDataResponseSchema = null;
        MongoCursor<Document> cursor;
        Bson filter = Filters.eq(UNDERSCORE_ID, Long.valueOf(id));
        checkMongoCollectionIfExistsOrNot(formId);
        try
        {
            MongoCollection<Document> collection = mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA + formId);
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
            throw new InvalidInputException(e.getMessage(), globalMessageSource.get(e.getMessage()));
        }
        if(!documentFlag)
        {
            throw new FormIdNotFoundException(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC,globalMessageSource.get(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC,id));
        }
        assert formDataResponseSchema != null;
        return  List.of(formDataResponseSchema);
    }

    private List<Map<String, Object>> getFormDataList(String formId, String id, String relations)
    {
        ArrayList<String> mappedArrayOfDocumentsName=new ArrayList<>();
        String[] relationsList = relations.split(COMMA);
        ArrayList<String> relationKeysList = new ArrayList<>();
        ArrayList<String> relationValuesList = new ArrayList<>();
        prepareRelationList(mappedArrayOfDocumentsName, relationsList, relationKeysList, relationValuesList);
        List<Map<String, Object>> relationalMapList = new ArrayList<>();
        List<AggregationOperation> aggregationOperationsList = new ArrayList<>();
        aggregationOperationsList.add(Aggregation.match(Criteria.where(UNDERSCORE_ID).is(Long.valueOf(id))));
        prepareDocumentAggregateList(mappedArrayOfDocumentsName, relationKeysList, relationValuesList, aggregationOperationsList);
        aggregationOperationsList.add(Aggregation.sort(Sort.by(Sort.Direction.DESC, CREATED_ON)));
        List<Document> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA + formId, Document.class).getMappedResults();
        prepareRelationsMap(relationalMapList, aggregateList);
        return relationalMapList;
    }

    @Override
    public void deleteAllFormDataByFormId(String formId)
    {
        try
        {
            mongoTemplate.dropCollection(TP_RUNTIME_FORM_DATA +formId+AUDIT);
            checkMongoCollectionIfExistsOrNot(formId);
            mongoTemplate.dropCollection(TP_RUNTIME_FORM_DATA +formId);
        }
        catch (Exception e)
        {
            throw new InvalidInputException(e.getMessage(), globalMessageSource.get(e.getMessage()));
        }
        if(elasticEnable)
        {
            String token = tokenUtils.getTokenFromContext();
            WebClient webClient= checkEmptyToken(token);
            try
            {
                    String  response= webClientWrapper.webclientRequest(webClient, gatewayApi + ELASTIC_VERSION1 + SLASH+PARAM_INDEX_NAME+ TP_RUNTIME_FORM_DATA +formId,DELETE,null);
                    logger.info(response);
            }
            catch (Exception e)
            {
                throw new InvalidInputException(e.getMessage(),globalMessageSource.get(e.getMessage()));
            }
        }
    }

    @Override
    public void deleteFormDataByFormIdAndId(String formId, String id)
    {
        boolean flag = false;
        long count = 0;
        Bson filter= Filters.eq(UNDERSCORE_ID,Long.valueOf(id));
        checkMongoCollectionIfExistsOrNot(formId);
        try
        {
                DeleteResult deleteResult= mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA +formId).deleteMany(filter);
                count=deleteResult.getDeletedCount();
                filter= Filters.eq(FORM_DATA_ID,Long.valueOf(id));
                mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA +formId+AUDIT).deleteMany(filter);
                flag=true;
        }
        catch(Exception e)
        {
            logger.error(e.getMessage());
        }
        if(count==0)
        {
            throw new FormIdNotFoundException(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC,globalMessageSource.get(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC,id));
        }
        if(elasticEnable)
        {
            String token = tokenUtils.getTokenFromContext();
            WebClient webClient=checkEmptyToken(token);
            try
            {
                if(flag)
                {
                    String response = webClientWrapper.webclientRequest(webClient, gatewayApi + ELASTIC_VERSION1 + SLASH + id + PARAM_INDEX_NAME + TP_RUNTIME_FORM_DATA + formId, DELETE, null);
                        logger.info(response);
                }
                else
                {
                    throw new EntityIdNotFoundException(UNABLE_TO_DELETE_FORM_DATA_IN_ELASTIC_DB,globalMessageSource.get(UNABLE_TO_DELETE_FORM_DATA_IN_ELASTIC_DB));
                }
            }
            catch (Exception e)
            {
                logger.error(e.getMessage());
            }
        }
    }

    @Override
    public AggregationResponse aggregateByFormIdFilterGroupBy(String formId, String filter, String groupBy, String operation)
    {
        checkMongoCollectionIfExistsOrNot(formId);
        Criteria criteria = new Criteria();
        ArrayList<Criteria> c1 = new ArrayList<>();
        List<AggregationOperation> aggregationOperationsList = new ArrayList<>();
        if(StringUtils.isNotEmpty(filter))
        {
            createMultipleFilterCriterias(filter, criteria, c1, aggregationOperationsList);
        }
        if(operation.equals(COUNT))
        {
            aggregationOperationsList.add(Aggregation.group(groupBy).count().as(COUNT));
        }
       List<Document> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList),
               TP_RUNTIME_FORM_DATA + formId, Document.class).getMappedResults();
        List<Map<String,String>> responseAggregationList=new ArrayList<>();
        for(Map<String,Object> map: aggregateList)
        {
           Map<String,String> aggregationMap=new HashMap<>();
           aggregationMap.put(UNDERSCORE_ID,String.valueOf(map.get(UNDERSCORE_ID)));
           aggregationMap.put(COUNT,String.valueOf(map.get(COUNT)));
           responseAggregationList.add(aggregationMap);
        }
        return new AggregationResponse(responseAggregationList);
    }

    private static void createMultipleFilterCriterias(String filter, Criteria criteria, ArrayList<Criteria> c1, List<AggregationOperation> aggregationOperationsList)
    {
        ArrayList<String> keysList = new ArrayList<>();
        ArrayList<String> valuesList = new ArrayList<>();
        String[] parts = filter.split(COMMA);
        extractKeyValuesList(keysList, valuesList, parts);
        prepareCriteriaList(keysList, valuesList, c1);
        criteria = criteria.andOperator(c1.toArray(new Criteria[0]));
        aggregationOperationsList.add(Aggregation.match(criteria));
    }
}
