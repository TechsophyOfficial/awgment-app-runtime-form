package com.techsophy.tsf.runtime.form.changelog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ElasticTest
{
    @Mock
    Environment env;
    @Mock
    private MongoTemplate mongo;
    @InjectMocks
    private ElasticFieldMigration elastic;

    @Test
    void testSetElasticPushField()
    {
        Mockito.when(env.getProperty(anyString())).thenReturn("true");
        elastic.setElasticPushField();
        Mockito.verify(mongo, times(1))
                .updateMulti( any(),  any(), (Class<?>) any());
    }
}
