package com.techsophy.tsf.runtime.form.aspect;

import com.techsophy.tsf.runtime.form.dto.FormAclDto;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import com.techsophy.tsf.runtime.form.utils.WebClientWrapper;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
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
            map.put("id",formAclDto.getId());
            map.put("indexName",TP_RUNTIME_FORM_DATA+formAclDto.getFormId());
            map.put("aclId",formAclDto.getAclId());
            String token= tokenUtils.getTokenFromContext();
            var client = webClientWrapper.createWebClient(token);
            String response = webClientWrapper.webclientRequest(client,gatewayApi+"/elastic/v1/acl",POST,map);
        }
    }
    @AfterReturning(pointcut="execution(* com.techsophy.tsf.runtime.form.service.impl.FormAclServiceImpl.deleteFormAcl(..)) && args(indexName)")
    void afterDeleteFormAclController(String indexName)
    {
        String token= tokenUtils.getTokenFromContext();
        var client = webClientWrapper.createWebClient(token);
        String response = webClientWrapper.webclientRequest(client,gatewayApi+"/elastic/v1/"+TP_RUNTIME_FORM_DATA+indexName+"/acl",DELETE,null);
    }

}
