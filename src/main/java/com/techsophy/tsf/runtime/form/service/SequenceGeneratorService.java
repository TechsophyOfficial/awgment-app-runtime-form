package com.techsophy.tsf.runtime.form.service;

import com.techsophy.tsf.runtime.form.dto.SequenceGeneratorDTO;
import com.techsophy.tsf.runtime.form.dto.SequenceGeneratorResponse;

public interface SequenceGeneratorService
{
    SequenceGeneratorResponse generateSequence(SequenceGeneratorDTO idGeneratorDTO);
}
