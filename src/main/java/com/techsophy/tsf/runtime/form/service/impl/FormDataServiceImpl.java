package com.techsophy.tsf.runtime.form.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.techsophy.idgenerator.IdGeneratorImpl;
import com.techsophy.tsf.commons.query.Filters;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.config.TenantScopedMongoTemplate;
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
import org.springframework.data.mongodb.core.query.Update;
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
public class FormDataServiceImpl implements FormDataService {
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
      int startIndex = msg.indexOf(INDEX) + INDEX.length();
      int endIndex = msg.indexOf(DUP_KEY);
      String extractedString = msg.substring(startIndex, endIndex).trim();
      throw new InvalidInputException(DUPLICATE_FIELD_VALUE, globalMessageSource.get(DUPLICATE_FIELD_VALUE, extractedString));
    } else {
      throw new InvalidInputException(DUPLICATE_FIELD_VALUE, e.getMessage());
    }
  }

  @Override
  public FormDataDefinition saveFormData(FormDataSchema formDataSchema, String filter, String aclFilter, List<String> orFilter) throws IOException {
    String formId = formDataSchema.getFormId();
    FormResponseSchema formResponseSchema = formService.getRuntimeFormById(formId);
    List<ValidationResult> validationResultList = formValidationServiceImpl.validateData(formResponseSchema, formDataSchema, formId);
    StringBuilder completeMessage = new StringBuilder();
    validationResultList.stream().filter(v -> !v.isValid()).forEach(v -> {
      completeMessage.append(v.getErrorCode()).append(SEMICOLON).append(v.getErrorMessage(globalMessageSource)).append(SEMICOLON);
      throw new InvalidInputException(String.valueOf(completeMessage), String.valueOf(completeMessage));
    });
    FormDataDefinition formDataDefinition = getFormDataDefinition(formDataSchema);
    Map<String, Object> loggedInUserDetails = userDetails.getUserDetails().get(0);
    BigInteger loggedInUserId = BigInteger.valueOf(Long.parseLong(loggedInUserDetails.get(ID).toString()));
    formDataDefinition.setUpdatedOn(String.valueOf(Instant.now()));
    formDataDefinition.setUpdatedById(String.valueOf(loggedInUserId));
    if (formDataDefinition.getId() == null) {
      formDataDefinition.setId(String.valueOf(idGenerator.nextId()));
      formDataDefinition.setVersion(1);
      formDataDefinition.setCreatedById(String.valueOf(loggedInUserId));
      formDataDefinition.setCreatedOn(String.valueOf(Instant.now()));
      logger.info("Before saving to DB: "+ formDataDefinition.getUpdatedById());
      saveToMongo(formId, formDataDefinition);
      logger.info("After saving to DB: "+ formDataDefinition.getUpdatedById());
    } else {

      Criteria criteria = Criteria.where("id").is(formDataDefinition.getId());
      Query query = new Query().addCriteria(criteria);
      FormDataDefinition existingFormDataDefinition = mongoTemplate.findOne(query, FormDataDefinition.class, TP_RUNTIME_FORM_DATA + formId);
      if (existingFormDataDefinition == null) {
        throw new InvalidInputException(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC, globalMessageSource.get(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC, formDataSchema.getId()));
      }
      formDataDefinition.setCreatedById(existingFormDataDefinition.getCreatedById());
      formDataDefinition.setCreatedOn(existingFormDataDefinition.getCreatedOn());
      formDataDefinition.setVersion(existingFormDataDefinition.getVersion() + 1);
      FormDataSchema formDataSchema1 = objectMapper.convertValue(formDataDefinition, FormDataSchema.class);
      Query filtersonData = getQuery(formDataSchema1, filter, aclFilter, orFilter);
      Update update = new Update().set("formData", formDataDefinition.getFormData())
        .set("formMetaData", formDataDefinition.getFormMetaData())
        .set("updatedById", formDataDefinition.getUpdatedById())
        .set("updatedOn", formDataDefinition.getUpdatedOn())
        .set("version", formDataDefinition.getVersion());
      logger.info("Before saving to DB: "+ formDataDefinition.getUpdatedById());
      UpdateResult updateResult = mongoTemplate.updateFirst(filtersonData, update, FormDataDefinition.class, TP_RUNTIME_FORM_DATA + formId);
      logger.info("After saving to DB: "+ formDataDefinition.getUpdatedById());
      if (updateResult.getMatchedCount() == 0) {
        throw new InvalidInputException(FILTERS_NOT_APPLICABLE, globalMessageSource.get(FILTERS_NOT_APPLICABLE, formDataSchema.getId()));
      }
    }
    FormDataAuditSchema formDataAuditSchema = new FormDataAuditSchema(
      String.valueOf(idGenerator.nextId()), formDataDefinition.getId(),
      formId,formDataDefinition.getVersion(), formDataDefinition.getFormData(), formDataDefinition.getFormMetaData()
    );
    try {
      FormDataAuditResponse formDataAuditResponse = this.formDataAuditService.saveFormDataAudit(formDataAuditSchema);
      if (null == formDataAuditResponse.getVersion() || isEmpty(formDataAuditResponse.getVersion().toString())) {
        throw new InvalidInputException(FORMDATA_AUDIT_FAILED, globalMessageSource.get(FORMDATA_AUDIT_FAILED));
      }
    } catch (JsonProcessingException e) {
      throw new InvalidInputException(FORMDATA_AUDIT_FAILED, globalMessageSource.get(FORMDATA_AUDIT_FAILED));
    }
    return formDataDefinition;
  }


  private Query getQuery(FormDataSchema formDataSchema, String filter, String aclFilter, List<String> orFilter) {
    Query query = new Query();
    Criteria idCriteria = Criteria.where(UNDERSCORE_ID).is(formDataSchema.getId());
    Criteria filterCriteria = getCriteria(filter);
    Criteria aclFilterCriteria = getCriteria(aclFilter);
    Criteria orFilterCriteria = getOrCriteria(orFilter);
    query.addCriteria(idCriteria.andOperator(filterCriteria, aclFilterCriteria, orFilterCriteria));
    return query;
  }

  private void saveToMongo(String formId, FormDataDefinition formDataDefinition) {
    try {
      mongoTemplate.save(formDataDefinition, TP_RUNTIME_FORM_DATA + formId);
    } catch (Exception e) {
      handleMongoException(e);
    }
  }

  private FormDataDefinition getFormDataDefinition(FormDataSchema formDataSchema) {
    return objectMapper.convertValue(formDataSchema, FormDataDefinition.class);
  }

  @Override
  public FormDataDefinition updateFormData(FormDataSchema formDataSchema, String filter, String aclFilter, List<String> orFilter) {
    FormDataDefinition formDataDefinition = mongoTemplate.findOne(getQuery(formDataSchema, filter, aclFilter, orFilter), FormDataDefinition.class, TP_RUNTIME_FORM_DATA + formDataSchema.getFormId());
    if (formDataDefinition == null) {
      throw new InvalidInputException(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC, globalMessageSource.get(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC, formDataSchema.getId()));
    }
    String loggedInUserId = userDetails.getCurrentAuditor().orElse(null);
    formDataDefinition.setVersion(formDataDefinition.getVersion() + 1);
    formDataDefinition.setUpdatedOn(String.valueOf(Instant.now()));
    formDataDefinition.setUpdatedById(loggedInUserId);
    Map<String, Object> modifiedFormData = formDataDefinition.getFormData();
    modifiedFormData.putAll(formDataSchema.getFormData());
    Optional.ofNullable(formDataSchema.getFormMetaData()).ifPresent(formDataDefinition::setFormMetaData);
    saveToMongo(formDataSchema.getFormId(), formDataDefinition);
    FormDataAuditSchema formDataAuditSchema = new FormDataAuditSchema(
      String.valueOf(idGenerator.nextId()),formDataDefinition.getId(),
      formDataDefinition.getFormId(), formDataDefinition.getVersion(), formDataDefinition.getFormData(), formDataDefinition.getFormMetaData()
    );
    try {
      FormDataAuditResponse formDataAuditResponse = this.formDataAuditService.saveFormDataAudit(formDataAuditSchema);
      if (null == formDataAuditResponse.getVersion() || isEmpty(formDataAuditResponse.getVersion().toString())) {
        throw new InvalidInputException(FORMDATA_AUDIT_FAILED, globalMessageSource.get(FORMDATA_AUDIT_FAILED));
      }
    } catch (JsonProcessingException e) {
      throw new InvalidInputException(FORMDATA_AUDIT_FAILED, globalMessageSource.get(FORMDATA_AUDIT_FAILED));
    }
    return formDataDefinition;
  }

  @Override
  public List getAllFormDataByFormId(String formId, String relations, String filter, String sortBy, String sortOrder, String aclFilter, List<String> orFilter) {
    checkMongoCollectionIfExistsOrNot(formId);
    Criteria andCriteria = getAndCriteria(filter, aclFilter, orFilter);
    if (isNotEmpty(relations)) {
      ArrayList<String> mappedArrayOfDocumentsName = new ArrayList<>();
      String[] relationsList = relations.split(COMMA);
      ArrayList<String> relationKeysList = new ArrayList<>();
      ArrayList<String> relationValuesList = new ArrayList<>();
      List<Map<String, Object>> relationalMapList = new ArrayList<>();
      List<AggregationOperation> aggregationOperationsList = new ArrayList<>();
      prepareRelationList(mappedArrayOfDocumentsName, relationsList, relationKeysList, relationValuesList);
      prepareDocumentAggregateList(mappedArrayOfDocumentsName, relationKeysList, relationValuesList, aggregationOperationsList);
      if (andCriteria != null) {
        aggregationOperationsList.add(Aggregation.match(andCriteria));
      }
      return getMapsEmptySort(formId, sortBy, sortOrder, relationalMapList, aggregationOperationsList);
    }
    return getFormDataResponseSchemasSort(formId, sortBy, sortOrder, andCriteria);
  }

  private Criteria getAndCriteria(String filter, String aclFilter, List<String> orFilter) {
    if (isNotBlank(filter) && isNotBlank(aclFilter) && (orFilter != null) && (!orFilter.isEmpty())) {
      return new Criteria().andOperator(getCriteria(filter), getCriteria(aclFilter), getOrCriteria(orFilter));
    } else if (isNotBlank(filter) && isNotBlank(aclFilter)) {
      return new Criteria().andOperator(getCriteria(filter), getCriteria(aclFilter));
    } else if (isNotBlank(filter)) {
      return getCriteria(filter);
    } else if (isNotBlank(aclFilter)) {
      return getCriteria(aclFilter);
    } else if ((orFilter != null) && (!orFilter.isEmpty())) {
      return getOrCriteria(orFilter);
    }
    return null;
  }

  public Criteria getOrCriteria(List<String> orFilters) {
    if (orFilters == null || orFilters.isEmpty()) {
      return new Criteria();
    } else {
      List<Criteria> criteriaList = new ArrayList<>();
      orFilters.forEach(filter -> criteriaList.add(getCriteria(filter)));
      return queryBuilder.orQueries(criteriaList);
    }
  }

  public Criteria getCriteria(String filter) {
    if (StringUtils.isBlank(filter)) {
      return new Criteria();
    } else if (!filter.startsWith("{")) {
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
    } else {
      try {
        Filters filters = objectMapper.readValue("{\"operations\":" + filter + "}", Filters.class);
        return filters.buildAndQuery(queryBuilder);
      } catch (JsonProcessingException e) {
        throw new IllegalArgumentException("Invalid filter: " + filter, e);
      }
    }
  }

  private List<FormDataResponseSchema> getFormDataResponseSchemasSort(String formId, String sortBy, String sortOrder, Criteria criteria) {
    Query query = new Query();
    if (criteria != null) {
      query.addCriteria(criteria);
    }
    checkIfBothSortByAndSortOrderGivenAsInput(sortBy, sortOrder);
    List<FormDataResponseSchema> formDataResponseSchemasList = new ArrayList<>();
    if (isEmpty(sortBy) && isEmpty(sortOrder)) {
      setQuery(query);
      List<FormDataDefinition> formDataDefinitionsList = getFormDataDefinitionsList(formId, query);
      prepareFormDataResponseSchemaList(formDataResponseSchemasList, formDataDefinitionsList);
    } else {
      query.with(Sort.by(Sort.Direction.fromString(sortOrder), sortBy));
      List<FormDataDefinition> formDataDefinitionsList = getFormDataDefinitionsList(formId, query);
      prepareFormDataResponseSchemaList(formDataResponseSchemasList, formDataDefinitionsList);
    }
    return formDataResponseSchemasList;
  }

  private List<Map<String, Object>> getMapsEmptySort(String formId, String sortBy, String sortOrder, List<Map<String, Object>> relationalMapList, List<AggregationOperation> aggregationOperationsList) {
    if (isEmpty(sortBy) && isEmpty(sortOrder)) {
      aggregationOperationsList.add(Aggregation.sort(Sort.by(Sort.Direction.DESC, CREATED_ON)));
      List<Document> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList),
        TP_RUNTIME_FORM_DATA + formId, Document.class).getMappedResults();
      prepareRelationsMap(relationalMapList, aggregateList);
    } else {
      aggregationOperationsList.add(Aggregation.sort(Sort.by(Sort.Direction.fromString(sortOrder), sortBy)));
      List<Document> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA + formId, Document.class).getMappedResults();
      prepareRelationsMap(relationalMapList, aggregateList);
    }
    return relationalMapList;
  }

  private static void prepareDocumentAggregateList(ArrayList<String> mappedArrayOfDocumentsName, ArrayList<String> relationKeysList, ArrayList<String> relationValuesList, List<AggregationOperation> aggregationOperationsList) {
    for (int j = 0; j < relationKeysList.size(); j++) {
      DocumentAggregationOperation documentAggregationOperation = new DocumentAggregationOperation(String.format(MONGO_AGGREGATION_STAGE_PIPELINE_1, relationKeysList.get(j), relationValuesList.get(j), relationValuesList.get(j), mappedArrayOfDocumentsName.get(j)));
      aggregationOperationsList.add(documentAggregationOperation);
    }
  }

  private WebClient checkEmptyToken(String token) {
    WebClient webClient;
    if (isNotEmpty(token)) {
      webClient = webClientWrapper.createWebClient(token);
    } else {
      throw new InvalidInputException(TOKEN_NOT_NULL, globalMessageSource.get(TOKEN_NOT_NULL, tokenUtils.getLoggedInUserId()));
    }
    return webClient;
  }

  @Override
  public PaginationResponsePayload getAllFormDataByFormId(String formId, String relations, String filter, String sortBy, String sortOrder, Pageable pageable, String aclFilter, List<String> orFilter) {
    PaginationResponsePayload paginationResponsePayload = new PaginationResponsePayload();
    checkMongoCollectionIfExistsOrNot(formId);
    List<Map<String, Object>> content = new ArrayList<>();
    paginationResponsePayload.setPage(pageable.getPageNumber());
    paginationResponsePayload.setSize(pageable.getPageSize());
    Query query = new Query();
    Criteria andCriteria = getAndCriteria(filter, aclFilter, orFilter);
    PaginationResponsePayload paginationResponsePayload1 = getPaginationResponsePayloadIfRelationsExists(formId, relations, sortBy + ";" + sortOrder, pageable, paginationResponsePayload, content, andCriteria);
    if (paginationResponsePayload1 != null) return paginationResponsePayload1;
    paginationResponsePayload1 = sortByAndSortOrderIsEmpty(formId, sortBy, sortOrder, pageable, paginationResponsePayload, content, andCriteria);
    if (paginationResponsePayload1 != null) return paginationResponsePayload1;
    checkIfBothSortByAndSortOrderGivenAsInput(sortBy, sortOrder);
    if (andCriteria != null) {
      query.addCriteria(andCriteria);
    }
    query.with(Sort.by(Sort.Direction.fromString(sortOrder), sortBy));
    long totalMatchedRecords = getCount(formId, query);
    query.with(pageable);
    List<FormDataDefinition> formDataDefinitionsList = getFormDataDefinitionsList(formId, query);
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

  private PaginationResponsePayload getPaginationResponsePayloadIfRelationsExists(String formId, String relations, String sort, Pageable pageable, PaginationResponsePayload paginationResponsePayload, List<Map<String, Object>> content, Criteria criteria) {
    String sortBy = EMPTY_STRING;
    String sortOrder = EMPTY_STRING;
    if (sort.split(";").length != 0) {
      sortBy = sort.split(";")[0];
      sortOrder = sort.split(";")[1];
    }
    if (isNotEmpty(relations)) {
      ArrayList<String> mappedArrayOfDocumentsName = new ArrayList<>();
      List<AggregationOperation> aggregationOperationsList = new ArrayList<>();
      String[] relationsList = relations.split(COMMA);
      ArrayList<String> relationKeysList = new ArrayList<>();
      ArrayList<String> relationValuesList = new ArrayList<>();
      prepareRelationList(mappedArrayOfDocumentsName, relationsList, relationKeysList, relationValuesList);
      if (criteria != null) {
        aggregationOperationsList.add(Aggregation.match(criteria));
      }
      prepareDocumentAggregateList(mappedArrayOfDocumentsName, relationKeysList, relationValuesList, aggregationOperationsList);
      PaginationResponsePayload paginationResponsePayload1 = getPaginationWithMongoAndEmptySort(formId, sortBy, sortOrder, pageable, paginationResponsePayload, content, aggregationOperationsList);
      if (paginationResponsePayload1 != null) return paginationResponsePayload1;
      FacetOperation facetOperation;
      if (sortBy.equals("null") || sortOrder.equals("null")) {
        facetOperation = Aggregation.facet(Aggregation.count().as(COUNT)).as(METADATA)
          .and(Aggregation.sort(Sort.by(Sort.Direction.DESC, CREATED_ON)),
            Aggregation.skip(pageable.getOffset()), Aggregation.limit(pageable.getPageSize())).as(DATA);
      } else {
        facetOperation = Aggregation.facet(Aggregation.count().as(COUNT)).as(METADATA)
          .and(Aggregation.sort(Sort.by(Sort.Direction.fromString(sortOrder), sortBy)),
            Aggregation.skip(pageable.getOffset()), Aggregation.limit(pageable.getPageSize())).as(DATA);
      }
      aggregationOperationsList.add(facetOperation);
      List<Document> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA + formId, Document.class).getMappedResults();
      Map<String, Object> dataMap = aggregateList.get(0);
      List<Map<String, Object>> metaDataList = getMetaDataList(dataMap);
      List<Map<String, Object>> dataList = getDataList(dataMap);
      prepareContentListFromData(content, dataList);
      Map<String, Object> metaData = new HashMap<>();
      metaData = getMetaDataMap(metaDataList, metaData);
      long totalMatchedRecords;
      totalMatchedRecords = extractCountOfMatchedRecords(metaData);
      int totalPages = getTotalPages(pageable, totalMatchedRecords);
      setPaginationResponsePayload(paginationResponsePayload, content, totalMatchedRecords, totalPages);
      return paginationResponsePayload;
    }
    return null;
  }

  private static List<Map<String, Object>> getDataList(Map<String, Object> dataMap) {
    return (List<Map<String, Object>>) dataMap.get(DATA);
  }

  private PaginationResponsePayload sortByAndSortOrderIsEmpty(String formId, String sortBy, String sortOrder, Pageable pageable, PaginationResponsePayload paginationResponsePayload, List<Map<String, Object>> content, Criteria criteria) {
    if (isEmpty(sortBy) && isEmpty(sortOrder)) {
      Query query = new Query();
      if (criteria != null) {
        query.addCriteria(criteria);
      }
      setQuery(query);
      long totalMatchedRecords = getCount(formId, query);
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

  private static void prepareContentList(List<Map<String, Object>> content, List<FormDataDefinition> formDataDefinitionsList) {
    ObjectMapper objectMapper1 = new ObjectMapper();
    formDataDefinitionsList.forEach(x -> content.add(objectMapper1.convertValue(x, Map.class)));
  }

  private static long extractCountOfMatchedRecords(Map<String, Object> metaData) {
    if (metaData != null) {
      return Long.parseLong(String.valueOf(metaData.get(COUNT)));
    } else return 0;
  }

  private static void prepareRelationList(ArrayList<String> mappedArrayOfDocumentsName, String[] relationsList, ArrayList<String> relationKeysList, ArrayList<String> relationValuesList) {
    Arrays.stream(relationsList).forEach(x -> {
      String[] keyValuePair = x.split(COLON);
      mappedArrayOfDocumentsName.add(keyValuePair[0]);
      keyValuePair[0] = TP_RUNTIME_FORM_DATA + keyValuePair[0];
      keyValuePair[1] = FORM_DATA + DOT + keyValuePair[1];
      relationKeysList.add(keyValuePair[0].replaceAll(REGEX_PATTERN_1, EMPTY_STRING));
      relationValuesList.add(keyValuePair[1].replaceAll(REGEX_PATTERN_1, EMPTY_STRING));
    });
  }

  private static void prepareContentListFromData(List<Map<String, Object>> content, List<Map<String, Object>> dataList) {
    dataList.forEach(x -> {
      x.put(ID, String.valueOf(x.get(UNDERSCORE_ID)));
      x.remove(UNDERSCORE_ID);
      content.add(x);
    });
  }

  private static Map<String, Object> getMetaDataMap(List<Map<String, Object>> metaDataList, Map<String, Object> metaData) {
    if (!metaDataList.isEmpty()) {
      metaData = metaDataList.get(0);
    }
    return metaData;
  }

  public List getAllFormDataByFormIdAndQ(String formId, String relations, String q, String sortBy, String sortOrder, String aclFilter, List<String> orFilter) {
    checkMongoCollectionIfExistsOrNot(formId);
    List<Map<String, Object>> relationalMapList1 = checkIfRelationsExists(formId, relations, sortBy, sortOrder, getCriteria(aclFilter));
    if (!relationalMapList1.isEmpty()) return relationalMapList1;
    Query query = new Query();
    Criteria andCriteria = getAndCriteria(null, aclFilter, orFilter);
    if (andCriteria != null) {
      query.addCriteria(andCriteria);
    }
    String searchString;
    searchString = checkValueOfQ(q);
    List<FormDataResponseSchema> formDataResponseSchemasList = new ArrayList<>();
    List<FormDataResponseSchema> formDataResponseSchemasList1 = ifSortEmpty(formId, sortBy, sortOrder, query, searchString, formDataResponseSchemasList);
    if (!formDataResponseSchemasList1.isEmpty()) return formDataResponseSchemasList1;
    checkIfBothSortByAndSortOrderGivenAsInput(sortBy, sortOrder);
    List<FormDataDefinition> formDataDefinitionsList;
    if (isNotEmpty(searchString)) {
      query.addCriteria(new Criteria().orOperator(
        Criteria.where(UNDERSCORE_ID).is(searchString),
        Criteria.where(VERSION).is(searchString),
        Criteria.where(CREATED_ON).is(searchString),
        Criteria.where(CREATED_BY_ID).is(searchString),
        Criteria.where(CREATED_ON).is(searchString),
        Criteria.where(UPDATED_BY_ID).is(searchString),
        Criteria.where(UPDATED_ON).is(searchString)));
    }
    if (isNotEmpty(sortBy) && isNotEmpty(sortOrder)) {
      query.with(Sort.by(Sort.Direction.fromString(sortOrder), sortBy));
    } else {
      setQuery(query);
    }
    formDataDefinitionsList = getFormDataDefinitionsList(formId, query);
    prepareFormDataResponseSchemaList(formDataResponseSchemasList, formDataDefinitionsList);
    return formDataResponseSchemasList;
  }

  private List<FormDataResponseSchema> ifSortEmpty(String formId, String sortBy, String sortOrder, Query query, String searchString, List<FormDataResponseSchema> formDataResponseSchemasList) {
    if (isEmpty(sortBy) && isEmpty(sortOrder)) {
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

  private List<Map<String, Object>> checkIfRelationsExists(String formId, String relations, String sortBy, String sortOrder, Criteria criteria) {
    if (isNotEmpty(relations)) {
      ArrayList<String> mappedArrayOfDocumentsName = new ArrayList<>();
      String[] relationsList = relations.split(COMMA);
      ArrayList<String> relationKeysList = new ArrayList<>();
      ArrayList<String> relationValuesList = new ArrayList<>();
      prepareRelationList(mappedArrayOfDocumentsName, relationsList, relationKeysList, relationValuesList);
      List<Map<String, Object>> relationalMapList = new ArrayList<>();
      List<AggregationOperation> aggregationOperationsList = new ArrayList<>();
      if (criteria != null) {
        aggregationOperationsList.add(Aggregation.match(criteria));
      }
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

  private static void prepareFormDataResponseSchemaList(List<FormDataResponseSchema> formDataResponseSchemasList, List<FormDataDefinition> formDataDefinitionsList) {
    formDataDefinitionsList.forEach(x -> {
      FormDataResponseSchema formDataResponseSchema = new FormDataResponseSchema(
        x.getId(), x.getFormData(), x.getFormMetaData(), String.valueOf(x.getVersion()), x.getCreatedById(),
        x.getCreatedOn(), x.getUpdatedById(), x.getUpdatedOn()
      );
      formDataResponseSchemasList.add(formDataResponseSchema);
    });
  }


  private static void prepareRelationsMap(List<Map<String, Object>> relationalMapList, List<Document> aggregateList) {
    aggregateList.forEach(x -> {
      x.put(ID, String.valueOf(x.get(UNDERSCORE_ID)));
      x.remove(UNDERSCORE_ID);
      relationalMapList.add(x);
    });
  }

  private String checkValueOfQ(String q) {
    if (q != null) {
      return URLDecoder.decode(q, StandardCharsets.UTF_8);
    }
    return EMPTY_STRING;
  }

  @Override
  public PaginationResponsePayload getAllFormDataByFormIdAndQ(String formId, String relations, String q, String sortBy, String sortOrder, Pageable pageable, String aclFilter, List<String> orFilter) {
    Criteria andCriteria = getAndCriteria(null, aclFilter, orFilter);
    PaginationResponsePayload paginationResponsePayload = new PaginationResponsePayload();
    checkMongoCollectionIfExistsOrNot(formId);
    paginationResponsePayload.setPage(pageable.getPageNumber());
    paginationResponsePayload.setSize(pageable.getPageSize());
    List<Map<String, Object>> content = new ArrayList<>();
    PaginationResponsePayload paginationResponsePayload1 = getPaginationWithMongoAndRelations(formId, relations, sortBy, sortOrder, pageable, paginationResponsePayload, content, andCriteria);
    if (paginationResponsePayload1 != null) return paginationResponsePayload1;
    Query query = new Query();
    if (andCriteria != null) {
      query.addCriteria(andCriteria);
    }
    if (q != null && !q.isEmpty()) {
      String searchString = checkValueOfQ(q);
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
    if (isNotEmpty(sortBy) && isNotEmpty(sortOrder)) {
      query.with(Sort.by(Sort.Direction.fromString(sortOrder), sortBy));
    } else {
      setQuery(query);
    }
    long totalMatchedRecords = getCount(formId, query);
    query.with(pageable);
    List<FormDataDefinition> formDataDefinitionsList = getFormDataDefinitionsList(formId, query);
    int totalPages = getTotalPages(pageable, totalMatchedRecords);
    prepareContentList(content, formDataDefinitionsList);
    setPaginationResponsePayload(paginationResponsePayload, content, totalMatchedRecords, totalPages);
    return paginationResponsePayload;
  }

  private void checkIfBothSortByAndSortOrderGivenAsInput(String sortBy, String sortOrder) {
    if (isEmpty(sortBy) && !isEmpty(sortOrder) || !isEmpty(sortBy) && isEmpty(sortOrder)) {
      throw new InvalidInputException(SORTBY_AND_SORTORDER_BOTH_SHOULD_BE_GIVEN, globalMessageSource.get(SORTBY_AND_SORTORDER_BOTH_SHOULD_BE_GIVEN));
    }
  }

  private PaginationResponsePayload getPaginationWithMongoAndRelations(String formId, String relations, String sortBy, String sortOrder, Pageable pageable, PaginationResponsePayload paginationResponsePayload, List<Map<String, Object>> content, Criteria aclFilterCriteria) {
    if (isNotEmpty(relations)) {
      ArrayList<String> mappedArrayOfDocumentsName = new ArrayList<>();
      String[] relationsList = relations.split(COMMA);
      ArrayList<String> relationKeysList = new ArrayList<>();
      ArrayList<String> relationValuesList = new ArrayList<>();
      prepareRelationList(mappedArrayOfDocumentsName, relationsList, relationKeysList, relationValuesList);
      List<AggregationOperation> aggregationOperationsList = new ArrayList<>();
      if (aclFilterCriteria != null) {
        aggregationOperationsList.add(Aggregation.match(aclFilterCriteria));
      }
      prepareDocumentAggregateList(mappedArrayOfDocumentsName, relationKeysList, relationValuesList, aggregationOperationsList);
      PaginationResponsePayload paginationResponsePayload1 = getPaginationWithMongoAndEmptySort(formId, sortBy, sortOrder, pageable, paginationResponsePayload, content, aggregationOperationsList);
      if (paginationResponsePayload1 != null) return paginationResponsePayload1;
      FacetOperation facetOperation = Aggregation.facet(Aggregation.count().as(COUNT)).as(METADATA).and(Aggregation.sort(Sort.by(Sort.Direction.fromString(sortOrder), sortBy)),
        Aggregation.skip(pageable.getOffset()), Aggregation.limit(pageable.getPageSize())).as(DATA);
      aggregationOperationsList.add(facetOperation);
      List<Document> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA + formId, Document.class).getMappedResults();
      Map<String, Object> dataMap = aggregateList.get(0);
      List<Map<String, Object>> metaDataList = getMetaDataList(dataMap);
      List<Map<String, Object>> dataList = getDataList(dataMap);
      prepareContentListFromData(content, dataList);
      Map<String, Object> metaData = new HashMap<>();
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

  private PaginationResponsePayload getPaginationWithMongoAndEmptySort(String formId, String sortBy, String sortOrder, Pageable pageable, PaginationResponsePayload paginationResponsePayload, List<Map<String, Object>> content, List<AggregationOperation> aggregationOperationsList) {
    if (isEmpty(sortBy) && isEmpty(sortOrder)) {
      FacetOperation facetOperation = Aggregation.facet(Aggregation.count().as(COUNT)).as(METADATA).and(Aggregation.sort(Sort.by(Sort.Direction.DESC, CREATED_ON)),
        Aggregation.skip(pageable.getOffset()), Aggregation.limit(pageable.getPageSize())).as(DATA);
      aggregationOperationsList.add(facetOperation);
      List<Document> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA + formId, Document.class).getMappedResults();
      Map<String, Object> dataMap = aggregateList.get(0);
      List<Map<String, Object>> metaDataList = getMetaDataList(dataMap);
      List<Map<String, Object>> dataList = getDataList(dataMap);
      prepareContentListFromData(content, dataList);
      Map<String, Object> metaData = new HashMap<>();
      metaData = getMetaDataMap(metaDataList, metaData);
      long totalMatchedRecords = extractCountOfMatchedRecords(metaData);
      int totalPages = getTotalPages(pageable, totalMatchedRecords);
      setPaginationResponsePayload(paginationResponsePayload, content, totalMatchedRecords, totalPages);
      return paginationResponsePayload;
    }
    return null;
  }

  private void checkMongoCollectionIfExistsOrNot(String formId) {
    if (!mongoTemplate.collectionExists(TP_RUNTIME_FORM_DATA + formId)) {
      throw new FormIdNotFoundException(FORM_DATA_DOES_NOT_EXISTS_WITH_GIVEN_FORMID, globalMessageSource.get(FORM_DATA_DOES_NOT_EXISTS_WITH_GIVEN_FORMID, formId));
    }
  }

  public PaginationResponsePayload getAllFormDataByFormId(String formId, String relations, String aclFilter, List<String> orFilter) {
    Criteria andCriteria = getAndCriteria(null, aclFilter, orFilter);
    PaginationResponsePayload paginationResponsePayload = new PaginationResponsePayload();
    checkMongoCollectionIfExistsOrNot(formId);
    List<Map<String, Object>> content = new ArrayList<>();
    Pageable pageable = getPageable(paginationResponsePayload);
    if (isNotEmpty(relations)) {
      ArrayList<String> mappedArrayOfDocumentsName = new ArrayList<>();
      String[] relationsList = relations.split(COMMA);
      ArrayList<String> relationKeysList = new ArrayList<>();
      ArrayList<String> relationValuesList = new ArrayList<>();
      prepareRelationList(mappedArrayOfDocumentsName, relationsList, relationKeysList, relationValuesList);
      List<AggregationOperation> aggregationOperationsList = new ArrayList<>();
      if (andCriteria != null) {
        aggregationOperationsList.add(Aggregation.match(andCriteria));
      }
      prepareDocumentAggregateList(mappedArrayOfDocumentsName, relationKeysList, relationValuesList, aggregationOperationsList);
      FacetOperation facetOperation = Aggregation.facet(Aggregation.count().as(COUNT)).as(METADATA).and(Aggregation.sort(Sort.by(Sort.Direction.DESC, CREATED_ON)),
        Aggregation.skip(pageable.getOffset()), Aggregation.limit(pageable.getPageSize())).as(DATA);
      aggregationOperationsList.add(facetOperation);
      List<Document> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA + formId, Document.class).getMappedResults();
      Map<String, Object> dataMap = aggregateList.get(0);
      List<Map<String, Object>> metaDataList = getMetaDataList(dataMap);
      List<Map<String, Object>> dataList = getDataList(dataMap);
      prepareContentListFromData(content, dataList);
      Map<String, Object> metaData = new HashMap<>();
      metaData = getMetaDataMap(metaDataList, metaData);
      long totalMatchedRecords;
      totalMatchedRecords = extractCountOfMatchedRecords(metaData);
      int totalPages = getTotalPages(pageable, totalMatchedRecords);
      setPaginationResponsePayload(paginationResponsePayload, content, totalMatchedRecords, totalPages);
      return paginationResponsePayload;
    }
    Query query = new Query();
    if (andCriteria != null) {
      query.addCriteria(andCriteria);
    }
    List<FormDataDefinition> formDataDefinitionsList;
    setQuery(query);
    long totalMatchedRecords = getCount(formId, query);
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

  private Pageable getPageable(PaginationResponsePayload paginationResponsePayload) {
    Pageable pageable = PageRequest.of(0, defaultPageLimit);
    paginationResponsePayload.setPage(pageable.getPageNumber());
    paginationResponsePayload.setSize(pageable.getPageSize());
    return pageable;
  }

  private static void setPaginationResponsePayload(PaginationResponsePayload paginationResponsePayload, List<Map<String, Object>> content, long totalMatchedRecords, int totalPages) {
    paginationResponsePayload.setContent(content);
    paginationResponsePayload.setTotalPages(totalPages);
    paginationResponsePayload.setTotalElements(totalMatchedRecords);
    paginationResponsePayload.setNumberOfElements(content.size());
  }

  @Override
  public List getFormDataByFormIdAndId(String formId, String id, String relations, String aclFilter, List<String> orFilter) {
    List<Map<String, Object>> responseList = new ArrayList<>();
    if (isNotEmpty(relations)) {
      return getFormDataList(formId, id, relations, aclFilter, orFilter);
    }
    Query query = new Query();
    if (isNotBlank(aclFilter) && orFilter != null && !orFilter.isEmpty()) {
      query.addCriteria(new Criteria().andOperator(Criteria.where(UNDERSCORE_ID).is(id), getCriteria(aclFilter), getOrCriteria(orFilter)));
    } else {
      query.addCriteria(Criteria.where(UNDERSCORE_ID).is(id));
    }
    checkMongoCollectionIfExistsOrNot(formId);
    try {
      if (mongoTemplate.find(query, Document.class, TP_RUNTIME_FORM_DATA + formId).iterator().hasNext()) {
        Document document = mongoTemplate.find(query, Document.class, TP_RUNTIME_FORM_DATA + formId).iterator().next();
        responseList.add(document);
      }
    } catch (Exception e) {
      throw new InvalidInputException(e.getMessage(), globalMessageSource.get(e.getMessage()));
    }
    return responseList;
  }

  private List<Map<String, Object>> getFormDataList(String formId, String id, String relations, String aclFilter, List<String> orFilter) {
    ArrayList<String> mappedArrayOfDocumentsName = new ArrayList<>();
    String[] relationsList = relations.split(COMMA);
    ArrayList<String> relationKeysList = new ArrayList<>();
    ArrayList<String> relationValuesList = new ArrayList<>();
    prepareRelationList(mappedArrayOfDocumentsName, relationsList, relationKeysList, relationValuesList);
    List<Map<String, Object>> relationalMapList = new ArrayList<>();
    List<AggregationOperation> aggregationOperationsList = new ArrayList<>();
    aggregationOperationsList.add(Aggregation.match(Criteria.where(UNDERSCORE_ID).is(Long.valueOf(id))));
    if (isNotBlank(aclFilter) && orFilter != null && !orFilter.isEmpty()) {
      aggregationOperationsList.add(Aggregation.match(new Criteria().andOperator(getCriteria(aclFilter), getOrCriteria(orFilter))));
    } else if (isNotBlank(aclFilter)) {
      aggregationOperationsList.add(Aggregation.match(getCriteria(aclFilter)));
    } else if (orFilter != null && !orFilter.isEmpty()) {
      aggregationOperationsList.add(Aggregation.match(getOrCriteria(orFilter)));
    }
    prepareDocumentAggregateList(mappedArrayOfDocumentsName, relationKeysList, relationValuesList, aggregationOperationsList);
    aggregationOperationsList.add(Aggregation.sort(Sort.by(Sort.Direction.DESC, CREATED_ON)));
    List<Document> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList), TP_RUNTIME_FORM_DATA + formId, Document.class).getMappedResults();
    prepareRelationsMap(relationalMapList, aggregateList);
    return relationalMapList;
  }

  @Override
  public void deleteAllFormDataByFormId(String formId) {
    try {
      mongoTemplate.dropCollection(TP_RUNTIME_FORM_DATA + formId + AUDIT);
      mongoTemplate.dropCollection(TP_RUNTIME_FORM_DATA + formId);
    } catch (Exception e) {
      throw new InvalidInputException(e.getMessage(), globalMessageSource.get(e.getMessage()));
    }
    WebClient webClient = checkEmptyToken(tokenUtils.getTokenFromContext());
    try {
      String response = webClientWrapper.webclientRequest(webClient, gatewayApi + ELASTIC_VERSION1 + SLASH + PARAM_INDEX_NAME + TP_RUNTIME_FORM_DATA + formId, DELETE, null);
      logger.info(response);
    } catch (Exception e) {
      throw new InvalidInputException(e.getMessage(), globalMessageSource.get(e.getMessage()));
    }
  }

  @Override
  public void deleteFormDataByFormIdAndId(String formId, String id, String filter, String aclFilter, List<String> orFilter) {
    boolean flag = false;
    long count = 0;
    Criteria andCriteria = getAndCriteria(filter, aclFilter, orFilter);
    Criteria removeCriteria;
    if (andCriteria != null) {
      removeCriteria = new Criteria().andOperator(Criteria.where(UNDERSCORE_ID).is(id), andCriteria);
    } else {
      removeCriteria = Criteria.where(UNDERSCORE_ID).is(id);
    }
    try {
      DeleteResult deleteResult = mongoTemplate.remove(new Query(removeCriteria), TP_RUNTIME_FORM_DATA + formId);
      count = deleteResult.getDeletedCount();
      mongoTemplate.remove(new Query(removeCriteria), TP_RUNTIME_FORM_DATA + formId + AUDIT);
      flag = true;
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
    if (count == 0) {
      throw new FormIdNotFoundException(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC, globalMessageSource.get(FORM_DATA_NOT_FOUND_WITH_GIVEN_FORMDATAID_IN_MONGO_AND_ELASTIC, id));
    }
    WebClient webClient = checkEmptyToken(tokenUtils.getTokenFromContext());
    try {
      if (flag) {
        String response = webClientWrapper.webclientRequest(webClient, gatewayApi + ELASTIC_VERSION1 + SLASH + id + PARAM_INDEX_NAME + TP_RUNTIME_FORM_DATA + formId, DELETE, null);
        logger.info(response);
      } else {
        throw new EntityIdNotFoundException(UNABLE_TO_DELETE_FORM_DATA_IN_ELASTIC_DB, globalMessageSource.get(UNABLE_TO_DELETE_FORM_DATA_IN_ELASTIC_DB));
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
  }

  @Override
  public AggregationResponse aggregateByFormIdFilterGroupBy(String formId, String filter, String groupBy, String operation) {
    checkMongoCollectionIfExistsOrNot(formId);
    List<AggregationOperation> aggregationOperationsList = new ArrayList<>();
    if (isNotEmpty(filter)) {
      createMultipleFilterCriteria(filter, aggregationOperationsList);
    }
    if (operation.equals(COUNT)) {
      aggregationOperationsList.add(Aggregation.group(groupBy).count().as(COUNT));
    }
    List<Document> aggregateList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationsList),
      TP_RUNTIME_FORM_DATA + formId, Document.class).getMappedResults();
    List<Map<String, String>> responseAggregationList = new ArrayList<>();
    aggregateList.forEach(x -> {
      Map<String, String> aggregationMap = new HashMap<>();
      aggregationMap.put(UNDERSCORE_ID, String.valueOf(x.get(UNDERSCORE_ID)));
      aggregationMap.put(COUNT, String.valueOf(x.get(COUNT)));
      responseAggregationList.add(aggregationMap);
    });
    return new AggregationResponse(responseAggregationList);
  }

  private void createMultipleFilterCriteria(String filter, List<AggregationOperation> aggregationOperationsList) {
    Criteria criteria = getCriteria(filter);
    aggregationOperationsList.add(Aggregation.match(criteria));
  }
}
