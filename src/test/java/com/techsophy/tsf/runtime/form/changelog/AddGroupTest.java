package com.techsophy.tsf.runtime.form.changelog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techsophy.tsf.runtime.form.entity.FormDefinition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.TP_FORM_DEFINITION_COLLECTION;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.UNDERSCORE_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class AddGroupTest
{
    @Mock
    private MongoTemplate template;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AddUser addUser;

    @Test
    void testChangeSetFormDefinition() throws IOException {
        FormDefinition formDefinition1 = new FormDefinition();
        Mockito.when(objectMapper.readValue((InputStream) any(), (Class<Object>) any())).thenReturn(formDefinition1);
        String id = String.valueOf(formDefinition1.getId());
        Query query = new Query();
        query.addCriteria(Criteria.where(UNDERSCORE_ID).is(id));
        Mockito.when(template.find(query, FormDefinition.class)).thenReturn(Collections.emptyList());
        addUser.changeSetFormDefinition();
        Mockito.verify(template, times(1)).save(formDefinition1,TP_FORM_DEFINITION_COLLECTION);
    }
}
