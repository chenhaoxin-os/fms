package com.fms.web.core.config;
import com.fms.system.service.LongTermAsyncRead;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "rfid.reader.mock", havingValue = "false", matchIfMissing = false)
public class RfidConfig {

    @Value("${rfid.reader.port:/dev/ttyUSB0}")
    private String comPort;

    @Value("${rfid.reader.antenna-count:1}")
    private int antennaCount;

    @Bean
    public LongTermAsyncRead longTermAsyncRead() {
        return new LongTermAsyncRead(comPort, antennaCount);
    }
}