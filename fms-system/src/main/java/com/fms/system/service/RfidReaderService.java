package com.fms.system.service;

import com.fms.common.exception.ServiceException;
import com.fms.common.utils.StringUtils;
import com.fms.system.domain.CardIssue;
import com.fms.system.mapper.CardIssueMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RfidReaderService {

    @Value("${rfid.reader.mock:true}")
    private boolean mockMode;

    @Autowired(required = false)
    private RealRfidReaderService realReaderService;

    @Autowired(required = false)
    private MockRfidReaderService mockReaderService;
    @Autowired
    private CardIssueMapper cardIssueMapper;
    @PostConstruct
    public void init() {
        if (mockMode) {
            log.info("=== RFID服务运行在 MOCK 模式 ===");
            // 确保 mockReaderService 已初始化（若未注入则手动创建）
            if (mockReaderService == null) {
                mockReaderService = new MockRfidReaderService();
            }
        } else {
            if (realReaderService != null) {
                log.info("=== RFID服务运行在 真实 模式 ===");
            } else {
                log.warn("真实模式但RealRfidReaderService未加载，回退到Mock模式");
                mockMode = true;
                if (mockReaderService == null) {
                    mockReaderService = new MockRfidReaderService();
                }
            }
        }
    }

    /**
     * 单次读取卡片（返回EPC）
     */
    public Map<String, Object> readSingleTag() {
        if (mockMode) {
            if (mockReaderService == null) {
                mockReaderService = new MockRfidReaderService();
            }
            return mockReaderService.readSingleTag();
        } else if (realReaderService != null) {
            return realReaderService.readSingleTag();
        } else {
            log.warn("真实模式不可用，使用Mock降级");
            if (mockReaderService == null) {
                mockReaderService = new MockRfidReaderService();
            }
            return mockReaderService.readSingleTag();
        }
    }

    // ================== 新增：写入EPC ==================
    /**
     * 写入指定EPC
     * @param cardIssue 新的EPC值（十六进制字符串）
     * @return 操作结果（success, message 等）
     */
    public Map<String, Object> writeEpc(CardIssue cardIssue) {
        // 校验卡类型数量限制
        Map<String, Object> result = validateCardTypeLimit(cardIssue.getId(), cardIssue.getPersonCode(), cardIssue.getCardType(), null);
        if (result.get("success") != null && !(Boolean) result.get("success")) {
            return result;// 如果校验失败，直接返回结果
        }
        if (mockMode) {// 判断是否为 Mock 模式
            if (mockReaderService == null) {// 如果 Mock 服务未初始化，则手动创建
                mockReaderService = new MockRfidReaderService();
            }
            return mockReaderService.writeEpc(cardIssue);// 调用 Mock 服务写入 EPC
        } else if (realReaderService != null) {
            return realReaderService.writeEpc(cardIssue);// 如果是真实模式且服务可用，调用真实服务写入 EPC
        } else {
            log.warn("真实模式不可用，使用 Mock 降级");
            if (mockReaderService == null) {
                mockReaderService = new MockRfidReaderService();
            }
            return mockReaderService.writeEpc(cardIssue);// 调用 Mock 服务写入 EPC
        }
    }
    /**
     * 校验卡类型数量限制（主卡≤1，副卡1≤1,副卡2≤1）
     * @param personCode 人员编码
     * @param cardType   卡类型
     * @param excludeId  需排除的记录ID（插入时传null）
     */
    private Map<String, Object>  validateCardTypeLimit(Long id,String personCode, String cardType, Long excludeId) {
        Map<String, Object> result = new HashMap<>();


        if (StringUtils.isBlank(personCode) || StringUtils.isBlank(cardType)) {
            result.put("success", false);
            result.put("message", "人员编码和卡类型不能为空");
        }
        int count = 0;
        if(null != id){
            count = cardIssueMapper.countByPersonCodeAndCardTypeAndStatus(personCode, cardType,"1", excludeId);
        }else{
            count= cardIssueMapper.countByPersonCodeAndCardType(personCode, cardType, null);
        }

        if ("主卡".equals(cardType) && count >= 1) {
            result.put("success", false);
            result.put("message", "该人员已存在主卡，不能再新增主卡");
        }
        if ("副卡1".equals(cardType) && count >= 1) {
            result.put("success", false);
            result.put("message", "该人员已存在副卡1，不能再新增副卡");
        }
        if ("副卡2".equals(cardType) && count >= 1) {
            result.put("success", false);
            result.put("message", "该人员已存在副卡2，不能再新增副卡");
        }
        return result;
    }
    // ================== 新增：获取是否Mock模式 ==================
    public boolean isMockMode() {
        return mockMode;
    }

    // ================== 以下为原有方法（保持不变） ==================

    /**
     * 开始连续读取
     */
    public boolean startReading() {
        if (mockMode) {
            if (mockReaderService == null) {
                mockReaderService = new MockRfidReaderService();
            }
            return mockReaderService.startReading();
        } else if (realReaderService != null) {
            return realReaderService.startReading();
        }
        return false;
    }

    /**
     * 停止读取
     */
    public void stopReading() {
        if (mockMode) {
            if (mockReaderService != null) {
                mockReaderService.stopReading();
            }
        } else if (realReaderService != null) {
            realReaderService.stopReading();
        }
    }

    /**
     * 获取当前已读取的标签列表
     */
    public List<String> getCurrentTags() {
        if (mockMode) {
            if (mockReaderService == null) {
                mockReaderService = new MockRfidReaderService();
            }
            return mockReaderService.getCurrentTags();
        } else if (realReaderService != null) {
            return realReaderService.getCurrentTags();
        }
        return List.of();
    }

    /**
     * 清空标签缓存
     */
    public void clearTags() {
        if (mockMode) {
            if (mockReaderService != null) {
                mockReaderService.clearTags();
            }
        } else if (realReaderService != null) {
            realReaderService.clearTags();
        }
    }

    /**
     * 读写器连接状态
     */
    public boolean isConnected() {
        if (mockMode) {
            return mockReaderService != null && mockReaderService.isConnected();
        } else if (realReaderService != null) {
            return realReaderService.isConnected();
        }
        return false;
    }

    /**
     * 是否正在读取
     */
    public boolean isReading() {
        if (mockMode) {
            return mockReaderService != null && mockReaderService.isReading();
        } else if (realReaderService != null) {
            return realReaderService.isReading();
        }
        return false;
    }
}