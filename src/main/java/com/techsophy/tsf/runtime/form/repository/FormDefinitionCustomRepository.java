package com.techsophy.tsf.runtime.form.repository;

import com.techsophy.tsf.runtime.form.entity.FormDefinition;
import java.io.UnsupportedEncodingException;
import java.util.List;

public interface FormDefinitionCustomRepository
{
    List<FormDefinition> findByNameOrId(String idOrNameLike) throws UnsupportedEncodingException;
    List<FormDefinition> findByNameOrIdAndType(String idOrNameLike,String type);
}
