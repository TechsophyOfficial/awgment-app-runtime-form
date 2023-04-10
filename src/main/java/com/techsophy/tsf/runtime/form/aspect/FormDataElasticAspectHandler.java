package com.techsophy.tsf.runtime.form.aspect;

import com.techsophy.tsf.runtime.form.entity.FormDataDefinition;
import com.techsophy.tsf.runtime.form.service.FormDataElasticService;
import com.techsophy.tsf.runtime.form.service.FormService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class FormDataElasticAspectHandler
{
    private final FormService formService;
    private final FormDataElasticService formDataElasticService;

    @AfterReturning(pointcut="execution(* com.techsophy.tsf.runtime.form.service.FormDataService..*(..))",returning = "formDataDefinition")
    void afterControllerAdvice(FormDataDefinition formDataDefinition)
    {
        if(formDataDefinition!=null&&formService.getRuntimeFormById(formDataDefinition.getFormId()).getElasticPush().isEnabled())
        {
            formDataElasticService.saveOrUpdateToElastic(formDataDefinition);
        }
    }
}
