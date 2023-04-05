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
    private final WebClientWrapper webClientWrapper;
    private final TokenUtils tokenUtils;
    private final FormDataElasticServiceImpl formDataElasticService;
    private final ObjectMapper objectMapper;


    @AfterReturning(pointcut="execution(* com.techsophy.tsf.runtime.form.service.impl.FormAclServiceImpl.saveFormAcl(..))",returning = "formAclDto")
   public void afterSaveFormAclController(FormAclDto formAclDto)
    {
            Map<String,Object> map = new HashMap<>();
            map.put(ID,formAclDto.getId());
            map.put(INDEX_NAME,TP_RUNTIME_FORM_DATA+formAclDto.getFormId());
            map.put(ACL_ID,formAclDto.getAclId());
            ELasticAcl eLasticAcl = this.objectMapper.convertValue(map,ELasticAcl.class);
            formDataElasticService.saveACL(eLasticAcl);
    }
    @AfterReturning(pointcut="execution(* com.techsophy.tsf.runtime.form.service.impl.FormAclServiceImpl.deleteFormAcl(..)) && args(formId)")
    void afterDeleteFormAclController(String formId)
    {
        formDataElasticService.deleteACL(TP_RUNTIME_FORM_DATA+formId);
    }

}
