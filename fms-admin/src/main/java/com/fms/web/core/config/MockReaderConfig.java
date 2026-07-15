package com.fms.web.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
public class MockReaderConfig {

    @Value("${rfid.reader.mock:true}")
    private boolean mockMode;

    @PostConstruct
    public void init() {
        if (mockMode) {
            log.info("╔════════════════════════════════════════════════════════════╗");
            log.info("║                    MOCK 模式已启用                          ║");
            log.info("║  说明：                                                  ║");
            log.info("║  1. 读写器使用模拟数据，无需连接真实设备                    ║");
            log.info("║  2. 点击【读取卡片】会返回模拟的EPC码                      ║");
            log.info("║  3. 适用于Mac开发和前端调试                               ║");
            log.info("║                                                          ║");
            log.info("║  如需使用真实读写器，请修改application.yml：              ║");
            log.info("║  rfid.reader.mock: false                                 ║");
            log.info("╚════════════════════════════════════════════════════════════╝");
        }
    }
}
