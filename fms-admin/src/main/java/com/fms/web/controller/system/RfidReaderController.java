package com.fms.web.controller.system;
import com.fms.system.domain.CardIssue;
import com.fms.system.service.RfidReaderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/system/rfid")
@CrossOrigin(origins = "*")
public class RfidReaderController {

    @Autowired
    private RfidReaderService rfidService;

    @PostMapping("/readCard")
    public Map<String, Object> readCard() {
        log.info("=== 收到读卡API请求 ===");
        Map<String, Object> result = rfidService.readSingleTag();
        log.info("读卡结果: {}", result);
        return result;
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        log.info("=== 收到状态查询请求 ===");
        Map<String, Object> result = new HashMap<>();
        result.put("connected", rfidService.isConnected());
        result.put("reading", rfidService.isReading());
        result.put("mock", true);
        return result;
    }

    // ================== 新增：写入EPC接口 ==================
    @PostMapping("/writeEpc")
    public Map<String, Object> writeEpc(@RequestBody CardIssue cardIssue) {
        log.info("=== 收到写EPC请求，EPC={} ===", cardIssue.getEpcCode());
        Map<String, Object> result = rfidService.writeEpc(cardIssue);
        log.info("写EPC结果: {}", result);
        return result;
    }
}
