package com.fms.system.service;

import com.fms.system.domain.EventData;
import com.fms.system.domain.TagState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class TagStateService {

    // 所有标签状态缓存
    private final ConcurrentHashMap<String, TagState> stateMap = new ConcurrentHashMap<>();

    /**
     * 处理一批上报事件，更新每个标签的状态
     */
    public void processEvents(List<EventData> events) {
        long now = System.currentTimeMillis();
        for (EventData event : events) {
            String epc = event.getEp();
            TagState state = stateMap.computeIfAbsent(epc, k -> {
                TagState s = new TagState();
                s.setEpc(epc);
                s.setLastSeen(now);
                s.setSeriesStart(now);
                s.setInCar(false);
                return s;
            });

            long gap = (now - state.getLastSeen()) / 1000; // 距上次上报的秒数

            // 更新最后出现时间
            state.setLastSeen(now);

            if (gap <= 2) {
                // 连续上报：计算从 seriesStart 到现在的持续时间
                long continuousSeconds = (now - state.getSeriesStart()) / 1000;
                // 如果在车状态已标记，且连续时间不足10秒（可能中途中断后恢复），但此处仍保持，不重复触发
                if (!state.isInCar() && continuousSeconds >= 10) {
                    // 在车判定
                    state.setInCar(true);
                    state.setCarEnterTime(now);
                    log.info("【在车判定】EPC={} 连续出现 {} 秒，标记为在车", epc, continuousSeconds);
                    // 可在此触发业务事件（如入库、推送）
                }
            } else {
                // 中断：重置连续序列开始时间
                state.setSeriesStart(now);
                // 注意：如果在车状态，但中断超过2秒，不会立即离车，由定时任务处理
                // 如果中断但仍在车，我们保留状态，等待定时任务超时离车
            }
        }
    }

    /**
     * 定时扫描在车标签，检测是否离车（30秒无上报）
     * 每10秒执行一次
     */
    @Scheduled(fixedDelay = 10000)
    public void checkCarTimeout() {
        long now = System.currentTimeMillis();
        stateMap.forEach((epc, state) -> {
            if (state.isInCar()) {
                long gap = (now - state.getLastSeen()) / 1000;
                if (gap >= 30) {
                    state.setInCar(false);
                    log.info("【离车判定】EPC={} 最后出现于 {} 秒前，判定离车", epc, gap);
                    // 可触发离车事件
                }
            }
        });
    }

    /**
     * 定期清理 5 分钟未出现的标签，释放内存
     */
    @Scheduled(fixedDelay = 60000)
    public void cleanStale() {
        long now = System.currentTimeMillis();
        stateMap.entrySet().removeIf(entry -> {
            long gap = (now - entry.getValue().getLastSeen()) / 1000;
            if (gap > 300) { // 5分钟
                log.debug("清理过期标签 EPC={}", entry.getKey());
                return true;
            }
            return false;
        });
    }

    // 可选：提供查询接口
    public ConcurrentHashMap<String, TagState> getStateMap() {
        return stateMap;
    }
}
