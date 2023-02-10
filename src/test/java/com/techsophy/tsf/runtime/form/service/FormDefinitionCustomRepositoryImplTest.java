package com.techsophy.tsf.runtime.form.service;

import com.techsophy.tsf.runtime.form.entity.FormDefinition;
import com.techsophy.tsf.runtime.form.repository.impl.FormDefinitionCustomRepositoryImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import java.util.List;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.FORM;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.ABC;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
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
