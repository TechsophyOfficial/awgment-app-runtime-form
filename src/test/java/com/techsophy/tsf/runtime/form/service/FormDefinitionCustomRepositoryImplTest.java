package com.techsophy.tsf.runtime.form.service;

import com.techsophy.tsf.runtime.form.entity.FormDefinition;
import com.techsophy.tsf.runtime.form.repository.impl.FormDefinitionCustomRepositoryImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.ABC;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.TEST_ACTIVE_PROFILE;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.FORM;
import static org.mockito.Mockito.when;

@ActiveProfiles(TEST_ACTIVE_PROFILE)
@SpringBootTest
class FormDefinitionCustomRepositoryImplTest
{
    @Mock
    MongoTemplate mongoTemplate;
    @Mock
    FormDefinition mockFormDefinition;
    @InjectMocks
    FormDefinitionCustomRepositoryImpl mockFormDefinitionCustomRepositoryImpl;

    @Test
    void findByNameOrIdTest()
    {
        when(mockFormDefinitionCustomRepositoryImpl.findByNameOrId(ABC)).thenReturn(List.of(mockFormDefinition));
        List<FormDefinition> formDefinitionListTest=mockFormDefinitionCustomRepositoryImpl.findByNameOrId(ABC);
        Assertions.assertNotNull(formDefinitionListTest);
    }

    @Test
    void findByNameOrIdAndTypeTest()
    {
        when(mockFormDefinitionCustomRepositoryImpl.findByNameOrIdAndType(ABC,FORM)).thenReturn(List.of(mockFormDefinition));
        List<FormDefinition> formDefinitionListTest= mockFormDefinitionCustomRepositoryImpl.findByNameOrIdAndType(ABC,FORM);
        Assertions.assertNotNull(formDefinitionListTest);
    }
}
