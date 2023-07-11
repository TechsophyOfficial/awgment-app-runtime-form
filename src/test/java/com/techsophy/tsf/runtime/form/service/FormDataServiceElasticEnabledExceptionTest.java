package com.techsophy.tsf.runtime.form.service;

import com.mongodb.client.result.DeleteResult;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.exception.InvalidInputException;
import com.techsophy.tsf.runtime.form.service.impl.FormDataServiceImpl;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import com.techsophy.tsf.runtime.form.utils.WebClientWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClient;

import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.EMPTY_STRING;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class FormDataServiceElasticEnabledExceptionTest
{
    @Mock
    TokenUtils mockTokenUtils;
    @Mock
    MessageSource mockMessageSource;
    @Mock
    GlobalMessageSource mockGlobalMessageSource;
    @Mock
    WebClientWrapper mockWebClientWrapper;
    @Mock
    WebClient mockWebClient;
    @Mock
    MongoTemplate mockMongoTemplate;
    @Mock
    DeleteResult deleteResult;
    @InjectMocks
    FormDataServiceImpl mockFormDataServiceImpl;

    @BeforeEach
    public void init()
    {
        ReflectionTestUtils.setField(mockFormDataServiceImpl,ELASTIC_SOURCE,true);
        ReflectionTestUtils.setField(mockFormDataServiceImpl,GATEWAY_API,GATEWAY_API_VALUE);
    }

    @Test
    void deleteAllFormDataByFormIdTokenEmptyExceptionTest()
    {
        when(mockTokenUtils.getTokenFromContext()).thenReturn(EMPTY_STRING);
        Assertions.assertThrows(InvalidInputException.class,()->mockFormDataServiceImpl.deleteAllFormDataByFormId(TEST_FORM_ID));
    }

    @Test
    void deleteAllFormDataByFormIdInvalidInputExceptionTest()
    {
        when(mockTokenUtils.getTokenFromContext()).thenReturn(TEST_TOKEN);
        when(mockWebClientWrapper.createWebClient(TEST_TOKEN)).thenReturn(mockWebClient);
        when(mockWebClientWrapper.webclientRequest(any(),any(),any(),any())).thenThrow(HttpServerErrorException.InternalServerError.class);
        Assertions.assertThrows(InvalidInputException.class,()->mockFormDataServiceImpl.deleteAllFormDataByFormId(TEST_FORM_ID));
    }

    @Test
    void deleteFormDataByFormIdAndIdTest()
    {
        DeleteResult mockDeleteResult=Mockito.mock(DeleteResult.class);
        when(mockMongoTemplate.remove(any(),anyString())).thenReturn(mockDeleteResult);
        when(mockDeleteResult.getDeletedCount()).thenReturn(Long.valueOf(1));
        Assertions.assertThrows(InvalidInputException.class,()->mockFormDataServiceImpl.deleteFormDataByFormIdAndId(TEST_FORM_ID,TEST_ID, null, null));
    }

    @Test
    void deleteFormDataByFormIdAndIdInvalidInputExceptionTest()
    {
        Mockito.when(mockMongoTemplate.remove(any(),anyString())).thenReturn(deleteResult);
        when(deleteResult.getDeletedCount()).thenReturn(Long.valueOf(1));
        Assertions.assertThrows(InvalidInputException.class,()->mockFormDataServiceImpl.deleteFormDataByFormIdAndId(TEST_FORM_ID,TEST_ID, null, null));
    }

    @Test
    void deleteFormDataByFormIdAndIdTokenInvalidInputExceptionTest()
    {
        DeleteResult mockDeleteResult= Mockito.mock(DeleteResult.class);
        when(mockMongoTemplate.remove(any(),anyString())).thenReturn(mockDeleteResult);
        when(mockDeleteResult.getDeletedCount()).thenReturn(Long.valueOf(1));
        Assertions.assertThrows(InvalidInputException.class,()->mockFormDataServiceImpl.deleteFormDataByFormIdAndId(TEST_FORM_ID,TEST_ID, null, null));
    }
}
