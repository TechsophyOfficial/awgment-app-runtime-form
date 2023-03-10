package com.techsophy.tsf.runtime.form.aspect;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;

@Aspect
@EnableAspectJAutoProxy
@Component
@AllArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class LoggingHandler
{
    @Before(CONTROLLER_CLASS_PATH)
    void beforeController(JoinPoint joinPoint)
    {
        String name = joinPoint.getSignature().getName();
        log.info(IS_INVOKED_IN_CONTROLLER,name);
    }

    @After(CONTROLLER_CLASS_PATH)
    void afterController(JoinPoint joinPoint)
    {
        String name = joinPoint.getSignature().getName();
        log.info(EXECUTION_IS_COMPLETED_IN_CONTROLLER,name);
    }

    @Before(SERVICE_CLASS_PATH)
    void beforeService(JoinPoint joinPoint)
    {
        String name = joinPoint.getSignature().getName();
        log.info(IS_INVOKED_IN_SERVICE,name);
    }

    @After(SERVICE_CLASS_PATH)
    void afterService(JoinPoint joinPoint)
    {
        String name = joinPoint.getSignature().getName();
        log.info(EXECUTION_IS_COMPLETED_IN_SERVICE,name);
    }

    @AfterThrowing(value= CONTROLLER_CLASS_PATH,throwing=EXCEPTION)
    public void logAfterThrowingController(JoinPoint joinPoint, Exception ex) {
        log.error(EXCEPTION_THROWN ,joinPoint.getSignature().getName() ,BRACKETS_IN_CONTROLLER);
        log.error(CAUSE,ex.getMessage());
    }

    @AfterThrowing(value=SERVICE_CLASS_PATH,throwing=EXCEPTION)
    public void logAfterThrowingService(JoinPoint joinPoint, Exception ex) {
        log.error(EXCEPTION_THROWN,joinPoint.getSignature().getName(),BRACKETS_IN_SERVICE);
        log.error(CAUSE,ex.getMessage());
    }
}
