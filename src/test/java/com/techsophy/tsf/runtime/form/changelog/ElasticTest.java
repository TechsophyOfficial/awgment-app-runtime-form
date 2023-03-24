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
import org.springframework.test.util.ReflectionTestUtils;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.ELASTIC_ENABLE;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ElasticTest
{
    @Mock
    private MongoTemplate mongo;
    @InjectMocks
    private ElasticFieldMigration elastic;

    @Test
    void testSetElasticPushField()
    {
        ReflectionTestUtils.setField(elastic,ELASTIC_ENABLE, true);
        elastic.setElasticPushField();
        Mockito.verify(mongo, times(1))
                .updateMulti(new Query(), new Update().set("elasticPush", Status.ENABLED),FormDefinition.class);
    }
}
