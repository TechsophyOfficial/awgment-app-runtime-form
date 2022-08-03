package com.techsophy.tsf.runtime.form.controller;

import com.techsophy.tsf.runtime.form.dto.SequenceGeneratorDTO;
import com.techsophy.tsf.runtime.form.dto.SequenceGeneratorResponse;
import com.techsophy.tsf.runtime.form.model.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static com.techsophy.tsf.runtime.form.constants.SequenceGeneratorConstants.SEQUENCE_NEXT;

@RequestMapping(BASE_URL+VERSION_V1)
public interface SequenceGeneratorController
{
    @PostMapping(SEQUENCE_NEXT)
    ApiResponse<SequenceGeneratorResponse> generateSequence(@RequestBody SequenceGeneratorDTO idGeneratorDTO);
}
