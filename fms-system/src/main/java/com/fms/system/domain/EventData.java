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

    @JsonProperty("fq")
    private Integer fq;

    @JsonProperty("pt")
    private Integer pt;

    @JsonProperty("ri")
    private Integer ri;

    @JsonProperty("rv")
    private Integer rv;

    @JsonProperty("ft")
    private Integer ft;

    @JsonProperty("lt")
    private Integer lt;

}
