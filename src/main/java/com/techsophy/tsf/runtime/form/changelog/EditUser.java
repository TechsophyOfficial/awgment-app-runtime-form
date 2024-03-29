package com.techsophy.tsf.runtime.form.changelog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techsophy.tsf.runtime.form.entity.FormDefinition;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import java.io.IOException;
import java.io.InputStream;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;

@ChangeUnit(id=EDIT_USER_FORM, order =ORDER_2,systemVersion=SYSTEM_VERSION_1)
@AllArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class EditUser
{
    private  final MongoTemplate mongo;
    private final ObjectMapper objectMapper;
    @Execution
    public void changeSetFormDefinition() throws IOException
    {
        String  path = TP_EDIT_USER;
        InputStream inputStreamTest=new ClassPathResource(path).getInputStream();
        FormDefinition formDefinition1 = objectMapper.readValue(inputStreamTest,FormDefinition.class);
        String id = String.valueOf(formDefinition1.getId());
        Query query = new Query();
        query.addCriteria(Criteria.where(UNDERSCORE_ID).is(id));
        if(mongo.find(query,FormDefinition.class).isEmpty())
        {
            mongo.save(formDefinition1, TP_FORM_DEFINITION_COLLECTION);
        }
    }
    @RollbackExecution
    public void rollback()
    {
        log.info(EXECUTION_IS_FAILED);
    }
}
