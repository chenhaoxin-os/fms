package com.fms.system.service;

import com.fms.system.domain.CardIssue;
import com.fms.system.mapper.CardIssueMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class MockRfidReaderService {

    private final List<String> mockTags = new ArrayList<>();
    private final AtomicBoolean reading = new AtomicBoolean(false);
    private final AtomicBoolean connected = new AtomicBoolean(true);

    @Autowired
    private CardIssueMapper mardIssueMapper;
    public Map<String, Object> readSingleTag() {
        Map<String, Object> result = new HashMap<>();
        if (mockTags.isEmpty()) {
            // 模拟生成一个假EPC（12字节）
            String mockEpc = "30313233343536373839404142434445";
            mockTags.add(mockEpc);
        }
        result.put("success", true);
        result.put("epc", mockTags.get(0));
        result.put("count", mockTags.size());
        log.info("Mock读取到EPC: {}", mockTags.get(0));
        return result;
    }

    public Map<String, Object> writeEpc(CardIssue cardIssue) {
        Map<String, Object> result = new HashMap<>();
        if (!mockTags.isEmpty()) {
            mockTags.set(0, cardIssue.getEpcCode());
        } else {
            mockTags.add(cardIssue.getEpcCode());
        }
        result.put("success", true);
        result.put("message", "Mock写入成功");
        log.info("Mock写入EPC: {}", cardIssue.getEpcCode());
        if (null != cardIssue.getId()){
            cardIssue.setCardStatus("1");
            mardIssueMapper.updateCardIssue(cardIssue);
        }else{
            cardIssue.setIssueTime(new java.util.Date());
            cardIssue.setCardStatus("1");
            mardIssueMapper.insertCardIssue(cardIssue);
        }
        return result;
    }

    public boolean startReading() {
        reading.set(true);
        return true;
    }

    public void stopReading() {
        reading.set(false);
    }

    public List<String> getCurrentTags() {
        return new ArrayList<>(mockTags);
    }

    public void clearTags() {
        mockTags.clear();
    }

    public boolean isConnected() {
        return connected.get();
    }

    public boolean isReading() {
        return reading.get();
    }
}