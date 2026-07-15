package com.fms.web.controller.system;


import com.fms.system.domain.EventData;
import com.fms.system.domain.ReaderEvent;
import com.fms.system.service.IDeviceService;
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
    private IDeviceService deviceService;

    @PostMapping("/")
    public Map<String, Object> handleTagRead(
            @RequestBody ReaderEvent request,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIp(httpRequest);
        log.info("========== 收到RFID标签上报请求 ==========");
        log.info("客户端IP: {}", clientIp);
        log.info("请求头: {}", getHeaders(httpRequest));
        log.info("请求体: {}", request);

        // 打印详细信息
        log.info("读写器名称: {}", request.getReaderName());
        log.info("事件类型: {}", request.getEventType());
        log.info("标签数量: {}", request.getEventData().size());
        for (EventData eventData : request.getEventData()) {
            log.info("标签EPC: {}, 天线: {}, 读取次数: {}",
                    eventData.getEp(), eventData.getAt(), eventData.getRc());

        }

        // 返回成功响应
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        response.put("timestamp", System.currentTimeMillis());

        log.info("响应: {}", response);
        return response;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, request.getHeader(name));
        }
        return headers;
    }
}
