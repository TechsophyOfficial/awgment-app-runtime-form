package com.techsophy.tsf.runtime.form.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.io.Serializable;
import static com.techsophy.tsf.runtime.form.constants.SequenceGeneratorConstants.TP_SEQUENCE_GENERATOR;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = TP_SEQUENCE_GENERATOR)
public class SequenceGeneratorDefinition extends  Auditable implements Serializable
{
    private static final long serialVersionUID = 1L;
    @Id
    private Long id;
    private int length;
    private String sequenceName;
    private Long lastValue;
}
