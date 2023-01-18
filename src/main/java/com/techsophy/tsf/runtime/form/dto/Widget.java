package com.techsophy.tsf.runtime.form.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Date;

@Data
public class Widget
{
    String type;
    String displayInTimezone;
    String locale;
    Boolean useLocaleSettings;
    Boolean allowInput;
    String mode;
    Boolean enableTime;
    Boolean noCalendar;
    String format;
    Integer hourIncrement;
    Integer minuteIncrement;
    @JsonProperty("time_24hr")
    Boolean time;
    Date minDate;
    String diabledDates;
    Boolean disableWeekends;
    Boolean disableWeekdays;
    String disableFunction;
    Date maxDate;
}
