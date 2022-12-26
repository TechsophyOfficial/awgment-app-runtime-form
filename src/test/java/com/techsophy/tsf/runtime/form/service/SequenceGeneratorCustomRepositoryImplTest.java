package com.techsophy.tsf.runtime.form.service;

import com.techsophy.tsf.runtime.form.entity.SequenceGeneratorDefinition;
import com.techsophy.tsf.runtime.form.repository.impl.SequenceGeneratorCustomRepositoryImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.TEST_ACTIVE_PROFILE;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles(TEST_ACTIVE_PROFILE)
class SequenceGeneratorCustomRepositoryImplTest {
    @Mock
    MongoTemplate mongoTemplate;
    @Mock
    SequenceGeneratorDefinition sequenceGeneratorDefinition;
    @InjectMocks
    SequenceGeneratorCustomRepositoryImpl sequenceGeneratorCustomRepository;

    @Test
    void existsBySequenceNameAndLengthTest(){
        Mockito.when(sequenceGeneratorCustomRepository.existsBySequenceNameAndLength("abc",1)).thenReturn(true);
        boolean response = sequenceGeneratorCustomRepository.existsBySequenceNameAndLength("abc",1);
        Assertions.assertTrue(response);
    }

    @Test
    void findBySequenceNameAndLengthTest(){
        Mockito.when(sequenceGeneratorCustomRepository.findBySequenceNameAndLength("abc",1)).thenReturn(sequenceGeneratorDefinition);
        SequenceGeneratorDefinition response = sequenceGeneratorCustomRepository.findBySequenceNameAndLength("abc",1);
        Assertions.assertNotNull(response);
    }
}
