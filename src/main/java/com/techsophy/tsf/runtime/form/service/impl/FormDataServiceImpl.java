package com.techsophy.tsf.runtime.form.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.DeleteResult;
import com.techsophy.idgenerator.IdGeneratorImpl;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.*;
import com.techsophy.tsf.runtime.form.entity.FormDataDefinition;
import com.techsophy.tsf.runtime.form.exception.EntityIdNotFoundException;
import com.techsophy.tsf.runtime.form.exception.FormIdNotFoundException;
import com.techsophy.tsf.runtime.form.exception.InvalidInputException;
import com.techsophy.tsf.runtime.form.service.FormDataAuditService;
import com.techsophy.tsf.runtime.form.service.FormDataService;
import com.techsophy.tsf.runtime.form.service.FormService;
import com.techsophy.tsf.runtime.form.service.MongoQueryBuilder;
import com.techsophy.tsf.runtime.form.utils.DocumentAggregationOperation;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import com.techsophy.tsf.runtime.form.utils.UserDetails;
import com.techsophy.tsf.runtime.form.utils.WebClientWrapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
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
import static com.techsophy.tsf.runtime.form.constants.FormDataConstants.*;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.DATA;
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
    private GlobalMessageSource globalMessageSource = null;
    private IdGeneratorImpl idGenerator = null;
    private WebClientWrapper webClientWrapper = null;
    private TokenUtils tokenUtils = null;
    private ObjectMapper objectMapper = null;
    private FormDataAuditService formDataAuditService = null;
    private static final Logger logger = LoggerFactory.getLogger(FormDataServiceImpl.class);
    private FormService formService = null;
    private FormValidationServiceImpl formValidationServiceImpl;
    private UserDetails userDetails;
    private MongoQueryBuilder queryBuilder;

    private void handleMongoException(Exception e) {
        boolean exist = e.getMessage().contains(E11000);
        if (exist) {
            String msg = e.getMessage();
            int startIndex = msg.indexOf(INDEX)+INDEX.length();
            int endIndex = msg.indexOf(DUP_KEY);
            String extractedString = msg.substring(startIndex, endIndex).trim();
            throw new InvalidInputException(DUPLICATE_FIELD_VALUE, globalMessageSource.get(DUPLICATE_FIELD_VALUE,extractedString));
        }
        else {
            throw new InvalidInputException(DUPLICATE_FIELD_VALUE, e.getMessage());
        }
    }

    @Override
    public FormDataDefinition saveFormData(FormDataSchema formDataSchema, String filter, String aclFilter) throws IOException
    {
        String formId=formDataSchema.getFormId();
        FormResponseSchema formResponseSchema = formService.getRuntimeFormById(formId);
        List<ValidationResult> validationResultList= formValidationServiceImpl.validateData(formResponseSchema,formDataSchema,formId);
        StringBuilder completeMessage= new StringBuilder();
        validationResultList.stream().filter(v->!v.isValid()).forEach(v->{
            completeMessage.append(v.getErrorCode()).append(SEMICOLON).append(v.getErrorMessage(globalMessageSource)).append(SEMICOLON);
            throw new InvalidInputException(String.valueOf(completeMessage),String.valueOf(completeMessage));
        });
        FormDataDefinition formDataDefinition = getFormDataDefinition(formDataSchema);
        Map<String, Object> loggedInUserDetails = userDetails.getUserDetails().get(0);
        BigInteger loggedInUserId = BigInteger.valueOf(Long.parseLong(loggedInUserDetails.get(ID).toString()));
        formDataDefinition.setUpdatedOn(String.valueOf(Instant.now()));
        formDataDefinition.setUpdatedById(String.valueOf(loggedInUserId));
        if(formDataDefinition.getId()==null)
        {
            formDataDefinition.setId(String.valueOf(idGenerator.nextId()));
            formDataDefinition.setVersion(1);
            formDataDefinition.setCreatedById(String.valueOf(loggedInUserId));
            formDataDefinition.setCreatedOn(String.valueOf(Instant.now()));
        }
        else
        {
            FormDataDefinition existingFormDataDefinition=mongoTemplate.findOne(getQuery(formDataSchema, filter, aclFilter),FormDataDefinition.class,TP_RUNTIME_FORM_DATA + formId);
            if(existingFormDataDefinition==null)
            {
                throw new InvalidInputException(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC,globalMessageSource.get(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC,formDataSchema.getId()));
            }
            formDataDefinition.setCreatedById(existingFormDataDefinition.getCreatedById());
            formDataDefinition.setCreatedOn(existingFormDataDefinition.getCreatedOn());
            formDataDefinition.setVersion(existingFormDataDefinition.getVersion()+1);
        }
        saveToMongo(formId,formDataDefinition);
        FormDataAuditSchema formDataAuditSchema=new FormDataAuditSchema(
                    String.valueOf(idGenerator.nextId()),formDataDefinition.getId(),
                    formId,1,formDataDefinition.getFormData(),formDataDefinition.getFormMetaData()
            );
        this.formDataAuditService.saveFormDataAudit(formDataAuditSchema);
        return formDataDefinition;
    }

    private Query getQuery(FormDataSchema formDataSchema, String filter, String aclFilter)
    {
        Query query=new Query();
        Criteria idCriteria=Criteria.where(UNDERSCORE_ID).is(formDataSchema.getId());
        Criteria filterCriteria= getCriteria(filter);
        Criteria aclFilterCriteria= getCriteria(aclFilter);
        query.addCriteria(idCriteria.andOperator(filterCriteria, aclFilterCriteria));
        return query;
    }

    private void saveToMongo(String formId, FormDataDefinition formDataDefinition) {
        try{
            mongoTemplate.save(formDataDefinition, TP_RUNTIME_FORM_DATA + formId);
        }catch(Exception e){
            handleMongoException(e);
        }
    }

    private FormDataDefinition getFormDataDefinition(FormDataSchema formDataSchema)
    {
      return objectMapper.convertValue(formDataSchema,FormDataDefinition.class);
    }

    @Override
    public FormDataDefinition updateFormData(FormDataSchema formDataSchema, String filter, String aclFilter)
    {
        FormDataDefinition formDataDefinition=mongoTemplate.findOne(getQuery(formDataSchema,filter,aclFilter),FormDataDefinition.class,TP_RUNTIME_FORM_DATA + formDataSchema.getFormId());
        if(formDataDefinition==null)
        {
            throw new InvalidInputException(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC,globalMessageSource.get(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC,formDataSchema.getId()));
        }
        formDataDefinition.setVersion(formDataDefinition.getVersion()+1);
        Map<String,Object> modifiedFormData=formDataDefinition.getFormData();
        modifiedFormData.putAll(formDataSchema.getFormData());
        Optional.ofNullable(formDataSchema.getFormMetaData()).ifPresent(formDataDefinition::setFormMetaData);
        saveToMongo(formDataSchema.getFormId(),formDataDefinition);
        return formDataDefinition;
    }

    @Override
    public List getAllFormDataByFormId(String formId, String relations, String filter, String sortBy, String sortOrder, String aclFilter)
    {
        checkMongoCollectionIfExistsOrNot(formId);
        Criteria andCriteria = getAndCriteria(filter, aclFilter);
        if (isNotEmpty(relations))
        {
            List<AggregationOperation> aggregationOperationsList=getAggregationOperationsList(relations);
            if(andCriteria!=null)
            {
                aggregationOperationsList.add(Aggregation.match(andCriteria));
            }
            return getMapsEmptySort(formId, sortBy, sortOrder, aggregationOperationsList);
        }
        return getFormDataResponseSchemasSort(formId, sortBy, sortOrder,andCriteria);
    }

    private static List<AggregationOperation> getAggregationOperationsList(String relations)
    {
        String[] relationsList = relations.split(COMMA);
        List<String> relationKeysList=getRelationKeysList(relationsList);
        List<String> relationValuesList=getRelationValuesList(relationsList);
        List<String> mappedArrayOfDocumentsName=getMappedArrayOfDocuments(relationsList);
        return getAggregationOperationList(mappedArrayOfDocumentsName, relationKeysList, relationValuesList);
    }

    private Criteria getAndCriteria(String filter, String aclFilter)
    {
        if(isNotBlank(filter)&&isNotBlank(aclFilter))
        {
           return new Criteria().andOperator(getCriteria(filter),getCriteria(aclFilter));
        }
        else if(isNotBlank(filter))
        {
           return getCriteria(filter);
        }
        else if(isNotBlank(aclFilter))
        {
            return getCriteria(aclFilter);
        }
       return null;
    }

    public Criteria getCriteria(String filter)
    {
        if(StringUtils.isBlank(filter))
        {
            return new Criteria();
        }
        else if(!filter.startsWith("{"))
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
                                .readValue("{\"operations\":"+filter+"}", com.techsophy.tsf.commons.query.Filters.class)
                                .getOperations()
                                .entrySet()
                                .stream()
                                .map(entry->entry.getValue().getCriteria(entry.getKey(),queryBuilder))
                                .collect(Collectors.toList()));
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Invalid filter: " + filter, e);
            }
        }
    }

    private List<FormDataResponseSchema> getFormDataResponseSchemasSort(String formId, String sortBy, String sortOrder,Criteria criteria)
    {
        Query query=new Query();
        if(criteria!=null)
        {
            query.addCriteria(criteria);
        }
        checkIfBothSortByAndSortOrderGivenAsInput(sortBy, sortOrder);
        List<FormDataDefinition> formDataDefinitionsList;
        if (isEmpty(sortBy) && isEmpty(sortOrder))
        {
            query.with(Sort.by(Sort.Direction.DESC, CREATED_ON));
            formDataDefinitionsList= getFormDataDefinitionsList(formId, query);
        }
        else
        {
            query.with(Sort.by(Sort.Direction.fromString(sortOrder), sortBy));
            formDataDefinitionsList = getFormDataDefinitionsList(formId, query);
        }
        return getFormDataResponseSchemaList(formDataDefinitionsList);
    }

    private List<Map<String, Object>> getMapsEmptySort(String formId, String sortBy, String sortOrder, List<AggregationOperation> aggregationOperationsList)
    {
        List<Document> aggregateList;
        if (isEmpty(sortBy) && isEmpty(sortOrder))
        {
            aggregationOperationsList.add(Aggregation.sort(Sort.by(Sort.Direction.DESC,CREATED_ON)));
            aggregateList=getDocumentList(formId, aggregationOperationsList);

        }
        else
        {
            aggregationOperationsList.add(Aggregation.sort(Sort.by(Sort.Direction.fromString(sortOrder), sortBy)));
            aggregateList=getDocumentList(formId, aggregationOperationsList);
        }
        return getRelationsMap(aggregateList);
    }

    private static List<AggregationOperation> getAggregationOperationList(List<String> mappedArrayOfDocumentsName, List<String> relationKeysList, List<String> relationValuesList)
    {
        List<AggregationOperation> aggregationOperationsList=new ArrayList<>();
        for(int j = 0; j< relationKeysList.size(); j++)
        {
            DocumentAggregationOperation documentAggregationOperation=new DocumentAggregationOperation(String.format(MONGO_AGGREGATION_STAGE_PIPELINE_1, relationKeysList.get(j), relationValuesList.get(j), relationValuesList.get(j), mappedArrayOfDocumentsName.get(j)));
            aggregationOperationsList.add(documentAggregationOperation);
        }
        return aggregationOperationsList;
    }

    private WebClient getWebClient(String token)
    {
        if (isNotEmpty(token))
        {
            return webClientWrapper.createWebClient(token);
        }
        else
        {
            throw new InvalidInputException(TOKEN_NOT_NULL, globalMessageSource.get(TOKEN_NOT_NULL, tokenUtils.getLoggedInUserId()));
        }
    }

    @Override
    public PaginationResponsePayload getAllFormDataByFormId(String formId, String relations, String filter, String sortBy, String sortOrder, Pageable pageable, String aclFilter)
    {
        checkMongoCollectionIfExistsOrNot(formId);
        Query query = new Query();
        Criteria andCriteria = getAndCriteria(filter, aclFilter);
        PaginationResponsePayload paginationResponsePayload = getPaginationResponsePayloadIfRelationsExists(formId, relations, sortBy+";"+sortOrder, pageable, andCriteria);
        if (paginationResponsePayload!= null) return paginationResponsePayload;
        paginationResponsePayload= sortByAndSortOrderIsEmpty(formId, sortBy, sortOrder, pageable,andCriteria);
        if (paginationResponsePayload != null) return paginationResponsePayload;
        checkIfBothSortByAndSortOrderGivenAsInput(sortBy, sortOrder);
        if(andCriteria!=null)
        {
            query.addCriteria(andCriteria);
        }
        query.with(Sort.by(Sort.Direction.fromString(sortOrder),sortBy));
        long totalMatchedRecords= getCount(formId, query);
        query.with(pageable);
        List<FormDataDefinition> formDataDefinitionsList= getFormDataDefinitionsList(formId, query);
        int totalPages = getTotalPages(pageable, totalMatchedRecords);
        List<Map<String,Object>> content=getContentList(formDataDefinitionsList);
        return getPaginationResponseFromContent(content, totalMatchedRecords, totalPages,pageable );
    }

    private List<FormDataDefinition> getFormDataDefinitionsList(String formId, Query query) {
        return mongoTemplate.find(query, FormDataDefinition.class, TP_RUNTIME_FORM_DATA + formId);
    }

    private long getCount(String formId, Query query) {
        return mongoTemplate.count(query, TP_RUNTIME_FORM_DATA + formId);
    }
    
    private PaginationResponsePayload getPaginationResponsePayloadIfRelationsExists(String formId, String relations, String sort, Pageable pageable, Criteria criteria)
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
            List<AggregationOperation> aggregationOperationsList=getAggregationOperationsList(relations);
            if(criteria!=null)
            {
                aggregationOperationsList.add(Aggregation.match(criteria));
            }
            PaginationResponsePayload paginationResponsePayload = getPaginationWithMongoAndEmptySort(formId, sortBy, sortOrder, pageable, aggregationOperationsList);
            if (paginationResponsePayload!=null) return paginationResponsePayload;
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
            List<Document> aggregateList = getDocumentList(formId, aggregationOperationsList);
            Map<String, Object> dataMap = getFirstDocument(aggregateList);
            List<Map<String,Object>> metaDataList= getMetaDataList(dataMap);
            List<Map<String,Object>> dataList= getDataList(dataMap);
            List<Map<String,Object>> content=getContentFromDataList(dataList);
            Map<String,Object> metaData = getMetaDataMap(metaDataList);
            long totalMatchedRecords=extractCountOfMatchedRecords(metaData);
            int totalPages = getTotalPages(pageable, totalMatchedRecords);
            return getPaginationResponseFromContent(content, totalMatchedRecords, totalPages,pageable );
        }
        return null;
    }

    private static Map<String, Object> getFirstDocument(List<Document> aggregateList)
    {
       return aggregateList.get(0);
    }

    private List<Document> getDocumentList(String formId, List<AggregationOperation> aggregationOperationsList)
    {
        return mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA + formId, Document.class).getMappedResults();
    }

    private static List<Map<String, Object>> getDataList(Map<String, Object> dataMap)
    {
        return (List<Map<String, Object>>) dataMap.get(DATA);
    }

    private PaginationResponsePayload sortByAndSortOrderIsEmpty(String formId, String sortBy, String sortOrder, Pageable pageable,Criteria criteria)
    {
        if (isEmpty(sortBy) && isEmpty(sortOrder))
        {
            Query query=new Query();
            if(criteria!=null)
            {
                query.addCriteria(criteria);
            }
            query.with(Sort.by(Sort.Direction.DESC, CREATED_ON));
            long totalMatchedRecords= getCount(formId, query);
            query.with(pageable);
            List<FormDataDefinition> formDataDefinitionsList = getFormDataDefinitionsList(formId, query);
            int totalPages = getTotalPages(pageable, totalMatchedRecords);
            List<Map<String,Object>> content=getContentList(formDataDefinitionsList);
            return getPaginationResponseFromContent(content, totalMatchedRecords, totalPages,pageable );
        }
        return null;
    }

    private static int getTotalPages(Pageable pageable, long totalMatchedRecords) {
        return (int) Math.ceil((float) totalMatchedRecords / pageable.getPageSize());
    }

    private static List<Map<String,Object>> getContentList(List<FormDataDefinition> formDataDefinitionsList)
    {
        List<Map<String,Object>> contentList=new ArrayList<>();
        ObjectMapper objectMapper1=new ObjectMapper();
        formDataDefinitionsList.forEach(x-> contentList.add(objectMapper1.convertValue(x,Map.class)));
        return contentList;
    }

    private static long extractCountOfMatchedRecords(Map<String, Object> metaData)
    {
        if(metaData!=null)
        {
            return Long.parseLong(String.valueOf(metaData.get(COUNT)));
        }
        else return 0;
    }

    private static List<String> getMappedArrayOfDocuments(String[] relationsList)
    {
       return Arrays.stream(relationsList).map(x->x.split(COLON)[0]).collect(Collectors.toList());
    }

    private static List<String> getRelationKeysList(String[] relationsList)
    {
       return Arrays.stream(relationsList).map(x->(TP_RUNTIME_FORM_DATA+x.split(COLON)[0]).replaceAll(REGEX_PATTERN_1, EMPTY_STRING)).collect(Collectors.toList());
    }

    private static List<String> getRelationValuesList(String[] relationsList)
    {
        return Arrays.stream(relationsList).map(x->(FORM_DATA+DOT+x.split(COLON)[1]).replaceAll(REGEX_PATTERN_1, EMPTY_STRING)).collect(Collectors.toList());
    }

    private static List<Map<String, Object>> getContentFromDataList(List<Map<String, Object>> dataList)
    {
        List<Map<String, Object>> content=new ArrayList<>();
        dataList.forEach(x->{
            x.put(ID,String.valueOf(x.get(UNDERSCORE_ID)));
            x.remove(UNDERSCORE_ID);
            content.add(x);
        });
        return content;
    }

    private static Map<String, Object> getMetaDataMap(List<Map<String, Object>> metaDataList)
    {
        if(!metaDataList.isEmpty())
        {
            return metaDataList.get(0);
        }
        return Collections.emptyMap();
    }

    public List getAllFormDataByFormIdAndQ(String formId, String relations, String q, String sortBy, String sortOrder, String aclFilter)
    {
        checkMongoCollectionIfExistsOrNot(formId);
        List<Map<String, Object>> relationalMapList = checkIfRelationsExists(formId, relations, sortBy, sortOrder,getCriteria(aclFilter));
        if (!relationalMapList.isEmpty()) return relationalMapList;
        Query query=new Query();
        Criteria andCriteria=getAndCriteria(null,aclFilter);
        if(andCriteria!=null)
        {
            query.addCriteria(andCriteria);
        }
        String searchString=checkValueOfQ(q);
        List<FormDataResponseSchema> formDataResponseSchemasList = ifSortEmpty(formId, sortBy, sortOrder, query, searchString);
        if (!formDataResponseSchemasList.isEmpty()) return formDataResponseSchemasList;
        checkIfBothSortByAndSortOrderGivenAsInput(sortBy, sortOrder);
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
            query.with(Sort.by(Sort.Direction.DESC, CREATED_ON));
        }
        List<FormDataDefinition> formDataDefinitionsList = getFormDataDefinitionsList(formId, query);
        return getFormDataResponseSchemaList(formDataDefinitionsList);
    }

    private List<FormDataResponseSchema> ifSortEmpty(String formId, String sortBy, String sortOrder, Query query, String searchString)
    {
        if(isEmpty(sortBy) && isEmpty(sortOrder))
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
            List<FormDataDefinition> formDataDefinitionsList = getFormDataDefinitionsList(formId, query);
            return getFormDataResponseSchemaList(formDataDefinitionsList);
        }
        return Collections.emptyList();
    }

    private List<Map<String, Object>> checkIfRelationsExists(String formId, String relations, String sortBy, String sortOrder,Criteria criteria)
    {
        if (isNotEmpty(relations))
        {
            List<AggregationOperation> aggregationOperationsList=getAggregationOperationsList(relations);
            if(criteria!=null)
            {
                aggregationOperationsList.add(Aggregation.match(criteria));
            }
            List<Map<String, Object>> relationalMapList = getMapsEmptySort(formId, sortBy, sortOrder, aggregationOperationsList);
            if (!relationalMapList.isEmpty()) return relationalMapList;
            aggregationOperationsList.add(Aggregation.sort(Sort.by(Sort.Direction.fromString(sortOrder), sortBy)));
            List<Document> aggregateList = getDocumentList(formId, aggregationOperationsList);
            return getRelationsMap(aggregateList);
        }
        return Collections.emptyList();
    }

    private static List<FormDataResponseSchema> getFormDataResponseSchemaList(List<FormDataDefinition> formDataDefinitionsList)
    {
        return formDataDefinitionsList.stream().map(x->new FormDataResponseSchema(x.getId(),x.getFormData(),x.getFormMetaData(),String.valueOf(x.getVersion()),x.getCreatedById(),
                x.getCreatedOn(),x.getUpdatedById(),x.getUpdatedOn())).collect(Collectors.toList());
    }


    private static List<Map<String, Object>> getRelationsMap(List<Document> aggregateList)
    {
        List<Map<String, Object>> relationalMapList=new ArrayList<>();
        aggregateList.forEach(x->{
            x.put(ID,String.valueOf(x.get(UNDERSCORE_ID)));
            x.remove(UNDERSCORE_ID);
            relationalMapList.add(x);
        });
        return relationalMapList;
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
    public PaginationResponsePayload getAllFormDataByFormIdAndQ(String formId, String relations, String q, String sortBy, String sortOrder, Pageable pageable, String aclFilter)
    {
        Criteria andCriteria=getAndCriteria(null,aclFilter);
        checkMongoCollectionIfExistsOrNot(formId);
        PaginationResponsePayload paginationResponsePayload = getPaginationWithMongoAndRelations(formId, relations, sortBy, sortOrder, pageable,andCriteria);
        if (paginationResponsePayload!=null) return paginationResponsePayload;
        Query query=new Query();
        if(andCriteria!=null)
        {
            query.addCriteria(andCriteria);
        }
        if(q!=null&&!q.isEmpty()) {
            String searchString= checkValueOfQ(q);
            paginationResponsePayload = sortByAndSortOrderIsEmpty(formId, sortBy, sortOrder, pageable, new Criteria().orOperator(Criteria.where(UNDERSCORE_ID).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                    Criteria.where(VERSION).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                    Criteria.where(CREATED_ON).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                    Criteria.where(CREATED_BY_ID).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                    Criteria.where(CREATED_BY_NAME).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                    Criteria.where(UPDATED_ON).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                    Criteria.where(UPDATED_BY_ID).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
                    Criteria.where(UPDATED_BY_NAME).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE))));
            if (paginationResponsePayload!= null) return paginationResponsePayload;
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
            query.with(Sort.by(Sort.Direction.DESC, CREATED_ON));
        }
        long totalMatchedRecords= getCount(formId, query);
        query.with(pageable);
        List<FormDataDefinition> formDataDefinitionsList = getFormDataDefinitionsList(formId, query);
        int totalPages = getTotalPages(pageable, totalMatchedRecords);
        List<Map<String,Object>> content=getContentList(formDataDefinitionsList);
        return getPaginationResponseFromContent(content, totalMatchedRecords, totalPages,pageable );
    }

    private void checkIfBothSortByAndSortOrderGivenAsInput(String sortBy, String sortOrder)
    {
        if (isEmpty(sortBy) && !isEmpty(sortOrder) || !isEmpty(sortBy) && isEmpty(sortOrder))
        {
            throw new InvalidInputException(SORTBY_AND_SORTORDER_BOTH_SHOULD_BE_GIVEN, globalMessageSource.get(SORTBY_AND_SORTORDER_BOTH_SHOULD_BE_GIVEN));
        }
    }

    private PaginationResponsePayload getPaginationWithMongoAndRelations(String formId, String relations, String sortBy, String sortOrder, Pageable pageable,Criteria aclFilterCriteria)
    {
        if (isNotEmpty(relations))
        {
            List<AggregationOperation> aggregationOperationsList=getAggregationOperationsList(relations);
            if(aclFilterCriteria!=null)
            {
                aggregationOperationsList.add(Aggregation.match(aclFilterCriteria));
            }
            PaginationResponsePayload paginationResponsePayload = getPaginationWithMongoAndEmptySort(formId, sortBy, sortOrder, pageable, aggregationOperationsList);
            if (paginationResponsePayload !=null) return paginationResponsePayload;
            FacetOperation facetOperation=Aggregation.facet(Aggregation.count().as(COUNT)).as(METADATA).and(Aggregation.sort(Sort.by(Sort.Direction.fromString(sortOrder), sortBy)),
                    Aggregation.skip(pageable.getOffset()),Aggregation.limit(pageable.getPageSize())).as(DATA);
            aggregationOperationsList.add(facetOperation);
            List<Document> aggregateList = getDocumentList(formId, aggregationOperationsList);
            Map<String, Object> dataMap = getFirstDocument(aggregateList);
            List<Map<String,Object>> metaDataList= getMetaDataList(dataMap);
            List<Map<String,Object>> dataList= getDataList(dataMap);
            List<Map<String,Object>> content=getContentFromDataList(dataList);
            Map<String,Object> metaData = getMetaDataMap(metaDataList);
            long totalMatchedRecords= extractCountOfMatchedRecords(metaData);
            int totalPages = getTotalPages(pageable, totalMatchedRecords);
            return getPaginationResponseFromContent(content, totalMatchedRecords, totalPages,pageable );
        }
        return null;
    }

    private static List<Map<String, Object>> getMetaDataList(Map<String, Object> dataMap) {
        return (List<Map<String, Object>>) dataMap.get(METADATA);
    }

    private PaginationResponsePayload getPaginationWithMongoAndEmptySort(String formId, String sortBy, String sortOrder, Pageable pageable, List<AggregationOperation> aggregationOperationsList)
    {
        if (isEmpty(sortBy) && isEmpty(sortOrder))
        {
            FacetOperation facetOperation=Aggregation.facet(Aggregation.count().as(COUNT)).as(METADATA).and(Aggregation.sort(Sort.by(Sort.Direction.DESC, CREATED_ON)),
                    Aggregation.skip(pageable.getOffset()),Aggregation.limit(pageable.getPageSize())).as(DATA);
            aggregationOperationsList.add(facetOperation);
            List<Document> aggregateList = getDocumentList(formId, aggregationOperationsList);
            Map<String, Object> dataMap = getFirstDocument(aggregateList);
            List<Map<String,Object>> metaDataList= getMetaDataList(dataMap);
            List<Map<String,Object>> dataList= getDataList(dataMap);
            List<Map<String,Object>> content=getContentFromDataList(dataList);
            Map<String,Object> metaData = getMetaDataMap(metaDataList);
            long totalMatchedRecords = extractCountOfMatchedRecords(metaData);
            int totalPages = getTotalPages(pageable, totalMatchedRecords);
            return getPaginationResponseFromContent(content, totalMatchedRecords, totalPages,pageable );
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

    public PaginationResponsePayload getAllFormDataByFormId(String formId, String relations, String aclFilter)
    {
        Criteria andCriteria=getAndCriteria(null,aclFilter);
        checkMongoCollectionIfExistsOrNot(formId);
        Pageable pageable = getPageable();
        if (isNotEmpty(relations))
        {
            List<AggregationOperation> aggregationOperationsList=getAggregationOperationsList(relations);
            if(andCriteria!=null)
            {
                aggregationOperationsList.add(Aggregation.match(andCriteria));
            }
            FacetOperation facetOperation=Aggregation.facet(Aggregation.count().as(COUNT)).as(METADATA).and(Aggregation.sort(Sort.by(Sort.Direction.DESC, CREATED_ON)),
                    Aggregation.skip(pageable.getOffset()),Aggregation.limit(pageable.getPageSize())).as(DATA);
            aggregationOperationsList.add(facetOperation);
            List<Document> aggregateList = getDocumentList(formId, aggregationOperationsList);
            Map<String, Object> dataMap = getFirstDocument(aggregateList);
            List<Map<String,Object>> metaDataList= getMetaDataList(dataMap);
            List<Map<String,Object>> dataList= getDataList(dataMap);
            List<Map<String,Object>> content=getContentFromDataList(dataList);
            Map<String,Object> metaData =getMetaDataMap(metaDataList);
            long totalMatchedRecords= extractCountOfMatchedRecords(metaData);
            int totalPages = getTotalPages(pageable, totalMatchedRecords);
            return getPaginationResponseFromContent(content, totalMatchedRecords, totalPages, pageable);
        }
        Query query = new Query();
        if(andCriteria!=null)
        {
           query.addCriteria(andCriteria);
        }
        query.with(Sort.by(Sort.Direction.DESC, CREATED_ON));
        long totalMatchedRecords= getCount(formId, query);
        query.with(pageable);
        List<FormDataDefinition> formDataDefinitionsList = getFormDataDefinitionsList(formId, query);
        int totalPages = getTotalPages(pageable, totalMatchedRecords);
        List<Map<String,Object>> content=getContentList(formDataDefinitionsList);
        return getPaginationResponseFromContent(content, totalMatchedRecords, totalPages,pageable);
    }

    private Pageable getPageable()
    {
        return PageRequest.of(0, defaultPageLimit);
    }

    private static PaginationResponsePayload getPaginationResponseFromContent(List<Map<String, Object>> content, long totalMatchedRecords, int totalPages,Pageable pageable)
    {
        PaginationResponsePayload paginationResponsePayload=new PaginationResponsePayload();
        paginationResponsePayload.setContent(content);
        paginationResponsePayload.setTotalPages(totalPages);
        paginationResponsePayload.setTotalElements(totalMatchedRecords);
        paginationResponsePayload.setNumberOfElements(content.size());
        if(pageable!=null)
        {
            paginationResponsePayload.setPage(pageable.getPageNumber());
            paginationResponsePayload.setSize(pageable.getPageSize());
        }
        return paginationResponsePayload;
    }

    @Override
    public List getFormDataByFormIdAndId(String formId, String id, String relations, String aclFilter)
    {
        if (isNotEmpty(relations))
        {
            return getFormDataList(formId, id, relations,aclFilter);
        }
        Query query=new Query();
        if(isNotBlank(aclFilter))
        {
            query.addCriteria(new Criteria().andOperator(Criteria.where(UNDERSCORE_ID).is(id),getCriteria(aclFilter)));
        }
        else {
            query.addCriteria(Criteria.where(UNDERSCORE_ID).is(id));
        }
        checkMongoCollectionIfExistsOrNot(formId);
        try
        {
            if(mongoTemplate.find(query,Document.class,TP_RUNTIME_FORM_DATA + formId).iterator().hasNext())
            {
                Document document=mongoTemplate.find(query,Document.class,TP_RUNTIME_FORM_DATA + formId).iterator().next();
                List<Map<String,Object>> responseList=new ArrayList<>();
                responseList.add(document);
                return responseList;
            }
        }
        catch(Exception e)
        {
            throw new InvalidInputException(e.getMessage(), globalMessageSource.get(e.getMessage()));
        }
        return Collections.emptyList();
    }

    private List<Map<String, Object>> getFormDataList(String formId, String id, String relations,String aclFilter)
    {
        List<AggregationOperation> aggregationOperationsList=getAggregationOperationsList(relations);
        aggregationOperationsList.add(Aggregation.match(Criteria.where(UNDERSCORE_ID).is(Long.valueOf(id))));
        if(isNotBlank(aclFilter))
        {
            aggregationOperationsList.add(Aggregation.match(getCriteria(aclFilter)));
        }
        aggregationOperationsList.add(Aggregation.sort(Sort.by(Sort.Direction.DESC, CREATED_ON)));
        List<Document> aggregateList = getDocumentList(formId, aggregationOperationsList);
        return getRelationsMap(aggregateList);
    }

    @Override
    public void deleteAllFormDataByFormId(String formId)
    {
        try
        {
            mongoTemplate.dropCollection(TP_RUNTIME_FORM_DATA +formId+AUDIT);
            mongoTemplate.dropCollection(TP_RUNTIME_FORM_DATA +formId);
        }
        catch (Exception e)
        {
            throw new InvalidInputException(e.getMessage(), globalMessageSource.get(e.getMessage()));
        }
            try
            {
                String  response= webClientWrapper.webclientRequest(getWebClient(tokenUtils.getTokenFromContext()), gatewayApi + ELASTIC_VERSION1 + SLASH+PARAM_INDEX_NAME+ TP_RUNTIME_FORM_DATA +formId,DELETE,null);
                logger.info(response);
            }
            catch (Exception e)
            {
                throw new InvalidInputException(e.getMessage(),globalMessageSource.get(e.getMessage()));
            }
    }

    @Override
    public void deleteFormDataByFormIdAndId(String formId, String id, String filter, String aclFilter)
    {
        boolean flag = false;
        long count = 0;
        Criteria andCriteria=getAndCriteria(filter,aclFilter);
        Criteria removeCriteria;
        if(andCriteria!=null)
        {
            removeCriteria=new Criteria().andOperator(Criteria.where(UNDERSCORE_ID).is(id),andCriteria);
        }
        else{
            removeCriteria=Criteria.where(UNDERSCORE_ID).is(id);
        }
        try
        {
                DeleteResult deleteResult=mongoTemplate.remove(new Query(removeCriteria),TP_RUNTIME_FORM_DATA +formId);
                count=deleteResult.getDeletedCount();
                mongoTemplate.remove(new Query(removeCriteria),TP_RUNTIME_FORM_DATA +formId+AUDIT);
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
            try
            {
                if(flag)
                {
                    String response = webClientWrapper.webclientRequest(getWebClient(tokenUtils.getTokenFromContext()), gatewayApi + ELASTIC_VERSION1 + SLASH + id + PARAM_INDEX_NAME + TP_RUNTIME_FORM_DATA + formId, DELETE, null);
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

    @Override
    public AggregationResponse aggregateByFormIdFilterGroupBy(String formId, String filter, String groupBy, String operation) {
        checkMongoCollectionIfExistsOrNot(formId);
        List<AggregationOperation> aggregationOperationsList=new ArrayList<>();
        if(isNotEmpty(filter))
        {
            Criteria criteria=getCriteria(filter);
            aggregationOperationsList.add(Aggregation.match(criteria));
        }
        if(operation.equals(COUNT))
        {
            aggregationOperationsList.add(Aggregation.group(groupBy).count().as(COUNT));
        }
        List<Document> aggregateList = getDocumentList(formId, aggregationOperationsList);
        List<Map<String,String>> responseAggregationList=new ArrayList<>();
        aggregateList.forEach(x->{
            Map<String,String> aggregationMap=new HashMap<>();
            aggregationMap.put(UNDERSCORE_ID,String.valueOf(x.get(UNDERSCORE_ID)));
            aggregationMap.put(COUNT,String.valueOf(x.get(COUNT)));
            responseAggregationList.add(aggregationMap);
        });
        return new AggregationResponse(responseAggregationList);
    }
}
