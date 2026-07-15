package com.fms.system.service;

import com.fms.system.domain.CardIssue;
import com.fms.system.mapper.CardIssueMapper;
import com.uhf.api.cls.Reader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 真实 RFID 读写器服务，使用 USB 串口模式
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "rfid.reader.mock", havingValue = "false", matchIfMissing = false)
public class RealRfidReaderService {

    @Value("${rfid.reader.port:/dev/ttyUSB0}")   // Linux 默认，Windows 可改为 COM4
    private String comPort;

    @Value("${rfid.reader.antenna-count:1}")      // 天线端口数量，根据实际设备修改
    private int antennaCount;

    @Autowired
    private CardIssueMapper cardIssueMapper;
    @Autowired
    private LongTermAsyncRead readerTask;
    private boolean connected = false;

    @PostConstruct
    public void init() {
        log.info("初始化真实 RFID 读写器（USB 串口模式），端口={}, 天线数={}", comPort, antennaCount);
        Reader.READER_ERR err = readerTask.openReader();
        if (err == Reader.READER_ERR.MT_OK_ERR) {
            connected = true;
            log.info("读写器连接成功");
            readerTask.startReadTags();
            log.info("已启动快速盘点");
        } else {
            connected = false;
            log.error("读写器连接失败，错误码：{}", err);
        }
    }

    @PreDestroy
    public void destroy() {
        log.info("关闭 RFID 读写器");
        if (readerTask != null) {
            readerTask.stopReadTask();
            Reader rdr = readerTask.getReader();
            if (rdr != null) {
                rdr.CloseReader();
            }
        }
        connected = false;
    }

    /**
     * 单次读取标签（取第一个）
     */
    public Map<String, Object> readSingleTag() {
        Map<String, Object> result = new HashMap<>();
        if (!connected || readerTask == null || !readerTask.isRunning()) {
            result.put("success", false);
            result.put("message", "读写器未连接或未启动盘点");
            return result;
        }
        List<String> epcs = readerTask.readAllEpc();
        if (epcs.isEmpty()) {
            result.put("success", false);
            result.put("message", "未读取到标签");
            result.put("epc", null);
            result.put("count", 0);
        } else {
            result.put("success", true);
            result.put("epc", epcs.get(0));
            result.put("count", epcs.size());
            result.put("message", "读取成功");
        }
        return result;
    }

    /**
     * 写入指定 EPC
     */
    public Map<String, Object> writeEpc(CardIssue cardIssue) {
        Map<String, Object> result = new HashMap<>();
        if (!connected || readerTask == null) {
            result.put("success", false);
            result.put("message", "读写器未连接");
            return result;
        }
        boolean success = readerTask.writeEpc(cardIssue.getEpcCode());
        result.put("success", success);
        result.put("message", success ? "写入成功" : "写入失败，请确认标签在读写范围内且密码正确");
        if(success){
            if (null != cardIssue.getId()){
                cardIssue.setCardStatus("1");
                cardIssueMapper.updateCardIssue(cardIssue);
            }else{
                cardIssue.setIssueTime(new java.util.Date());
                cardIssue.setCardStatus("1");
                cardIssueMapper.insertCardIssue(cardIssue);
            }
        }
        return result;
    }

    // 以下为控制方法，供 RfidReaderService 调用
    public boolean startReading() {
        if (readerTask != null && !readerTask.isRunning()) {
            readerTask.startReadTags();
            return true;
        }
        return false;
    }

    public void stopReading() {
        if (readerTask != null) {
            readerTask.stopReadTask();
        }
    }

    public List<String> getCurrentTags() {
        if (readerTask != null && readerTask.isRunning()) {
            return readerTask.readAllEpc();
        }
        return List.of();
    }

    public void clearTags() {
        // 无缓存操作，忽略
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isReading() {
        return readerTask != null && readerTask.isRunning();
    }
}