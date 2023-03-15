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
import com.techsophy.tsf.runtime.form.entity.Status;
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
import java.util.stream.Collectors;
import static com.techsophy.tsf.runtime.form.constants.ErrorConstants.*;
import static com.techsophy.tsf.runtime.form.constants.FormDataConstants.CONTAINS_ONLY_NUMBER;
import static com.techsophy.tsf.runtime.form.constants.FormDataConstants.E11000;
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

    private void handleMongoException(Exception e) {
        boolean exist = e.getMessage().contains(E11000);
        if (exist) {
            throw new InvalidInputException(DUPLICATE_FIELD_VALUE, globalMessageSource.get(DUPLICATE_FIELD_VALUE));
        } else {
            throw new InvalidInputException(DUPLICATE_FIELD_VALUE, e.getMessage());
        }
    }
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
                    try {
                        mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA +formId).updateOne(filter,Updates.combine(
                                Updates.set(FORM_DATA,formDataDefinition.getFormData()),Updates.set(VERSION,version)));
                    }catch(Exception e)
                    {
                        handleMongoException(e);
                    }
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
            if(formResponseSchema.getElasticPush().equals(Status.ENABLED))
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
        try{
            mongoTemplate.save(formDataDefinition, TP_RUNTIME_FORM_DATA + formId);
        }catch(Exception e){
            handleMongoException(e);
        }

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
        if (uniqueDocumentId==null|| StringUtils.isEmpty(uniqueDocumentId))
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
        try {
            mongoTemplate.getCollection(TP_RUNTIME_FORM_DATA + formId).updateOne(filter, Updates.combine(
                    Updates.set(FORM_DATA, formData), Updates.set(VERSION, version), Updates.set(FORM_META_DATA, formMetaData)));
        }catch(Exception e)
        {
            handleMongoException(e);

        }
        FormDataAuditSchema formDataAuditSchema = getFormDataAuditSchema(formId, version, formDataDefinition);
        this.formDataAuditService.saveFormDataAudit(formDataAuditSchema);
        FormResponseSchema formResponseSchema = formService.getRuntimeFormById(formId);
        if (formResponseSchema.getElasticPush().equals(Status.ENABLED))
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
        checkMongoCollectionIfExistsOrNot(formId);
        Criteria criteria=getCriteria(filter);
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
        return getFormDataResponseSchemasSort(formId, sortBy, sortOrder,criteria);
    }

    public Criteria getCriteria(String filter)
    {
        if(!filter.startsWith("{"))
        {
            return new Criteria().andOperator(Arrays.stream(filter.split(COMMA))
                    .map(x -> x.split(COLON))
                    .collect(Collectors.toMap(
                            keyValue -> keyValue[0].replaceAll(REGEX_PATTERN_1, EMPTY_STRING),
                            keyValue -> keyValue[1].replaceAll(REGEX_PATTERN_1, EMPTY_STRING)
                    ))
                    .entrySet()
                    .stream()
                    .map(entry -> entry.getKey().equals(ID) || entry.getValue().matches(CONTAINS_ONLY_NUMBER)
                            ? Criteria.where(entry.getKey()).is(Long.parseLong(entry.getValue()))
                            : Criteria.where(entry.getKey()).is(entry.getValue()))
                    .collect(Collectors.toList()));
        }
        else
        {
            try {
                return new Criteria().andOperator(
                        new ObjectMapper()
                                .readValue("{\"operations\":"+filter+"}", com.techsophy.tsf.runtime.form.dto.Filters.class)
                                .getOperations()
                                .entrySet()
                                .stream()
                                .map(entry->entry.getValue().getCriteria(entry.getKey()))
                                .collect(Collectors.toList()));
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Invalid filter: " + filter, e);
            }
        }
    }

    private List<FormDataResponseSchema> getFormDataResponseSchemasSort(String formId, String sortBy, String sortOrder,Criteria criteria)
    {
        Query query=new Query(criteria);
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
        checkMongoCollectionIfExistsOrNot(formId);
        List<Map<String, Object>> content = new ArrayList<>();
        paginationResponsePayload.setPage(pageable.getPageNumber());
        paginationResponsePayload.setSize(pageable.getPageSize());
        Query query = new Query();
        Criteria criteria=getCriteria(filter);
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
            FacetOperation facetOperation;
            if(sortBy.equals("null")||sortOrder.equals("null"))
            {
                facetOperation=Aggregation.facet(Aggregation.count().as(COUNT)).as(METADATA)
                        .and(Aggregation.sort(Sort.by(Sort.Direction.DESC, CREATED_ON)),
                                Aggregation.skip(pageable.getOffset()),Aggregation.limit(pageable.getPageSize())).as(DATA);
            }
            else
            {
                facetOperation=Aggregation.facet(Aggregation.count().as(COUNT)).as(METADATA)
                        .and(Aggregation.sort(Sort.by(Sort.Direction.fromString(sortOrder), sortBy)),
                                Aggregation.skip(pageable.getOffset()),Aggregation.limit(pageable.getPageSize())).as(DATA);
            }
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
        else {
            setQuery(query);
        }
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
        if(q !=null)
        {
             return URLDecoder.decode(q,StandardCharsets.UTF_8);
        }
        return EMPTY_STRING;
    }

    @Override
    public PaginationResponsePayload getAllFormDataByFormIdAndQ(String formId, String relations, String q, String sortBy, String sortOrder, Pageable pageable)
    {
        PaginationResponsePayload paginationResponsePayload = new PaginationResponsePayload();
        checkMongoCollectionIfExistsOrNot(formId);
        paginationResponsePayload.setPage(pageable.getPageNumber());
        paginationResponsePayload.setSize(pageable.getPageSize());
        List<Map<String, Object>> content = new ArrayList<>();
        PaginationResponsePayload paginationResponsePayload1 = getPaginationWithMongoAndRelations(formId, relations, sortBy, sortOrder, pageable, paginationResponsePayload, content);
        if (paginationResponsePayload1!=null) return paginationResponsePayload1;
        Query query = new Query();
        if(q!=null&&!q.isEmpty()) {
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
        }
        if(isNotEmpty(sortBy)&&isNotEmpty(sortOrder))
        {
            query.with(Sort.by(Sort.Direction.fromString(sortOrder),sortBy));
        }
        else {
            setQuery(query);
        }
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

    public PaginationResponsePayload getAllFormDataByFormId(String formId,String relations)
    {
        PaginationResponsePayload paginationResponsePayload = new PaginationResponsePayload();
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

    @Override
    public List getFormDataByFormIdAndId(String formId, String id,String relations)
    {
        List<Map<String,Object>> responseList=new ArrayList<>();
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
        FormResponseSchema formResponseSchema = formService.getRuntimeFormById(formId);
        if(formResponseSchema.getElasticPush().equals(Status.ENABLED))
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
        FormResponseSchema formResponseSchema = formService.getRuntimeFormById(formId);
        if(formResponseSchema.getElasticPush().equals(Status.ENABLED))
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
    public AggregationResponse aggregateByFormIdFilterGroupBy(String formId, String filter, String groupBy, String operation) {
        checkMongoCollectionIfExistsOrNot(formId);
        List<AggregationOperation> aggregationOperationsList = new ArrayList<>();
        if(isNotEmpty(filter))
        {
            createMultipleFilterCriteria(filter,aggregationOperationsList);
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

    private void createMultipleFilterCriteria(String filter,List<AggregationOperation> aggregationOperationsList)
    {
        Criteria criteria=getCriteria(filter);
        aggregationOperationsList.add(Aggregation.match(criteria));
    }
}
