package com.fms.system.socket;

import com.alibaba.fastjson2.JSONObject;
import com.fms.system.domain.Vehicle;
import com.fms.system.service.IVehicleService;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
@ServerEndpoint("/websocket/vehicle")
public class VehicleWebSocket {

    // 存储所有连接的会话
    private static final CopyOnWriteArraySet<Session> sessions = new CopyOnWriteArraySet<>();

    // 注意：@Autowired 在 WebSocket 中需要特殊处理，使用静态注入
    private static IVehicleService vehicleService;

    @Autowired
    public void setVehicleService(IVehicleService vehicleService) {
        VehicleWebSocket.vehicleService = vehicleService;
    }

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        log.info("WebSocket 连接建立，当前连接数：{}", sessions.size());
        // 连接建立后立即推送一次数据
        sendVehicleData(session);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        log.info("WebSocket 连接关闭，当前连接数：{}", sessions.size());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 错误：", error);
        sessions.remove(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log.debug("收到客户端消息：{}", message);
        // 可以处理客户端发来的消息，比如刷新请求
        if ("REFRESH".equalsIgnoreCase(message)) {
            sendVehicleData(session);
        }
    }

    /**
     * 向单个会话发送车辆数据
     */
    private void sendVehicleData(Session session) {
        try {
            List<Vehicle> vehicles = vehicleService.selectVehicleList(new Vehicle());
            JSONObject result = new JSONObject();
            result.put("type", "vehicleUpdate");
            result.put("data", vehicles);
            result.put("timestamp", System.currentTimeMillis());
            session.getBasicRemote().sendText(result.toJSONString());
        } catch (IOException e) {
            log.error("发送车辆数据失败：", e);
        }
    }

    /**
     * 广播车辆数据给所有客户端（数据变更时调用）
     */
    public static void broadcastVehicleData() {
        if (sessions.isEmpty()) {
            return;
        }
        try {
            List<Vehicle> vehicles = vehicleService.selectVehicleList(new Vehicle());
            JSONObject result = new JSONObject();
            result.put("type", "vehicleUpdate");
            result.put("data", vehicles);
            result.put("timestamp", System.currentTimeMillis());
            String jsonData = result.toJSONString();

            for (Session session : sessions) {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(jsonData);
                }
            }
            log.debug("广播车辆数据完成，连接数：{}", sessions.size());
        } catch (IOException e) {
            log.error("广播车辆数据失败：", e);
        }
    }
}
