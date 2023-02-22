package com.techsophy.tsf.runtime.form.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
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
import static org.apache.commons.lang3.StringUtils.*;

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
        BigInteger id;
        int version;
        checkUserDetailsPresentOrNot(userDetails);
        Map<String, Object> loggedInUserDetails = userDetails.getUserDetails().get(0);
        BigInteger loggedInUserId = BigInteger.valueOf(Long.parseLong(String.valueOf(loggedInUserDetails.get(ID))));
        FormResponseSchema formResponseSchema = formService.getRuntimeFormById(formId);
        List<ValidationResult> validationResultList= formValidationServiceImpl.validateData(formResponseSchema,formDataSchema,formId);
        StringBuilder completeMessage= new StringBuilder();
        validationResultList.stream().filter(v->!v.isValid()).forEach(v->{
            completeMessage.append(v.getErrorCode()).append(SEMICOLON).append(v.getErrorMessage(globalMessageSource)).append(SEMICOLON);
            throw new InvalidInputException(String.valueOf(completeMessage),String.valueOf(completeMessage));
        });
        String uniqueDocumentId = extractUniqueDocumentId(formDataSchema);
        FormDataDefinition formDataDefinition = getFormDataDefinition(formDataSchema);
        setUpdateAudit(loggedInUserId, formDataDefinition);
        if (mongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA + formId))
            {
                if (uniqueDocumentId==null|| isEmpty(uniqueDocumentId))
                {
                    id = getNextId();
                    formDataDefinition.setId(String.valueOf(id));
                    version = 1;
                    setVersion(formDataDefinition, version);
                    setCreatedAudit(loggedInUserId, formDataDefinition);
                    saveToMongo(formId, formDataDefinition);
                    FormDataAuditSchema formDataAuditSchema = getFormDataAuditSchema(formId, version, formDataDefinition);
                    this.formDataAuditService.saveFormDataAudit(formDataAuditSchema);
                }
                else
                {
                    Bson filter=Filters.eq(UNDERSCORE_ID,uniqueDocumentId);
                    version= (int) mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA +formId).find(filter).iterator().next().get(VERSION);
                    version = updateVersion(version);
                    id=BigInteger.valueOf(Long.parseLong(uniqueDocumentId));
                    mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA +formId).updateOne(filter,Updates.combine(
                            Updates.set(FORM_DATA,formDataDefinition.getFormData()),Updates.set(VERSION,version)));
                    FormDataAuditSchema formDataAuditSchema = getFormDataAuditSchema(formId, 1, formDataDefinition);
                    this.formDataAuditService.saveFormDataAudit(formDataAuditSchema);
                }
            }
            else
            {
                if(uniqueDocumentId!=null)
                {
                    throw new InvalidInputException(PAYLOAD_SHOULD_NOT_HAVE_ID,globalMessageSource.get(PAYLOAD_SHOULD_NOT_HAVE_ID));
                }
                id = getNextId();
                formDataDefinition.setId(String.valueOf(id));
                version = 1;
                setVersion(formDataDefinition, version);
                setCreatedAudit(loggedInUserId, formDataDefinition);
                saveToMongo(formId, formDataDefinition);
                FormDataAuditSchema formDataAuditSchema = new
                        FormDataAuditSchema(String.valueOf(getNextId()),String.valueOf(formDataDefinition.getId()),formId,version,
                        formDataSchema.getFormData(),formDataSchema.getFormMetaData());
                this.formDataAuditService.saveFormDataAudit(formDataAuditSchema);
            }
            if(elasticEnable)
            {
                WebClient webClient=checkEmptyToken(tokenUtils.getTokenFromContext());
                saveFormDataToElastic(formDataDefinition,webClient,uniqueDocumentId);
                updateFormDataToElastic(webClient,uniqueDocumentId,formDataDefinition);
            }
        return new FormDataResponse(String.valueOf(id),version);
    }

    private FormDataAuditSchema getFormDataAuditSchema(String formId, int version, FormDataDefinition formDataDefinition) {
       return new FormDataAuditSchema(String.valueOf(getNextId()),String.valueOf(formDataDefinition.getId()), formId, version,
                formDataDefinition.getFormData(), formDataDefinition.getFormMetaData());
    }

    private void saveToMongo(String formId, FormDataDefinition formDataDefinition) {
        mongoTemplate.save(formDataDefinition, TP_RUNTIME_FORM_DATA + formId);
    }

    private static void setUpdateAudit(BigInteger loggedInUserId, FormDataDefinition formDataDefinition) {
        formDataDefinition.setUpdatedById(String.valueOf(loggedInUserId));
        formDataDefinition.setUpdatedOn(String.valueOf(Date.from(Instant.now())));
    }

    private FormDataDefinition getFormDataDefinition(FormDataSchema formDataSchema)
    {
      return objectMapper.convertValue(formDataSchema,FormDataDefinition.class);
    }

    private static void setCreatedAudit(BigInteger loggedInUserId, FormDataDefinition formDataDefinition)
    {
        formDataDefinition.setCreatedById(String.valueOf(loggedInUserId));
        formDataDefinition.setCreatedOn(String.valueOf(Date.from(Instant.now())));
    }

    private BigInteger getNextId()
    {
        BigInteger id;
        id=idGenerator.nextId();
        return id;
    }

    private void updateFormDataToElastic(WebClient webClient,String uniqueDocumentId,FormDataDefinition formDataDefinition) throws JsonProcessingException
    {
        if(uniqueDocumentId!=null)
        {
            String response = fetchResponseFromElasticDB(formDataDefinition.getFormId(), webClient, uniqueDocumentId);
            Map<String,Object> responseMap= getResponseMap(response);
            checkResponseMapData(uniqueDocumentId, responseMap);
            Map<String,Object> dataMap= getMap(responseMap);
            int version= Integer.parseInt(String.valueOf(dataMap.get(VERSION)));
            version = updateVersion(version);
            setVersion(formDataDefinition, version);
            formDataDefinition.setId(uniqueDocumentId);
            formDataDefinition.setCreatedById(String.valueOf(dataMap.get(CREATED_BY_ID)));
            formDataDefinition.setCreatedOn(String.valueOf(dataMap.get(CREATED_ON)));
            updateElasticDocument(webClient,formDataDefinition, responseMap);
        }
    }

    private static int updateVersion(int version) {
        version = version +1;
        return version;
    }

    private static void setVersion(FormDataDefinition formDataDefinition, int version) {
        formDataDefinition.setVersion(version);
    }

    private LinkedHashMap<String,Object> getMap(Map<String, Object> responseMap)
    {
        return this.objectMapper.convertValue(responseMap.get(DATA), LinkedHashMap.class);
    }

    private Map<String,Object> getResponseMap(String response) throws JsonProcessingException
    {
        return this.objectMapper.readValue(response, Map.class);
    }

    private void saveFormDataToElastic(FormDataDefinition formDataDefinition,WebClient webClient,String uniqueDocumentId)
    {
        if (uniqueDocumentId==null)
        {
            emptyIdSaveToElasticDB(webClient, formDataDefinition);
        }
    }

    private void updateElasticDocument(WebClient webClient,FormDataDefinition formDataDefinition, Map<String, Object> responseMap)
    {
        String response;
        try
        {
            response = postWebClientResponse(webClient, formDataDefinition);
            logger.info(response);
        }
        catch (Exception e)
        {
            Document newDocument = new Document(responseMap);
            Bson filter= Filters.eq(UNDERSCORE_ID,Long.valueOf(String.valueOf(formDataDefinition.getId())));
            FindOneAndReplaceOptions findOneAndReplaceOptions = new FindOneAndReplaceOptions();
            findOneAndReplaceOptions.returnDocument(ReturnDocument.AFTER);
            mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA + formDataDefinition.getFormId()).findOneAndReplace(filter,newDocument,findOneAndReplaceOptions);
            throw new RecordUnableToSaveException(UNABLE_TO_SAVE_IN_ELASTIC_AND_DB,globalMessageSource.get(UNABLE_TO_SAVE_IN_ELASTIC_AND_DB));
        }
    }

    private String postWebClientResponse(WebClient webClient, FormDataDefinition formDataDefinition)
    {
        return webClientWrapper.webclientRequest(webClient,gatewayApi+ELASTIC_VERSION1+SLASH+ TP_RUNTIME_FORM_DATA + formDataDefinition.getFormId() +PARAM_SOURCE+elasticSource,POST, formDataDefinition);
    }

    private String fetchResponseFromElasticDB(String formId, WebClient webClient, String uniqueDocumentId)
    {
        String response;
        try
        {
            response = getWebClientResponse(formId, webClient, uniqueDocumentId);
            logger.info(response);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage());
            throw new InvalidInputException(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC,globalMessageSource.get(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC, uniqueDocumentId));
        }
        return response;
    }

    private String getWebClientResponse(String formId, WebClient webClient, String uniqueDocumentId)
    {
        return webClientWrapper.webclientRequest(webClient,gatewayApi+ELASTIC_VERSION1+SLASH+ uniqueDocumentId +PARAM_INDEX_NAME+ TP_RUNTIME_FORM_DATA + formId,GET,null);
    }

    private void emptyIdSaveToElasticDB(WebClient webClient,FormDataDefinition formDataDefinition)
    {
        String response;
        try
        {
            response= postWebClientResponse(webClient,formDataDefinition);
            logger.info(response);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage());
            Bson filter= Filters.eq(UNDERSCORE_ID,Long.valueOf(String.valueOf(formDataDefinition.getId())));
            mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA + formDataDefinition.getFormId()).deleteMany(filter);
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

    private static String extractUniqueDocumentId(FormDataSchema formDataSchema)
    {
        String uniqueDocumentId = getUniqueDocumentId(formDataSchema);
        if (isEmpty(uniqueDocumentId)&&formDataSchema.getFormData().get(ID)!=null)
        {
            uniqueDocumentId = String.valueOf(formDataSchema.getFormData().get(ID));
        }
        return uniqueDocumentId;
    }

    private static String getUniqueDocumentId(FormDataSchema formDataSchema) {
        return formDataSchema.getId();
    }

    private void checkUserDetailsPresentOrNot(UserDetails userDetails) throws JsonProcessingException
    {
        Map<String, Object> loggedInUserDetails=userDetails.getUserDetails().get(0);
        if (isEmpty(String.valueOf(loggedInUserDetails.get(ID))))
        {
            throw new UserDetailsIdNotFoundException(LOGGED_IN_USER_ID_NOT_FOUND,globalMessageSource.get(LOGGED_IN_USER_ID_NOT_FOUND,String.valueOf(loggedInUserDetails.get(ID))));
        }
    }

    @Override
    public FormDataResponse updateFormData(FormDataSchema formDataSchema) throws JsonProcessingException
    {
        String formId=formDataSchema.getFormId();
        String id = getUniqueDocumentId(formDataSchema);
        checkIfFormIdIsEmpty(formDataSchema, formId);
        checkIfIdIsEmpty(id);
        checkMongoCollectionIfExistsOrNot(formDataSchema.getFormId());
        checkUserDetailsPresentOrNot(userDetails);
        Map<String, Object> loggedInUserDetails = userDetails.getUserDetails().get(0);
        BigInteger loggedInUserId = BigInteger.valueOf(Long.parseLong(String.valueOf(loggedInUserDetails.get(ID))));
        Bson filter=Filters.eq(UNDERSCORE_ID,formDataSchema.getId());
        Document document=new Document();
        if(mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA +formId).find(filter).iterator().hasNext())
        {
            document=mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA +formId).find(filter).iterator().next();
        }
        int version=0;
        Map<String,Object> formData=new HashMap<>();
        Map<String,Object> formMetaData=new HashMap<>();
        FormDataDefinition formDataDefinition=new FormDataDefinition();
        if(document.get(VERSION)!=null)
        {
            version= (int) document.get(VERSION);
        }
        version = updateVersion(version);
        setVersion(formDataDefinition, version);
        formDataDefinition.setId(id);
        formDataDefinition.setFormId(formId);
        if(document.get(FORM_DATA)!=null)
        {
            formData= (Map<String, Object>) document.get(FORM_DATA);
        }
        if(formDataSchema.getFormData()!=null)
        {
            formData.putAll(formDataSchema.getFormData());
        }
        formDataDefinition.setFormData(formData);
        if(document.get(FORM_META_DATA)!=null)
        {
            formMetaData= (Map<String, Object>) document.get(FORM_META_DATA);
        }
        if(formDataSchema.getFormMetaData()!=null)
        {
            formMetaData.putAll(formDataSchema.getFormMetaData());
        }
        formDataDefinition.setFormMetaData(formMetaData);
        formDataDefinition.setCreatedById(String.valueOf(document.get(CREATED_BY_ID)));
        formDataDefinition.setCreatedOn(String.valueOf(document.get(CREATED_ON)));
        formDataDefinition.setUpdatedById(String.valueOf(loggedInUserId));
        formDataDefinition.setUpdatedOn(String.valueOf(Instant.now()));
        mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA +formId).updateOne(filter,Updates.combine(
                Updates.set(FORM_DATA,formData),Updates.set(VERSION,version),Updates.set(FORM_META_DATA,formMetaData)));
        FormDataAuditSchema formDataAuditSchema = getFormDataAuditSchema(formId, version, formDataDefinition);
        this.formDataAuditService.saveFormDataAudit(formDataAuditSchema);
        if (elasticEnable)
        {
            String token = tokenUtils.getTokenFromContext();
            log.info(LOGGED_USER + loggedInUserId);
            log.info(TOKEN+token);
            WebClient webClient= checkEmptyToken(token);
            String response;
            log.info(GATEWAY+gatewayApi);
            try
            {
                response=postWebClientResponse(webClient,formDataDefinition);
                logger.info(response);
            }
            catch (Exception e)
            {
                logger.error(e.getMessage());
                filter= Filters.eq(UNDERSCORE_ID,Long.valueOf(String.valueOf(formDataDefinition.getId())));
                mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA + formDataDefinition.getFormId()).deleteMany(filter);
                throw new RecordUnableToSaveException(UNABLE_TO_SAVE_IN_ELASTIC_AND_DB,globalMessageSource.get(UNABLE_TO_SAVE_IN_ELASTIC_AND_DB));
            }
        }
        return new FormDataResponse(id,version);
    }

    private void checkIfIdIsEmpty(String id)
    {
        if(isEmpty(id))
        {
            throw new InvalidInputException(ID_CANNOT_BE_EMPTY,globalMessageSource.get(ID_CANNOT_BE_EMPTY));
        }
    }

    private void checkIfFormIdIsEmpty(FormDataSchema formDataSchema, String formId)
    {
        if (isEmpty(formDataSchema.getFormId()))
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
            checkIfBothSortByAndSortOrderGivenAsInput(sortBy, sortOrder);
            WebClient webClient = checkEmptyToken(tokenUtils.getTokenFromContext());
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
                Map<String,Object> responseMap= getResponseMap(response);
                Map<String,Object> dataMap= getDataMap(responseMap);
                contentList = getContentList(dataMap);
            }
            catch (Exception e)
            {
                throw new InvalidInputException(e.getMessage(), globalMessageSource.get(e.getMessage()));
            }
            return contentList;
        }
        checkMongoCollectionIfExistsOrNot(formId);
        ArrayList<String> keysList = new ArrayList<>();
        ArrayList<String> valuesList = new ArrayList<>();
        extractKeyValuesList(keysList, valuesList, filter);
        Criteria criteria=prepareCriteriaList(keysList,valuesList);
        if (isNotEmpty(relations))
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
            return getMapsEmptySort(formId, sortBy, sortOrder, relationalMapList, aggregationOperationsList);
        }
        return getFormDataResponseSchemasSort(formId, sortBy, sortOrder);
    }

    private List<Map<String,Object>> getContentList(Map<String, Object> dataMap)
    {
        return this.objectMapper.convertValue(dataMap.get(CONTENT), List.class);
    }

    private Map<String,Object> getDataMap(Map<String, Object> responseMap)
    {
        return this.objectMapper.convertValue(responseMap.get(DATA), Map.class);
    }

    private List<FormDataResponseSchema> getFormDataResponseSchemasSort(String formId, String sortBy, String sortOrder)
    {
        Query query=new Query();
        checkIfBothSortByAndSortOrderGivenAsInput(sortBy, sortOrder);
        List<FormDataResponseSchema> formDataResponseSchemasList = new ArrayList<>();
        if (isEmpty(sortBy) && isEmpty(sortOrder))
        {
            setQuery(query);
            List<FormDataDefinition> formDataDefinitionsList = getFormDataDefinitionsList(formId, query);
            prepareFormDataResponseSchemaList(formDataResponseSchemasList, formDataDefinitionsList);
        }
        else
        {
            query.with(Sort.by(Sort.Direction.fromString(sortOrder), sortBy));
            List<FormDataDefinition> formDataDefinitionsList = getFormDataDefinitionsList(formId, query);
            prepareFormDataResponseSchemaList(formDataResponseSchemasList, formDataDefinitionsList);
        }
        return formDataResponseSchemasList;
    }

    private List<Map<String, Object>> getMapsEmptySort(String formId, String sortBy, String sortOrder, List<Map<String, Object>> relationalMapList, List<AggregationOperation> aggregationOperationsList)
    {
        if (isEmpty(sortBy) && isEmpty(sortOrder))
        {
            aggregationOperationsList.add(Aggregation.sort(Sort.by(Sort.Direction.DESC,CREATED_ON)));
            List<Document> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList),
                    TP_RUNTIME_FORM_DATA + formId,Document.class).getMappedResults();
            prepareRelationsMap(relationalMapList, aggregateList);
        }
        else
        {
            aggregationOperationsList.add(Aggregation.sort(Sort.by(Sort.Direction.fromString(sortOrder), sortBy)));
            List<Document> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA + formId, Document.class).getMappedResults();
            prepareRelationsMap(relationalMapList, aggregateList);
        }
        return relationalMapList;
    }

    private static void prepareDocumentAggregateList(ArrayList<String> mappedArrayOfDocumentsName, ArrayList<String> relationKeysList, ArrayList<String> relationValuesList, List<AggregationOperation> aggregationOperationsList)
    {
        for(int j = 0; j< relationKeysList.size(); j++)
        {
            DocumentAggregationOperation documentAggregationOperation=new DocumentAggregationOperation(String.format(MONGO_AGGREGATION_STAGE_PIPELINE_1, relationKeysList.get(j), relationValuesList.get(j), relationValuesList.get(j), mappedArrayOfDocumentsName.get(j)));
            aggregationOperationsList.add(documentAggregationOperation);
        }
    }

    private static void extractKeyValuesList(ArrayList<String> keysList, ArrayList<String> valuesList, String filter)
    {
        String[] parts = filter.split(COMMA);
        Arrays.stream(parts).forEach(x->{
            String[] keyValue = x.split(COLON);
            keysList.add(keyValue[0].replaceAll(REGEX_PATTERN_1, EMPTY_STRING));
            valuesList.add(keyValue[1].replaceAll(REGEX_PATTERN_1, EMPTY_STRING));
        });
    }

    private WebClient checkEmptyToken(String token)
    {
        WebClient webClient;
        if (isNotEmpty(token))
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
            checkIfBothSortByAndSortOrderGivenAsInput(sortBy, sortOrder);
            WebClient webClient = checkEmptyToken(tokenUtils.getTokenFromContext());
            try
            {
                String response=checkSortByAndSortOrderAndPagination(formId, filter, sortBy, sortOrder, pageable, webClient);
                Map<String,Object> responseMap= getResponseMap(response);
                Map<String,Object> dataMap= getDataMap(responseMap);
                contentList = getContentList(dataMap);
                paginationResponsePayload.setContent(contentList);
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
        extractKeyValuesList(keysList, valuesList, filter);
        Criteria criteria = prepareCriteriaList(keysList, valuesList);
        PaginationResponsePayload paginationResponsePayload1 = getPaginationResponsePayloadIfRelationsExists(formId, relations, sortBy+";"+sortOrder, pageable, paginationResponsePayload, content, criteria);
        if (paginationResponsePayload1 != null) return paginationResponsePayload1;
        paginationResponsePayload1 = sortByAndSortOrderIsEmpty(formId, sortBy, sortOrder, pageable, paginationResponsePayload, content, criteria);
        if (paginationResponsePayload1 != null) return paginationResponsePayload1;
        checkIfBothSortByAndSortOrderGivenAsInput(sortBy, sortOrder);
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.fromString(sortOrder),sortBy));
        long totalMatchedRecords= getCount(formId, query);
        query.with(pageable);
        List<FormDataDefinition> formDataDefinitionsList= getFormDataDefinitionsList(formId, query);
        int totalPages = getTotalPages(pageable, totalMatchedRecords);
        prepareContentList(content, formDataDefinitionsList);
        setPaginationResponsePayload(paginationResponsePayload, content, totalMatchedRecords, totalPages);
        return paginationResponsePayload;
    }

    private List<FormDataDefinition> getFormDataDefinitionsList(String formId, Query query) {
        return mongoTemplate.find(query, FormDataDefinition.class, TP_RUNTIME_FORM_DATA + formId);
    }

    private long getCount(String formId, Query query) {
        return mongoTemplate.count(query, TP_RUNTIME_FORM_DATA + formId);
    }

    private static Criteria prepareCriteriaList(ArrayList<String> keysList, ArrayList<String> valuesList)
    {
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
        Criteria criteria=new Criteria();
        return criteria.andOperator(c1.toArray(new Criteria[0]));
    }

    private PaginationResponsePayload getPaginationResponsePayloadIfRelationsExists(String formId, String relations, String sort, Pageable pageable, PaginationResponsePayload paginationResponsePayload, List<Map<String, Object>> content, Criteria criteria)
    {
        String sortBy=EMPTY_STRING;
        String sortOrder=EMPTY_STRING;
        if(sort.split(";").length!=0)
        {
            sortBy=sort.split(";")[0];
            sortOrder=sort.split(";")[1];
        }
        if (isNotEmpty(relations))
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
            if (paginationResponsePayload1!=null) return paginationResponsePayload1;
            FacetOperation facetOperation=Aggregation.facet(Aggregation.count().as(COUNT)).as(METADATA).and(Aggregation.sort(Sort.by(Sort.Direction.fromString(sortOrder), sortBy)),
                    Aggregation.skip(pageable.getOffset()),Aggregation.limit(pageable.getPageSize())).as(DATA);
            aggregationOperationsList.add(facetOperation);
            List<Document> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA + formId, Document.class).getMappedResults();
            Map<String,Object> dataMap=aggregateList.get(0);
            List<Map<String,Object>> metaDataList= getMetaDataList(dataMap);
            List<Map<String,Object>> dataList= getDataList(dataMap);
            prepareContentListFromData(content, dataList);
            Map<String,Object> metaData = new HashMap<>();
            metaData = getMetaDataMap(metaDataList, metaData);
            long totalMatchedRecords;
            totalMatchedRecords = extractCountOfMatchedRecords(metaData);
            int totalPages = getTotalPages(pageable, totalMatchedRecords);
            setPaginationResponsePayload(paginationResponsePayload, content, totalMatchedRecords, totalPages);
            return paginationResponsePayload;
        }
        return null;
    }

    private static List<Map<String, Object>> getDataList(Map<String, Object> dataMap)
    {
        return (List<Map<String, Object>>) dataMap.get(DATA);
    }

    private String checkSortByAndSortOrderAndPagination(String formId, String filter, String sortBy, String sortOrder, Pageable pageable, WebClient webClient)
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

    private PaginationResponsePayload sortByAndSortOrderIsEmpty(String formId, String sortBy, String sortOrder, Pageable pageable, PaginationResponsePayload paginationResponsePayload, List<Map<String, Object>> content, Criteria criteria)
    {
        if (isEmpty(sortBy) && isEmpty(sortOrder))
        {
            Query query=new Query();
            query.addCriteria(criteria);
            setQuery(query);
            long totalMatchedRecords= getCount(formId, query);
            query.with(pageable);
            List<FormDataDefinition> formDataDefinitionsList = getFormDataDefinitionsList(formId, query);
            int totalPages = getTotalPages(pageable, totalMatchedRecords);
            prepareContentList(content, formDataDefinitionsList);
            setPaginationResponsePayload(paginationResponsePayload, content, totalMatchedRecords, totalPages);
            return paginationResponsePayload;
        }
        return null;
    }

    private static int getTotalPages(Pageable pageable, long totalMatchedRecords) {
        return (int) Math.ceil((float) totalMatchedRecords / pageable.getPageSize());
    }

    private static void prepareContentList(List<Map<String, Object>> content, List<FormDataDefinition> formDataDefinitionsList)
    {
        ObjectMapper objectMapper1=new ObjectMapper();
        formDataDefinitionsList.forEach(x-> content.add(objectMapper1.convertValue(x,Map.class)));
    }

    private static long extractCountOfMatchedRecords(Map<String, Object> metaData)
    {
        if(metaData !=null)
        {
            return Long.parseLong(String.valueOf(metaData.get(COUNT)));
        }
        else return 0;
    }

    private static void prepareRelationList(ArrayList<String> mappedArrayOfDocumentsName, String[] relationsList, ArrayList<String> relationKeysList, ArrayList<String> relationValuesList)
    {
        Arrays.stream(relationsList).forEach(x->{
            String[] keyValuePair= x.split(COLON);
            mappedArrayOfDocumentsName.add(keyValuePair[0]);
            keyValuePair[0] = TP_RUNTIME_FORM_DATA + keyValuePair[0];
            keyValuePair[1] = FORM_DATA + DOT + keyValuePair[1];
            relationKeysList.add(keyValuePair[0].replaceAll(REGEX_PATTERN_1, EMPTY_STRING));
            relationValuesList.add(keyValuePair[1].replaceAll(REGEX_PATTERN_1, EMPTY_STRING));
        });
    }

    private static void prepareContentListFromData(List<Map<String, Object>> content, List<Map<String, Object>> dataList)
    {
        dataList.forEach(x->{
            x.put(ID,String.valueOf(x.get(UNDERSCORE_ID)));
            x.remove(UNDERSCORE_ID);
            content.add(x);
        });
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
        if (!relationalMapList1.isEmpty()) return relationalMapList1;
        Query query = new Query();
        String searchString;
        searchString = checkValueOfQ(q);
        List<FormDataResponseSchema> formDataResponseSchemasList = new ArrayList<>();
        List<FormDataResponseSchema> formDataResponseSchemasList1 = ifSortEmpty(formId, sortBy, sortOrder, query, searchString, formDataResponseSchemasList);
        if (!formDataResponseSchemasList1.isEmpty()) return formDataResponseSchemasList1;
        checkIfBothSortByAndSortOrderGivenAsInput(sortBy, sortOrder);
        List<FormDataDefinition> formDataDefinitionsList;
        if(isNotEmpty(searchString))
        {
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where(UNDERSCORE_ID).is(searchString),
                    Criteria.where(VERSION).is(searchString),
                    Criteria.where(CREATED_ON).is(searchString),
                    Criteria.where(CREATED_BY_ID).is(searchString),
                    Criteria.where(CREATED_ON).is(searchString),
                    Criteria.where(UPDATED_BY_ID).is(searchString),
                    Criteria.where(UPDATED_ON).is(searchString)));
        }
        if(isNotEmpty(sortBy)&& isNotEmpty(sortOrder))
        {
            query.with(Sort.by(Sort.Direction.fromString(sortOrder), sortBy));
        }
        setQuery(query);
        formDataDefinitionsList = getFormDataDefinitionsList(formId, query);
        prepareFormDataResponseSchemaList(formDataResponseSchemasList, formDataDefinitionsList);
        return formDataResponseSchemasList;
    }

    private List<FormDataResponseSchema> ifSortEmpty(String formId, String sortBy, String sortOrder, Query query, String searchString, List<FormDataResponseSchema> formDataResponseSchemasList)
    {
        if(isEmpty(sortBy) && isEmpty(sortOrder))
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
            setQuery(query);
            formDataDefinitionsList = getFormDataDefinitionsList(formId, query);
            prepareFormDataResponseSchemaList(formDataResponseSchemasList, formDataDefinitionsList);
            return formDataResponseSchemasList;
        }
        return Collections.emptyList();
    }

    private List<Map<String, Object>> checkIfRelationsExists(String formId, String relations, String sortBy, String sortOrder)
    {
        if (isNotEmpty(relations))
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
            if (!relationalMapList1.isEmpty()) return relationalMapList1;
            aggregationOperationsList.add(Aggregation.sort(Sort.by(Sort.Direction.fromString(sortOrder), sortBy)));
            List<Document> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA + formId, Document.class).getMappedResults();
            prepareRelationsMap(relationalMapList, aggregateList);
            return relationalMapList;
        }
        return Collections.emptyList();
    }

    private static boolean checkFormDataList(List<Map<String, Object>> formDataList)
    {
        return !formDataList.isEmpty();
    }

    private static void prepareFormDataResponseSchemaList(List<FormDataResponseSchema> formDataResponseSchemasList, List<FormDataDefinition> formDataDefinitionsList)
    {
        formDataDefinitionsList.forEach(x->{
            FormDataResponseSchema formDataResponseSchema=new FormDataResponseSchema(
                    x.getId(),x.getFormData(),x.getFormMetaData(),String.valueOf(x.getVersion()),x.getCreatedById(),
                    x.getCreatedOn(),x.getUpdatedById(),x.getUpdatedOn()
            );
            formDataResponseSchemasList.add(formDataResponseSchema);
        });
    }


    private static void prepareRelationsMap(List<Map<String, Object>> relationalMapList, List<Document> aggregateList)
    {
        aggregateList.forEach(x->{
            x.put(ID,String.valueOf(x.get(UNDERSCORE_ID)));
            x.remove(UNDERSCORE_ID);
            relationalMapList.add(x);
        });
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
        WebClient webClient=checkEmptyToken(tokenUtils.getTokenFromContext());
        try
        {
            extractFromElastic(formId, q, sortBy, sortOrder, webClient);
        }
        catch (Exception e)
        {
            throw new InvalidInputException(e.getMessage(), globalMessageSource.get(e.getMessage()));
        }
        return responseList;
    }

    private void extractFromElastic(String formId, String q, String sortBy, String sortOrder, WebClient webClient) throws JsonProcessingException
    {
        String response =EMPTY_STRING;
        if (isEmpty(q) && isBlank(sortBy) && isBlank(sortOrder))
        {
            response = webClientWrapper.webclientRequest(webClient, gatewayApi + ELASTIC_VERSION1 + PARAM_INDEX_NAME + TP_RUNTIME_FORM_DATA + formId + AND_SOURCE + elasticSource, GET, null);
            logger.info(response);
        }
        if (isEmpty(q) && isNotBlank(sortBy) && isNotBlank(sortOrder))
        {
            response = webClientWrapper.webclientRequest(webClient, gatewayApi + ELASTIC_VERSION1 + PARAM_SORT_BY + sortBy + AND_SORT_ORDER + sortOrder + AND_INDEX_NAME + TP_RUNTIME_FORM_DATA + formId + AND_SOURCE + elasticSource, GET, null);
        }
        if (isNotEmpty(q) && isBlank(sortBy) && isBlank(sortOrder))
        {
                response = webClientWrapper.webclientRequest(webClient, gatewayApi + ELASTIC_VERSION1 + PARAM_Q + q + AND_INDEX_NAME + TP_RUNTIME_FORM_DATA + formId + AND_SOURCE + elasticSource, GET, null);
                logger.info(response);
        }
        if (isNotEmpty(q) && isNotBlank(sortBy) && isNotBlank(sortOrder))
        {
            response = webClientWrapper.webclientRequest(webClient, gatewayApi + ELASTIC_VERSION1 + PARAM_SORT_BY + sortBy + AND_SORT_ORDER + sortOrder + AND_Q + q + AND_INDEX_NAME + TP_RUNTIME_FORM_DATA + formId + AND_SOURCE + elasticSource, GET, null);
        }
        Map<String, Object> responseMap = getResponseMap(response);
        Map<String, Object> dataMap = getDataMap(responseMap);
        getContentList(dataMap);
    }

    @Override
    public PaginationResponsePayload getAllFormDataByFormIdAndQ(String formId, String relations, String q, String sortBy, String sortOrder, Pageable pageable)
    {
        PaginationResponsePayload paginationResponsePayload = new PaginationResponsePayload();
        PaginationResponsePayload paginationResponse = getPaginationWithElasticAndNoRelations(formId, relations, q, sortBy, sortOrder, pageable, paginationResponsePayload);
        if (paginationResponse!=null) return paginationResponse;
        checkMongoCollectionIfExistsOrNot(formId);
        paginationResponsePayload.setPage(pageable.getPageNumber());
        paginationResponsePayload.setSize(pageable.getPageSize());
        List<Map<String, Object>> content = new ArrayList<>();
        PaginationResponsePayload paginationResponsePayload1 = getPaginationWithMongoAndRelations(formId, relations, sortBy, sortOrder, pageable, paginationResponsePayload, content);
        if (paginationResponsePayload1!=null) return paginationResponsePayload1;
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC, CREATED_ON));
        long totalMatchedRecords= getCount(formId, query);
        query.with(pageable);
        List<FormDataDefinition> formDataDefinitionsList = getFormDataDefinitionsList(formId, query);
        int totalPages = getTotalPages(pageable, totalMatchedRecords);
        prepareContentList(content, formDataDefinitionsList);
        setPaginationResponsePayload(paginationResponsePayload, content, totalMatchedRecords, totalPages);
        return paginationResponsePayload;
    }

    private void checkIfBothSortByAndSortOrderGivenAsInput(String sortBy, String sortOrder)
    {
        if (isEmpty(sortBy) && !isEmpty(sortOrder) || !isEmpty(sortBy) && isEmpty(sortOrder))
        {
            throw new InvalidInputException(SORTBY_AND_SORTORDER_BOTH_SHOULD_BE_GIVEN, globalMessageSource.get(SORTBY_AND_SORTORDER_BOTH_SHOULD_BE_GIVEN));
        }
    }

    private PaginationResponsePayload getPaginationWithMongoAndRelations(String formId, String relations, String sortBy, String sortOrder, Pageable pageable, PaginationResponsePayload paginationResponsePayload, List<Map<String, Object>> content)
    {
        if (isNotEmpty(relations))
        {
            ArrayList<String> mappedArrayOfDocumentsName=new ArrayList<>();
            String[] relationsList = relations.split(COMMA);
            ArrayList<String> relationKeysList = new ArrayList<>();
            ArrayList<String> relationValuesList = new ArrayList<>();
            prepareRelationList(mappedArrayOfDocumentsName, relationsList, relationKeysList, relationValuesList);
            List<AggregationOperation> aggregationOperationsList = new ArrayList<>();
            prepareDocumentAggregateList(mappedArrayOfDocumentsName, relationKeysList, relationValuesList, aggregationOperationsList);
            PaginationResponsePayload paginationResponsePayload1 = getPaginationWithMongoAndEmptySort(formId, sortBy, sortOrder, pageable, paginationResponsePayload, content, aggregationOperationsList);
            if (paginationResponsePayload1!=null) return paginationResponsePayload1;
            FacetOperation facetOperation=Aggregation.facet(Aggregation.count().as(COUNT)).as(METADATA).and(Aggregation.sort(Sort.by(Sort.Direction.fromString(sortOrder), sortBy)),
                    Aggregation.skip(pageable.getOffset()),Aggregation.limit(pageable.getPageSize())).as(DATA);
            aggregationOperationsList.add(facetOperation);
            List<Document> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA + formId, Document.class).getMappedResults();
            Map<String,Object> dataMap=aggregateList.get(0);
            List<Map<String,Object>> metaDataList= getMetaDataList(dataMap);
            List<Map<String,Object>> dataList= getDataList(dataMap);
            prepareContentListFromData(content, dataList);
            Map<String,Object> metaData = new HashMap<>();
            metaData = getMetaDataMap(metaDataList, metaData);
            long totalMatchedRecords;
            totalMatchedRecords = extractCountOfMatchedRecords(metaData);
            int totalPages = getTotalPages(pageable, totalMatchedRecords);
            setPaginationResponsePayload(paginationResponsePayload, content, totalMatchedRecords, totalPages);
            return paginationResponsePayload;
        }
        return null;
    }

    private static List<Map<String, Object>> getMetaDataList(Map<String, Object> dataMap) {
        return (List<Map<String, Object>>) dataMap.get(METADATA);
    }

    private PaginationResponsePayload getPaginationWithMongoAndEmptySort(String formId, String sortBy, String sortOrder, Pageable pageable, PaginationResponsePayload paginationResponsePayload, List<Map<String, Object>> content, List<AggregationOperation> aggregationOperationsList)
    {
        if (isEmpty(sortBy) && isEmpty(sortOrder))
        {
            FacetOperation facetOperation=Aggregation.facet(Aggregation.count().as(COUNT)).as(METADATA).and(Aggregation.sort(Sort.by(Sort.Direction.DESC, CREATED_ON)),
                    Aggregation.skip(pageable.getOffset()),Aggregation.limit(pageable.getPageSize())).as(DATA);
            aggregationOperationsList.add(facetOperation);
            List<Document> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA + formId, Document.class).getMappedResults();
            Map<String,Object> dataMap=aggregateList.get(0);
            List<Map<String,Object>> metaDataList= getMetaDataList(dataMap);
            List<Map<String,Object>> dataList= getDataList(dataMap);
            prepareContentListFromData(content, dataList);
            Map<String,Object> metaData = new HashMap<>();
            metaData = getMetaDataMap(metaDataList, metaData);
            long totalMatchedRecords = extractCountOfMatchedRecords(metaData);
            int totalPages = getTotalPages(pageable, totalMatchedRecords);
            setPaginationResponsePayload(paginationResponsePayload, content, totalMatchedRecords, totalPages);
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
        checkIfBothSortByAndSortOrderGivenAsInput(sortBy,sortOrder);
        WebClient webClient=checkEmptyToken( tokenUtils.getTokenFromContext());
        try
        {
            String response = getString(formId, q, sortBy, sortOrder, pageable, webClient);
            Map<String,Object> responseMap= getResponseMap(response);
            Map<String,Object> dataMap= getDataMap(responseMap);
            contentList = getContentList(dataMap);
            paginationResponsePayload.setContent(contentList);
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

    private String getString(String formId, String q, String sortBy, String sortOrder, Pageable pageable, WebClient webClient)
    {
        String response=EMPTY_STRING;
        if (isEmpty(q) && isBlank(sortBy) && isBlank(sortOrder))
        {
            response =webClientWrapper.webclientRequest(webClient,gatewayApi +ELASTIC_VERSION1+PARAM_PAGE+ pageable.getPageNumber()+AND_SIZE+ pageable.getPageSize()+AND_INDEX_NAME+ TP_RUNTIME_FORM_DATA + formId +AND_SOURCE+elasticSource,GET,null);
            logger.info(response);
        }
        if(isEmpty(q)&&isNotBlank(sortBy)&&isNotBlank(sortOrder))
        {
            response =webClientWrapper.webclientRequest(webClient,gatewayApi +ELASTIC_VERSION1+PARAM_SORT_BY+ sortBy +AND_SORT_ORDER+ sortOrder +AND_PAGE+ pageable.getPageNumber()+AND_SIZE+ pageable.getPageSize()+AND_INDEX_NAME+ TP_RUNTIME_FORM_DATA + formId +AND_SOURCE+elasticSource,GET,null);
        }
        if(isNotEmpty(q)&& isBlank(sortBy)&& isBlank(sortOrder))
        {
                response =webClientWrapper.webclientRequest(webClient,gatewayApi +ELASTIC_VERSION1+PARAM_Q+ q +AND_PAGE+ pageable.getPageNumber()+AND_SIZE+ pageable.getPageSize()+AND_INDEX_NAME+ TP_RUNTIME_FORM_DATA + formId +AND_SOURCE+elasticSource,GET,null);
                logger.info(response);
        }
        if(isNotEmpty(q)&&isNotBlank(sortBy)&&isNotBlank(sortOrder))
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
        Pageable pageable = getPageable(paginationResponsePayload);
        if (isNotEmpty(relations))
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
            List<Map<String,Object>> metaDataList= getMetaDataList(dataMap);
            List<Map<String,Object>> dataList= getDataList(dataMap);
            prepareContentListFromData(content, dataList);
            Map<String,Object> metaData = new HashMap<>();
            metaData = getMetaDataMap(metaDataList, metaData);
            long totalMatchedRecords;
            totalMatchedRecords = extractCountOfMatchedRecords(metaData);
            int totalPages = getTotalPages(pageable, totalMatchedRecords);
            setPaginationResponsePayload(paginationResponsePayload, content, totalMatchedRecords, totalPages);
            return paginationResponsePayload;
        }
        Query query = new Query();
        List<FormDataDefinition> formDataDefinitionsList;
        setQuery(query);
        long totalMatchedRecords= getCount(formId, query);
        query.with(pageable);
        formDataDefinitionsList = getFormDataDefinitionsList(formId, query);
        int totalPages = getTotalPages(pageable, totalMatchedRecords);
        prepareContentList(content, formDataDefinitionsList);
        paginationResponsePayload.setPage(0);
        paginationResponsePayload.setSize(defaultPageLimit);
        setPaginationResponsePayload(paginationResponsePayload, content, totalMatchedRecords, totalPages);
        return paginationResponsePayload;
    }

    private static void setQuery(Query query) {
        query.with(Sort.by(Sort.Direction.DESC, CREATED_ON));
    }

    private Pageable getPageable(PaginationResponsePayload paginationResponsePayload)
    {
        Pageable pageable = PageRequest.of(0, defaultPageLimit);
        paginationResponsePayload.setPage(pageable.getPageNumber());
        paginationResponsePayload.setSize(pageable.getPageSize());
        return pageable;
    }

    private static void setPaginationResponsePayload(PaginationResponsePayload paginationResponsePayload, List<Map<String, Object>> content, long totalMatchedRecords, int totalPages)
    {
        paginationResponsePayload.setContent(content);
        paginationResponsePayload.setTotalPages(totalPages);
        paginationResponsePayload.setTotalElements(totalMatchedRecords);
        paginationResponsePayload.setNumberOfElements(content.size());
    }

    private PaginationResponsePayload getPaginationResponsePayload(String formId, PaginationResponsePayload paginationResponsePayload)
    {
        List<Map<String,Object>> contentList;
        WebClient webClient= checkEmptyToken(tokenUtils.getTokenFromContext());
        try
        {
            String response= webClientWrapper.webclientRequest(webClient, gatewayApi + ELASTIC_VERSION1 + PARAM_INDEX_NAME + TP_RUNTIME_FORM_DATA + formId + AND_SOURCE + elasticSource + AND_PAGE_AND_SIZE + defaultPageLimit, GET, null);
            logger.info(response);
            Map<String, Object> responseMap = getResponseMap(response);
            Map<String, Object> dataMap = getDataMap(responseMap);
            contentList = getContentList(dataMap);
            paginationResponsePayload.setContent(contentList);
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
        List<Map<String,Object>> responseList=new ArrayList<>();
        if (elasticEnable&&relations==null)
        {
            WebClient webClient;
            String token = tokenUtils.getTokenFromContext();
            webClient =checkEmptyToken(token);
            try
            {
                String response= webClientWrapper.webclientRequest(webClient, gatewayApi + ELASTIC_VERSION1 + SLASH + id + PARAM_INDEX_NAME + TP_RUNTIME_FORM_DATA + formId, GET, null);
                logger.info(response);
                Map<String,Object> responseMap = getResponseMap(response);
                Map<String,Object> dataMap = getDataMap(responseMap);
                responseList.add(dataMap);
            }
            catch (Exception e)
            {
                throw new InvalidInputException(e.getMessage(), globalMessageSource.get(e.getMessage()));
            }
            return responseList;
        }
        if (isNotEmpty(relations))
        {
            return getFormDataList(formId, id, relations);
        }
        Bson filter = Filters.eq(UNDERSCORE_ID, Long.valueOf(id));
        checkMongoCollectionIfExistsOrNot(formId);
        try
        {
            if(mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA + formId).find(filter).iterator().hasNext())
            {
                Document document=mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA + formId).find(filter).iterator().next();
                responseList.add(document);
            }
        }
        catch(Exception e)
        {
            throw new InvalidInputException(e.getMessage(), globalMessageSource.get(e.getMessage()));
        }
        return  responseList;
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
            WebClient webClient= checkEmptyToken(tokenUtils.getTokenFromContext());
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
        Bson filter= Filters.eq(UNDERSCORE_ID,id);
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
            WebClient webClient=checkEmptyToken(tokenUtils.getTokenFromContext());
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
        List<AggregationOperation> aggregationOperationsList = new ArrayList<>();
        if(isNotEmpty(filter))
        {
            createMultipleFilterCriteria(filter, criteria, aggregationOperationsList);
        }
        if(operation.equals(COUNT))
        {
            aggregationOperationsList.add(Aggregation.group(groupBy).count().as(COUNT));
        }
       List<Document> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList),
               TP_RUNTIME_FORM_DATA + formId, Document.class).getMappedResults();
        List<Map<String,String>> responseAggregationList=new ArrayList<>();
        aggregateList.forEach(x->{
            Map<String,String> aggregationMap=new HashMap<>();
            aggregationMap.put(UNDERSCORE_ID,String.valueOf(x.get(UNDERSCORE_ID)));
            aggregationMap.put(COUNT,String.valueOf(x.get(COUNT)));
            responseAggregationList.add(aggregationMap);
        });
        return new AggregationResponse(responseAggregationList);
    }

    private static void createMultipleFilterCriteria(String filter, Criteria criteria, List<AggregationOperation> aggregationOperationsList)
    {
        ArrayList<String> keysList = new ArrayList<>();
        ArrayList<String> valuesList = new ArrayList<>();
        extractKeyValuesList(keysList, valuesList, filter);
        prepareCriteriaList(keysList, valuesList);
        aggregationOperationsList.add(Aggregation.match(criteria));
    }
}
