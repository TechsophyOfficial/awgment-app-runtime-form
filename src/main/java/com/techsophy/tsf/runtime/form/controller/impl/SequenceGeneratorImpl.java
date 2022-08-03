package com.techsophy.tsf.runtime.form.controller.impl;

import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.controller.SequenceGeneratorController;
import com.techsophy.tsf.runtime.form.dto.SequenceGeneratorDTO;
import com.techsophy.tsf.runtime.form.dto.SequenceGeneratorResponse;
import com.techsophy.tsf.runtime.form.model.ApiResponse;
import com.techsophy.tsf.runtime.form.service.SequenceGeneratorService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import static com.techsophy.tsf.runtime.form.constants.SequenceGeneratorConstants.SEQUENCE_GENERATED_SUCCESS;

@RestController
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class SequenceGeneratorImpl implements SequenceGeneratorController
{
    private SequenceGeneratorService idGeneratorService;
    private GlobalMessageSource globalMessageSource;

    @Override
    public ApiResponse<SequenceGeneratorResponse> generateSequence(SequenceGeneratorDTO idGeneratorDTO)
    {
        return new ApiResponse<>(idGeneratorService.generateSequence(idGeneratorDTO),true,globalMessageSource.get(SEQUENCE_GENERATED_SUCCESS));
    }
}
