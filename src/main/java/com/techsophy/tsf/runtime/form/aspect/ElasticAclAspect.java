package com.techsophy.tsf.runtime.form.aspect;

import com.techsophy.tsf.runtime.form.dto.ELasticAcl;
import com.techsophy.tsf.runtime.form.dto.FormAclDto;
import com.techsophy.tsf.runtime.form.service.impl.FormDataElasticServiceImpl;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;


@Aspect
@Component
@RequiredArgsConstructor
public class ElasticAclAspect {
    private final FormDataElasticServiceImpl formDataElasticService;

    @AfterReturning(pointcut="execution(* com.techsophy.tsf.runtime.form.service.impl.FormAclServiceImpl.saveFormAcl(..))",returning = "formAclDto")
   public void afterSaveFormAclController(FormAclDto formAclDto)
    {
        ELasticAcl eLasticAcl = new ELasticAcl();
        eLasticAcl.setId(formAclDto.getId());
        eLasticAcl.setIndexName(formDataElasticService.formIdToIndexName(formAclDto.getFormId()));
        eLasticAcl.setAclId(formAclDto.getAclId());
        formDataElasticService.saveACL(eLasticAcl);
    }
    @AfterReturning(pointcut="execution(* com.techsophy.tsf.runtime.form.service.impl.FormAclServiceImpl.deleteFormAcl(..)) && args(formId)")
    void afterDeleteFormAclController(String formId)
    {
        formDataElasticService.deleteACL(formDataElasticService.formIdToIndexName(formId));
    }

}
