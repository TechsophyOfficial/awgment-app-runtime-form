package com.techsophy.tsf.runtime.form.service;

import com.techsophy.idgenerator.IdGeneratorImpl;
import com.techsophy.tsf.runtime.form.dto.SequenceGeneratorDTO;
import com.techsophy.tsf.runtime.form.dto.SequenceGeneratorResponse;
import com.techsophy.tsf.runtime.form.entity.SequenceGeneratorDefinition;
import com.techsophy.tsf.runtime.form.repository.SequenceGeneratorRepository;
import com.techsophy.tsf.runtime.form.service.impl.SequenceGeneratorServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigInteger;
import java.time.Instant;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.STRING;
import static org.mockito.ArgumentMatchers.*;
import static shadow.org.assertj.core.api.Assertions.assertThat;

@ExtendWith({MockitoExtension.class})
class SequenceGeneratorServiceImplTest
{
    @Mock
    SequenceGeneratorRepository sequenceGeneratorRepository;
    @Mock
    IdGeneratorImpl idGeneratorImpl;
    @InjectMocks
    SequenceGeneratorServiceImpl sequenceGeneratorService;

    @Test
    void generateSequence()
    {
        SequenceGeneratorDefinition sequenceGeneratorDefinition=new SequenceGeneratorDefinition(1L,1,STRING,1L);
        sequenceGeneratorDefinition.setCreatedOn(Instant.now());
        sequenceGeneratorDefinition.setUpdatedOn(Instant.now());
        SequenceGeneratorDTO sequenceGeneratorDTO = new SequenceGeneratorDTO();
        sequenceGeneratorDTO.setSequenceName(STRING);
        sequenceGeneratorDTO.setLength("1");
        Mockito.when(idGeneratorImpl.nextId()).thenReturn(BigInteger.ONE);
        Mockito.when(sequenceGeneratorRepository.save(any())).thenReturn(sequenceGeneratorDefinition);
        Mockito.when(sequenceGeneratorRepository.findBySequenceNameAndLength(anyString(),anyInt())).thenReturn(sequenceGeneratorDefinition);
        Mockito.when(sequenceGeneratorRepository.existsBySequenceNameAndLength(anyString(),anyInt())).thenReturn(false).thenReturn(true);
        SequenceGeneratorResponse response = sequenceGeneratorService.generateSequence(sequenceGeneratorDTO);
        SequenceGeneratorResponse response1 = sequenceGeneratorService.generateSequence(sequenceGeneratorDTO);
        assertThat(response).isInstanceOf(SequenceGeneratorResponse.class);
        assertThat(response1).isInstanceOf(SequenceGeneratorResponse.class);
    }
}
