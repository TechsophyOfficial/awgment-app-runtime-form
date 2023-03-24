package com.techsophy.tsf.runtime.form.service;

import com.techsophy.tsf.runtime.form.entity.FormDataDefinition;

public interface FormDataElasticService
{
    void saveOrUpdateToElastic(FormDataDefinition formDataDefinition);
}
