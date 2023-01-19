package com.techsophy.tsf.runtime.form.dto;

import lombok.Data;
import java.util.Date;

@Data
public class DatePicker
{
    String disable;
    String disableFunction;
    Boolean disableWeekends;
    Boolean disableWeekdays;
    Date minDate;
    Date maxDate;
    Boolean showWeeks;
    Integer startingDay;
    String initDate;
    String minMode;
    String maxMode;
    Integer yearRows;
    Integer yearColumns;
}
