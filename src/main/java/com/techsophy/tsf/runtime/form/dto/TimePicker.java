package com.techsophy.tsf.runtime.form.dto;

import lombok.Data;

@Data
public class TimePicker
{
    Boolean showMeridian;
    Integer hourStep;
    Integer minuteStep;
    Boolean readonlyInput;
    Boolean mousewheel;
    Boolean arrowkeys;
}
