package com.fms.system.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventData {
    @JsonProperty("ep")
    private String ep;

    @JsonProperty("bd")
    private String bd;

    @JsonProperty("at")
    private Integer at;

    @JsonProperty("rc")
    private Integer rc;
}
