package com.techsophy.tsf.runtime.form.config;

import com.techsophy.tsf.runtime.form.utils.RelationUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.ARGS;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.KEY;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class GlobalMessageSourceTest
{
    @Mock
    MessageSource mockMessageSource;
    @Mock
    RelationUtils mockRelationUtils;
    @InjectMocks
    GlobalMessageSource mockGlobalMessageSource;

    @BeforeEach
    public void setUp()
    {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getTestSingleArgument()
    {
        Mockito.when(mockMessageSource.getMessage(any(),any(),any())).thenReturn(KEY);
        String responseTest=mockGlobalMessageSource.get(KEY);
        Assertions.assertNotNull(responseTest);
    }

    @Test
    void getTestDoubleArguments()
    {
        Mockito.when(mockMessageSource.getMessage(any(),any(),any())).thenReturn(KEY);
        String responseTest=mockGlobalMessageSource.get(KEY,ARGS);
        Assertions.assertNotNull(responseTest);
    }
}