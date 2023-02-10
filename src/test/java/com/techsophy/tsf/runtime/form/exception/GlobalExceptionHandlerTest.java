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
}