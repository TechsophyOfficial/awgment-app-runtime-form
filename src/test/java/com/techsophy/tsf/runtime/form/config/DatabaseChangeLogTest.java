package com.techsophy.tsf.runtime.form.config;

import com.techsophy.tsf.runtime.form.entity.FormDefinition;
import com.techsophy.tsf.runtime.form.repository.FormDefinitionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigInteger;
import java.util.List;

import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.STRING;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.TEST_ACTIVE_PROFILE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ActiveProfiles(TEST_ACTIVE_PROFILE)
@SpringBootTest
class DatabaseChangeLogTest {
    @Mock
    FormDefinitionRepository formDefinitionRepository;
    @InjectMocks
    DatabaseChangeLog databaseChangeLog;
    @Test
    void changeSetFormDefinition() throws Exception
    {
        FormDefinition formDefinition = new FormDefinition(BigInteger.ONE,STRING,BigInteger.ONE,null,null,null,STRING,null);
        Mockito.when(formDefinitionRepository.findAll()).thenReturn(List.of(formDefinition));
        Mockito.when(formDefinitionRepository.saveAll(any())).thenReturn(List.of(formDefinition));
        databaseChangeLog.changeSetFormDefinition();
        verify(formDefinitionRepository,times(1)).saveAll(any());
    }
    @Test
    void rollback() throws Exception
    {
        FormDefinition formDefinition = new FormDefinition(BigInteger.ONE,STRING,BigInteger.ONE,null,null,null,STRING,null);
        Mockito.when(formDefinitionRepository.saveAll(any())).thenReturn(List.of(formDefinition));
        databaseChangeLog.rollback();
        verify(formDefinitionRepository,times(1)).saveAll(any());

    }

}
