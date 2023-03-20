package com.techsophy.tsf.runtime.form.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techsophy.tsf.runtime.form.entity.FormDataDefinition;
import com.techsophy.tsf.runtime.form.model.ApiResponse;
import com.techsophy.tsf.runtime.form.service.FormDataElasticService;
import com.techsophy.tsf.runtime.form.service.FormService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class FormDataElasticAspectHandler
{
    private final FormService formService;
    private final ObjectMapper objectMapper;
    private final FormDataElasticService formDataElasticService;

    @Before("execution(* com.techsophy.tsf.runtime.form.controller.FormDataController..*(..))")
    void beforeControllerAdvice(JoinPoint joinPoint) throws Throwable
    {
        ProceedingJoinPoint proceedingJoinPoint = (ProceedingJoinPoint) joinPoint;
        ApiResponse<FormDataDefinition> response = objectMapper.convertValue(proceedingJoinPoint.proceed(), ApiResponse.class);
        FormDataDefinition formDataDefinition= objectMapper.convertValue(response.getData(),FormDataDefinition.class);
        if(formDataDefinition!=null&&formService.getRuntimeFormById(formDataDefinition.getFormId()).getElasticPush().isEnabled())
        {
            formDataElasticService.saveOrUpdateToElastic(formDataDefinition);
        }
    }
}
