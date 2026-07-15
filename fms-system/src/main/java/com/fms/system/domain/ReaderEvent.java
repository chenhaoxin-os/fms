package com.fms.system.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReaderEvent {
    @JsonProperty("reader_name")
    private String readerName;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("event_data")
    private List<EventData> eventData;
}

