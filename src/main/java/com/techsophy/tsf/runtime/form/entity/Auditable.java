package com.techsophy.tsf.runtime.form.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import java.math.BigInteger;
import java.time.Instant;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class Auditable
{
    private BigInteger createdById;
    private Instant createdOn;
    private BigInteger updatedById;
    private Instant updatedOn;
}
