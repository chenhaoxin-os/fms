package com.fms.system.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fms.system.domain.Device;
import com.fms.system.domain.EventData;
import com.fms.system.domain.TagState;
import com.fms.system.domain.Vehicle;
import com.fms.system.socket.VehicleWebSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class TagStateService {

    @Value("${tag.state.continuous-threshold:10}")
    private int continuousThreshold;

    @Value("${tag.state.leave-car-threshold:30}")
    private int leaveCarThreshold;

    @Autowired
    private IDeviceService iDeviceService;

    @Autowired
    private IVehicleService iVehicleService;

    @Autowired
    private ICardIssueService iCardIssueService;
    // 所有标签状态缓存
    private final ConcurrentHashMap<String, TagState> stateMap = new ConcurrentHashMap<>();

    /**
     * 处理一批上报事件，更新每个标签的状态
     */
    public void processEvents(List<EventData> events,String deviceCode) {
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

            long gap = (now - state.getLastSeen()) / 1000;
            state.setLastSeen(now);

            // 🔥 关键修复：如果标签已离车，重新出现时重置 seriesStart
            if (!state.isInCar()&& gap > 5) {
                state.setSeriesStart(now);
                log.info("【重新进入】EPC={} 离车后重新出现，重置连续计数", epc);
            }

            if (gap <= 5) {
                long continuousSeconds = (now - state.getSeriesStart()) / 1000;
                if (!state.isInCar() && continuousSeconds >= continuousThreshold) {
                    state.setInCar(true);
                    state.setCarEnterTime(now);
                    log.info("【在车判定】EPC={} 连续出现 {} 秒，标记为在车", epc, continuousSeconds);
                    Device device = new  Device();
                    device.setDeviceId(deviceCode);
                    List<Device> devices = iDeviceService.selectDeviceList(device);
                    if(null != devices && devices.size() > 0){
                        Vehicle vehicle = new  Vehicle();
                        vehicle.setDeviceId(devices.get(0).getId());
                        List<Vehicle> vehicles = iVehicleService.selectVehicleList(vehicle);
                        if(null != vehicles && vehicles.size() > 0){
                            String stableCrewJson = vehicles.get(0).getStableCrew();

                            // 获取或创建 JSONArray
                            JSONArray jsonArray;
                            String name = iCardIssueService.getNameByEpcCode(epc);

                            if (stableCrewJson != null && !stableCrewJson.isEmpty()) {
                                // 不为空：解析现有 JSON
                                jsonArray = JSON.parseArray(stableCrewJson);

                                // 使用 Map 去重，key 为 name，value 为 JSONObject
                                Map<String, JSONObject> crewMap = new LinkedHashMap<>(); // 保持顺序
                                for (int i = 0; i < jsonArray.size(); i++) {
                                    JSONObject item = jsonArray.getJSONObject(i);
                                    crewMap.put(item.getString("name"), item);
                                }

                                // 添加新成员（如果存在则覆盖）
                                JSONObject newCrew = new JSONObject();
                                newCrew.put("name", name);
                                crewMap.put(name, newCrew);

                                // 转换回 JSONArray
                                JSONArray newJsonArray = new JSONArray();
                                for (JSONObject crew : crewMap.values()) {
                                    newJsonArray.add(crew);
                                }
                                jsonArray = newJsonArray;
                            } else {
                                // 为空：新建 JSONArray 并添加第一个成员
                                jsonArray = new JSONArray();
                                JSONObject newCrew = new JSONObject();
                                newCrew.put("name", name);
                                jsonArray.add(newCrew);
                            }

// 更新车辆
                            vehicles.get(0).setStableCrew(JSON.toJSONString(jsonArray));
                            iVehicleService.updateVehicle(vehicles.get(0));
                            VehicleWebSocket.broadcastVehicleData();
                        }

                    }
                }
            } else {
                log.info("【中断】EPC={} 距上次上报 {} 秒（>5秒），连续计数继续累计", epc, gap);
            }
        }
    }

    /**
     * 定时扫描在车标签，检测是否离车（30秒无上报）
     * 每10秒执行一次
     */
    @Scheduled(fixedDelay = 1000)
    public void checkCarTimeout() {
        long now = System.currentTimeMillis();
        stateMap.forEach((epc, state) -> {
            if (state.isInCar()) {
                long gap = (now - state.getLastSeen()) / 1000;
                if (gap >= leaveCarThreshold) {
                    state.setInCar(false);
                    state.setLastSeen(now);
                    state.setSeriesStart(now);
                    log.info("【离车判定】EPC={} 最后出现于 {} 秒前，判定离车", epc, gap);

                    // ========== 根据 epc 查询人员名称，再从所有车辆中删除 ==========
                    try {
                        String name = iCardIssueService.getNameByEpcCode(epc);
                        if (name == null || name.isEmpty()) {
                            log.warn("【离车删除】EPC={} 未查询到对应人员名称，跳过删除", epc);
                            return;
                        }
                        // 从所有车辆的 stableCrew 中删除该人员
                        removeCrewFromAllVehicles(name, epc);
                        VehicleWebSocket.broadcastVehicleData();
                    } catch (Exception e) {
                        log.error("【离车删除】EPC={} 删除稳定人员失败", epc, e);
                    }
                }
            }
        });
    }

    /**
     * 从所有车辆的 stableCrew 中按人员名称删除
     * @param name 人员名称
     * @param epc 标签EPC（仅用于日志）
     */
    private void removeCrewFromAllVehicles(String name, String epc) {
        // 1. 查询所有车辆
        List<Vehicle> allVehicles = iVehicleService.selectVehicleList(new Vehicle());
        if (allVehicles == null || allVehicles.isEmpty()) {
            log.debug("【离车删除】没有车辆数据，无需删除");
            return;
        }

        boolean deleted = false;
        for (Vehicle vehicle : allVehicles) {
            String stableCrewJson = vehicle.getStableCrew();
            if (stableCrewJson == null || stableCrewJson.isEmpty()) {
                continue; // 该车辆没有稳定人员，跳过
            }

            // 2. 解析 JSONArray
            JSONArray jsonArray = JSON.parseArray(stableCrewJson);
            if (jsonArray == null || jsonArray.isEmpty()) {
                continue;
            }

            // 3. 过滤掉名称为 name 的成员
            JSONArray newArray = new JSONArray();
            boolean removed = false;
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                String itemName = item.getString("name");
                if (!name.equals(itemName)) {
                    newArray.add(item);
                } else {
                    removed = true;
                }
            }

            // 4. 如果有删除，更新车辆
            if (removed) {
                vehicle.setStableCrew(JSON.toJSONString(newArray));
                iVehicleService.updateVehicle(vehicle);
                deleted = true;
                log.info("【离车删除】人员 {} (EPC={}) 已从车辆 {} 的稳定人员中移除",
                        name, epc, vehicle.getDeviceId());
            }
        }

        if (!deleted) {
            log.info("【离车删除】人员 {} (EPC={}) 未在任何车辆的稳定人员列表中找到", name, epc);
        }
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
