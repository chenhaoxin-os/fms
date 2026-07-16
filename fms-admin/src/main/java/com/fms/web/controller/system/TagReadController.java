package com.fms.web.controller.system;


import com.fms.system.domain.EventData;
import com.fms.system.domain.ReaderEvent;
import com.fms.system.service.IDeviceService;
import com.fms.system.service.TagStateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class TagReadController {

    @Autowired
    private TagStateService tagStateService;

    @PostMapping("/tagRead")
    public Map<String, Object> handleTagRead(
            @RequestBody ReaderEvent request,
            HttpServletRequest httpRequest) {

        log.info("收到RFID上报，读写器: {}, 标签数: {}",
                request.getReaderName(), request.getEventData().size());

        // 委托状态处理
        tagStateService.processEvents(request.getEventData());

        // 可选的详细日志（按需保留）
        for (EventData event : request.getEventData()) {
            log.debug("EPC: {}, 天线: {}, 次数: {}", event.getEp(), event.getAt(), event.getRc());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

}
