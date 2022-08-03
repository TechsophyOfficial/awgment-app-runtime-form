package com.techsophy.tsf.runtime.form.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techsophy.tsf.runtime.form.dto.FormResponseSchema;
import com.techsophy.tsf.runtime.form.dto.FormSchema;
import com.techsophy.tsf.runtime.form.model.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.stream.Stream;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;

@RequestMapping(BASE_URL+ VERSION_V1)
public interface  FormController
{
    @PostMapping(FORMS_URL)
    @PreAuthorize(CREATE_OR_ALL_ACCESS)
    ApiResponse<Void> saveRuntimeForm(@RequestBody @Validated FormSchema formSchema) throws JsonProcessingException;

    @GetMapping(FORM_BY_ID_URL)
    @PreAuthorize(READ_OR_ALL_ACCESS)
    ApiResponse<FormResponseSchema> getRuntimeFormById(@PathVariable(ID) String id) throws IOException;

    @GetMapping(FORMS_URL)
    @PreAuthorize(READ_OR_ALL_ACCESS)
    ApiResponse<Stream<FormResponseSchema>> getAllRuntimeForms(@RequestParam(INCLUDE_CONTENT) boolean includeContent, @RequestParam(value= TYPE,required = false) String type);

    @DeleteMapping(FORM_BY_ID_URL)
    @PreAuthorize(DELETE_OR_ALL_ACCESS)
    ApiResponse<Void> deleteRuntimeFormById(@PathVariable(ID) String id);

    @GetMapping(SEARCH_FORM_URL)
    @PreAuthorize(READ_OR_ALL_ACCESS)
    ApiResponse<Stream<FormResponseSchema>> searchRuntimeFormByIdOrNameLike(@RequestParam(ID_OR_NAME_LIKE) String idOrNameLike, @RequestParam(value = TYPE,required = false) String type) throws UnsupportedEncodingException;
}
