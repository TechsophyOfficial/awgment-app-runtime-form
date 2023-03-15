package com.techsophy.tsf.runtime.form.changelog;

import com.techsophy.tsf.runtime.form.entity.FormDefinition;
import com.techsophy.tsf.runtime.form.entity.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import static org.mockito.Mockito.times;
@ExtendWith(MockitoExtension.class)
class ElasticTest
{
    @Mock
    private MongoTemplate mongo;
    @InjectMocks
    private Elastic elastic;

    @Test
    void testSetElasticPushField()
    {
        elastic.setElasticPushField();
        Mockito.verify(mongo, times(1))
                .updateMulti(new Query(), new Update().set("elasticPush", Status.ENABLED),FormDefinition.class);
    }
}
