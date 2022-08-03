package com.techsophy.tsf.runtime.form.service.impl;

import com.techsophy.idgenerator.IdGeneratorImpl;
import com.techsophy.tsf.runtime.form.dto.SequenceGeneratorDTO;
import com.techsophy.tsf.runtime.form.dto.SequenceGeneratorResponse;
import com.techsophy.tsf.runtime.form.entity.SequenceGeneratorDefinition;
import com.techsophy.tsf.runtime.form.repository.SequenceGeneratorRepository;
import com.techsophy.tsf.runtime.form.service.SequenceGeneratorService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import static com.techsophy.tsf.runtime.form.constants.SequenceGeneratorConstants.*;

@Service
@AllArgsConstructor
public class SequenceGeneratorServiceImpl implements SequenceGeneratorService
{
    private SequenceGeneratorRepository sequenceGeneratorRepository;
    private IdGeneratorImpl idGeneratorImpl;

    @Override
    public SequenceGeneratorResponse generateSequence(SequenceGeneratorDTO idGeneratorDTO)
    {
        String sequenceName=idGeneratorDTO.getSequenceName();
        int length= Integer.parseInt((idGeneratorDTO.getLength()));
        SequenceGeneratorDefinition existingDefinition;
        SequenceGeneratorDefinition sequenceGeneratorDefinition=new SequenceGeneratorDefinition();
        sequenceGeneratorDefinition.setUpdatedOn(Instant.now());
        if(sequenceGeneratorRepository.existsBySequenceNameAndLength(sequenceName,length))
        {
           existingDefinition = sequenceGeneratorRepository.findBySequenceNameAndLength(sequenceName,length);
           sequenceGeneratorDefinition.setSequenceName(existingDefinition.getSequenceName());
           sequenceGeneratorDefinition.setLength(existingDefinition.getLength());
           Long lastValue;
           if(existingDefinition.getLastValue()>(Math.pow(10,existingDefinition.getLength())-1))
           {
               lastValue= -1L;
           }
           else
           {
               lastValue= existingDefinition.getLastValue();
           }
           sequenceGeneratorDefinition.setLastValue(lastValue+1);
           sequenceGeneratorDefinition.setCreatedOn(existingDefinition.getCreatedOn());
           sequenceGeneratorDefinition.setId(existingDefinition.getId());
           sequenceGeneratorRepository.save(sequenceGeneratorDefinition);
        }
        else
        {
            sequenceGeneratorDefinition.setId(Long.valueOf(String.valueOf(idGeneratorImpl.nextId())));
            sequenceGeneratorDefinition.setLength(length);
            sequenceGeneratorDefinition.setLastValue(1L);
            sequenceGeneratorDefinition.setSequenceName(sequenceName);
            sequenceGeneratorDefinition.setCreatedOn(Instant.now());
            sequenceGeneratorRepository.save(sequenceGeneratorDefinition);
        }
        String format=PERCENTAGE_ZERO+sequenceGeneratorDefinition.getLength()+D;
        String formattedValue= String.format(format,sequenceGeneratorDefinition.getLastValue());
        Date createdOn=Date.from(sequenceGeneratorDefinition.getCreatedOn());
        Date updatedOn=Date.from(sequenceGeneratorDefinition.getUpdatedOn());
        String formattedCreatedOn=new SimpleDateFormat(DATE_FORMAT).format(createdOn);
        String formattedUpdatedOn=new SimpleDateFormat(DATE_FORMAT).format(updatedOn);
        return new SequenceGeneratorResponse(sequenceGeneratorDefinition.getLength(),sequenceGeneratorDefinition.getSequenceName(),formattedValue,formattedCreatedOn,formattedUpdatedOn);
    }
}
