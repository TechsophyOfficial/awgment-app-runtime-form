package com.techsophy.tsf.runtime.form.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Status {
    @JsonProperty("enabled")
    ENABLED,
    @JsonProperty("disabled")
    DISABLED
}
