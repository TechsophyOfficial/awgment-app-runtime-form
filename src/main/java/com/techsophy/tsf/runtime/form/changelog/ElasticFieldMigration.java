package com.techsophy.tsf.runtime.form.changelog;

import com.techsophy.tsf.runtime.form.entity.FormDefinition;
import com.techsophy.tsf.runtime.form.entity.Status;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.EXECUTION_IS_FAILED;

@ChangeUnit(id="push-to-elastic",order = "5")
@Slf4j
@RequiredArgsConstructor
public class ElasticFieldMigration
{
    private  final MongoTemplate mongo;

    @Execution
    public void setElasticPushField()
    {
        Query query = new Query();
        Update update = new Update().set("elasticPush", Status.ENABLED);
        mongo.updateMulti(query, update, FormDefinition.class);
    }

    @RollbackExecution
    public void rollback()
    {
        log.info(EXECUTION_IS_FAILED);
    }
}
