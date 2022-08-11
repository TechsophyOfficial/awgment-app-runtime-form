package com.techsophy.tsf.runtime.form.changelog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.ClassPath;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.nimbusds.jose.shaded.json.JSONObject;
import com.nimbusds.jose.shaded.json.parser.JSONParser;
import com.nimbusds.jose.shaded.json.parser.ParseException;
import com.nimbusds.jose.util.Resource;
import com.techsophy.tsf.runtime.form.entity.FormDefinition;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;

@ChangeUnit(id=ADD_USER_FORM, order =ORDER_1,systemVersion=SYSTEM_VERSION_1)
@AllArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j

public class AddUser
{
    private  final MongoTemplate template;
    private final ObjectMapper objectMapper;
    public static  int count =0;
//    @Value("classpath:TP_ADD_USER.json")
//    private Resource data;
    @Execution
    public void changeSetFormDefinition() throws IOException, ParseException {
            String  path ="TP_ADD_USER.json";
            InputStream inputStreamTest=new ClassPathResource(path).getInputStream();
            FormDefinition formDefinition1 = objectMapper.readValue(inputStreamTest,FormDefinition.class);
            String id = String.valueOf(formDefinition1.getId());
            Query query = new Query();
            query.addCriteria(Criteria.where("_id").is(id));
            if(template.find(query,FormDefinition.class).size()==0) {
                template.save(formDefinition1, TP_FORM_DEFINITION_COLLECTION);
            }
    }
    @RollbackExecution
    public void rollback()
    {

    }
}
