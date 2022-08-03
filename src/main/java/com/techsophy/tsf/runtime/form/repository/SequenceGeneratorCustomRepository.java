package com.techsophy.tsf.runtime.form.repository;

import com.techsophy.tsf.runtime.form.entity.SequenceGeneratorDefinition;

public interface SequenceGeneratorCustomRepository
{
     boolean existsBySequenceNameAndLength(String sequenceName,int length);
     SequenceGeneratorDefinition findBySequenceNameAndLength(String sequenceName,int length);
}
