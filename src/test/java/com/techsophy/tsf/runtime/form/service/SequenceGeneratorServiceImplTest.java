package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techsophy.idgenerator.IdGeneratorImpl;
import com.techsophy.tsf.runtime.form.dto.SequenceGeneratorDTO;
import com.techsophy.tsf.runtime.form.dto.SequenceGeneratorResponse;
import com.techsophy.tsf.runtime.form.entity.SequenceGeneratorDefinition;
import com.techsophy.tsf.runtime.form.repository.SequenceGeneratorRepository;
import com.techsophy.tsf.runtime.form.service.impl.SequenceGeneratorServiceImpl;
import com.techsophy.tsf.runtime.form.utils.UserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.time.Instant;
import java.util.*;

import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.NULL;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static shadow.org.assertj.core.api.Assertions.assertThat;

@ExtendWith({MockitoExtension.class})
class SequenceGeneratorServiceImplTest {
  @Mock
  SequenceGeneratorRepository sequenceGeneratorRepository;
  @Mock
  IdGeneratorImpl idGeneratorImpl;
  @Mock
  UserDetails mockUserDetails;
  @InjectMocks
  SequenceGeneratorServiceImpl sequenceGeneratorService;

  @Test
  void generateSequence() throws JsonProcessingException {
    SequenceGeneratorDefinition sequenceGeneratorDefinition = new SequenceGeneratorDefinition(1L, 1, STRING, 1L);
    sequenceGeneratorDefinition.setCreatedOn(String.valueOf(Instant.now()));
    sequenceGeneratorDefinition.setUpdatedOn(String.valueOf(Instant.now()));
    SequenceGeneratorDTO sequenceGeneratorDTO = new SequenceGeneratorDTO();
    sequenceGeneratorDTO.setSequenceName(STRING);
    sequenceGeneratorDTO.setLength("1");
    Mockito.when(mockUserDetails.getCurrentAuditor()).thenReturn(Optional.of(LOGGED_USER_ID));
    Mockito.when(idGeneratorImpl.nextId()).thenReturn(BigInteger.ONE);
    Mockito.when(sequenceGeneratorRepository.save(any())).thenReturn(sequenceGeneratorDefinition);
    Mockito.when(sequenceGeneratorRepository.findBySequenceNameAndLength(anyString(), anyInt())).thenReturn(sequenceGeneratorDefinition);
    Mockito.when(sequenceGeneratorRepository.existsBySequenceNameAndLength(anyString(), anyInt())).thenReturn(false).thenReturn(true);
    SequenceGeneratorResponse response = sequenceGeneratorService.generateSequence(sequenceGeneratorDTO);
    SequenceGeneratorResponse response1 = sequenceGeneratorService.generateSequence(sequenceGeneratorDTO);
    assertThat(response).isInstanceOf(SequenceGeneratorResponse.class);
    assertThat(response1).isInstanceOf(SequenceGeneratorResponse.class);
  }
}
