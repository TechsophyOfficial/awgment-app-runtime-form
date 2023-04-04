package com.techsophy.tsf.runtime.form.aspect;

import com.techsophy.tsf.commons.user.UserDetails;
import com.techsophy.tsf.runtime.form.dto.FormAclDto;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import com.techsophy.tsf.runtime.form.utils.WebClientWrapper;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.techsophy.tsf.runtime.form.constants.FormDataConstants.*;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;

@Aspect
@Component
@RequiredArgsConstructor
public class FormDataPushToElasticIndexName {
    private final WebClientWrapper webClientWrapper;
    private final TokenUtils tokenUtils;
    @Value(GATEWAY_URI)
    private final String gatewayApi;

    @AfterReturning(pointcut="execution(* com.techsophy.tsf.runtime.form.service.impl.FormAclServiceImpl.saveFormAcl(..))",returning = "formAclDto")
   public void afterSaveFormAclController(FormAclDto formAclDto)
    {
        if(formAclDto!=null)
        {
            Map<String,Object> map = new HashMap<>();
            map.put(ID,formAclDto.getId());
            map.put(INDEX_NAME,TP_RUNTIME_FORM_DATA+formAclDto.getFormId());
            map.put(ACL_ID,formAclDto.getAclId());
            String token= new com.techsophy.tsf.commons.user.UserDetails(gatewayApi).getToken().get();
            var client = webClientWrapper.createWebClient(token);
            webClientWrapper.webclientRequest(client,gatewayApi+ELASTIC+VERSION_V1+ACL,POST,map);
        }
    }
    @AfterReturning(pointcut="execution(* com.techsophy.tsf.runtime.form.service.impl.FormAclServiceImpl.deleteFormAcl(..)) && args(formId)")
    void afterDeleteFormAclController(String formId)
    {
        String token= new com.techsophy.tsf.commons.user.UserDetails(gatewayApi).getToken().get();
        var client = webClientWrapper.createWebClient(token);
        webClientWrapper.webclientRequest(client,gatewayApi+ELASTIC+VERSION_V1+SLASH+TP_RUNTIME_FORM_DATA+formId+ACL,DELETE,null);
    }

}
