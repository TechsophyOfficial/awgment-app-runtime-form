package com.techsophy.tsf.runtime.form.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techsophy.tsf.runtime.form.dto.ELasticAcl;
import com.techsophy.tsf.runtime.form.dto.FormAclDto;
import com.techsophy.tsf.runtime.form.service.impl.FormDataElasticServiceImpl;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import com.techsophy.tsf.runtime.form.utils.WebClientWrapper;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import static com.techsophy.tsf.runtime.form.constants.FormDataConstants.*;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;

@Aspect
@Component
@RequiredArgsConstructor
public class ElasticAclAspect {
    private final FormDataElasticServiceImpl formDataElasticService;
    private ObjectMapper objectMapper =  new ObjectMapper();

    @AfterReturning(pointcut="execution(* com.techsophy.tsf.runtime.form.service.impl.FormAclServiceImpl.saveFormAcl(..))",returning = "formAclDto")
   public void afterSaveFormAclController(FormAclDto formAclDto)
    {
            Map<String,Object> map = new HashMap<>();
            map.put(ID,formAclDto.getId());
            map.put(INDEX_NAME,FormDataElasticServiceImpl.formIdToIndexName(formAclDto.getFormId()));
            map.put(ACL_ID,formAclDto.getAclId());
            ELasticAcl eLasticAcl = this.objectMapper.convertValue(map,ELasticAcl.class);
            formDataElasticService.saveACL(eLasticAcl);
    }
    @AfterReturning(pointcut="execution(* com.techsophy.tsf.runtime.form.service.impl.FormAclServiceImpl.deleteFormAcl(..)) && args(formId)")
    void afterDeleteFormAclController(String formId)
    {
        formDataElasticService.deleteACL(FormDataElasticServiceImpl.formIdToIndexName(formId));
    }

}
