package com.techsophy.tsf.runtime.form.service;

import com.techsophy.tsf.runtime.form.dto.ELasticAcl;
import com.techsophy.tsf.runtime.form.entity.FormDataDefinition;

public interface FormDataElasticService
{
    void saveOrUpdateToElastic(FormDataDefinition formDataDefinition);

    void  saveACL(ELasticAcl eLasticAcl);

    void deleteACL(String indexName);
}
