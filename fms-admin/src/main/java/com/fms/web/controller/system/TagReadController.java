package com.fms.web.controller.system;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fms.system.domain.EventData;
import com.fms.system.service.TagStateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class TagReadController {

    @Autowired
    private TagStateService tagStateService;

    @PostMapping("/tagRead")
    public Map<String, Object> handleTagRead(
            @RequestBody Map<String, Object> rawRequest,
            @ModelAttribute HttpServletRequest httpRequest) {

        log.info("========== 收到RFID标签上报请求 ==========");
        log.info("请求体: {}", rawRequest);
        // 手动解析，处理 event_data 可能是数组或整数的情况
        Object eventDataObj = rawRequest.get("event_data");
        List<EventData> eventDataList = new ArrayList<>();

        if (eventDataObj instanceof List) {
            // 是数组，正常解析
            ObjectMapper mapper = new ObjectMapper();
            eventDataList = mapper.convertValue(eventDataObj,
                    new TypeReference<>() {});
            // 委托状态处理
            tagStateService.processEvents(eventDataList, rawRequest.get("reader_name").toString());
            // 可选的详细日志（按需保留）
            for (EventData event : eventDataList) {
                log.debug("EPC: {}, 天线: {}, 次数: {}", event.getEp(), event.getAt(), event.getRc());
            }
        } else if (eventDataObj instanceof Integer) {
            // 是整数，记录日志但忽略
            log.warn("event_data 是整数类型，值: {}, 忽略处理", eventDataObj);
        } else {
            log.warn("event_data 类型异常: {}", eventDataObj.getClass());
        }
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

}
