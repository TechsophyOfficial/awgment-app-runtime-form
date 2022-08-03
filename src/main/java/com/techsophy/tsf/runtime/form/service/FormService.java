package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techsophy.tsf.runtime.form.dto.FormResponseSchema;
import com.techsophy.tsf.runtime.form.dto.FormSchema;
import java.io.UnsupportedEncodingException;
import java.util.stream.Stream;

public interface FormService
{
    void saveRuntimeForm(FormSchema formSchema) throws JsonProcessingException;

    FormResponseSchema getRuntimeFormById(String id);

    Stream<FormResponseSchema> getAllRuntimeForms(boolean content,String type);

    boolean deleteRuntimeFormById(String id);

    Stream<FormResponseSchema> searchRuntimeFormByIdOrNameLike(String idOrNameLike, String type) throws UnsupportedEncodingException;
}
