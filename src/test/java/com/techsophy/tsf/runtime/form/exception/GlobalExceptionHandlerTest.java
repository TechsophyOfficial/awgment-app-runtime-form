package com.techsophy.tsf.runtime.form.exception;

import com.techsophy.tsf.runtime.form.model.ApiErrorResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static com.techsophy.tsf.runtime.form.constants.ErrorConstants.ACCESS_DENIED;
import static com.techsophy.tsf.runtime.form.constants.FormAclConstants.NO_RECORD_FOUND;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest
{
    @Mock
    WebRequest webRequest;
    @InjectMocks
    GlobalExceptionHandler globalExceptionHandler;

    @Test
    void recordUnableToSaveExceptionTest()
    {
        RecordUnableToSaveException exception = new RecordUnableToSaveException("error", "msg");
        ResponseEntity<ApiErrorResponse> actualResponse = globalExceptionHandler.recordUnableToSaveException(exception, webRequest);
        ApiErrorResponse errorDetails = new ApiErrorResponse(actualResponse.getBody().getTimestamp(), exception.getMessage(), exception.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR, webRequest.getDescription(false));
        ResponseEntity<ApiErrorResponse> expectedResponse=  new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
        Assertions.assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void ACLExceptionTest()
    {
        ACLException aclException=new ACLException("errorCode",ACCESS_DENIED);
        ResponseEntity<ApiErrorResponse> x=globalExceptionHandler.handleACLException(aclException,webRequest);
        Assertions.assertTrue(x.getStatusCode().is4xxClientError());
    }

    @Test
    void entityPathExceptionTest()
    {
        EntityPathException entityPathException=new EntityPathException("errorCode",NO_RECORD_FOUND);
        ResponseEntity<ApiErrorResponse> x=globalExceptionHandler.entityPathException(entityPathException,webRequest);
        Assertions.assertTrue(x.getStatusCode().is4xxClientError());
    }

    @Test
    void invalidInputExceptionTest()
    {
        InvalidInputException invalidInputException=new InvalidInputException("errorCode",NO_RECORD_FOUND);
        ResponseEntity<ApiErrorResponse> x=globalExceptionHandler.invalidInputException(invalidInputException,webRequest);
        Assertions.assertTrue(x.getStatusCode().is4xxClientError());
    }
}