// src/utils/websocket.js
class VehicleWebSocket {
    constructor(options = {}) {
        this.url = options.url || `ws://${window.location.host}/websocket/vehicle`;
        this.onMessage = options.onMessage || (() => {});
        this.onOpen = options.onOpen || (() => {});
        this.onClose = options.onClose || (() => {});
        this.onError = options.onError || (() => {});
        this.reconnectInterval = options.reconnectInterval || 3000;
        this.maxReconnectAttempts = options.maxReconnectAttempts || 10;

        this.ws = null;
        this.reconnectAttempts = 0;
        this.isManualClose = false;
        this.heartbeatTimer = null;

        this.connect();
    }

    connect() {
        try {
            this.ws = new WebSocket(this.url);

            this.ws.onopen = (event) => {
                console.log('WebSocket 连接成功');
                this.reconnectAttempts = 0;
                this.isManualClose = false;
                this.onOpen(event);
                this.startHeartbeat();
            };

            this.ws.onmessage = (event) => {
                try {
                    const data = JSON.parse(event.data);
                    this.onMessage(data);
                } catch (e) {
                    console.error('解析 WebSocket 消息失败：', e);
                }
            };

            this.ws.onclose = (event) => {
                console.log('WebSocket 连接关闭');
                this.stopHeartbeat();
                this.onClose(event);
                if (!this.isManualClose) {
                    this.reconnect();
                }
            };

            this.ws.onerror = (error) => {
                console.error('WebSocket 错误：', error);
                this.onError(error);
            };
        } catch (error) {
            console.error('WebSocket 连接失败：', error);
            this.reconnect();
        }
    }

    reconnect() {
        if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            console.warn('WebSocket 重连次数已达上限');
            return;
        }
        this.reconnectAttempts++;
        console.log(`WebSocket 尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`);
        setTimeout(() => {
            this.connect();
        }, this.reconnectInterval);
    }

    send(data) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(typeof data === 'string' ? data : JSON.stringify(data));
        } else {
            console.warn('WebSocket 未连接，无法发送消息');
        }
    }

    close() {
        this.isManualClose = true;
        this.stopHeartbeat();
        if (this.ws) {
            this.ws.close();
        }
    }

    startHeartbeat() {
        this.stopHeartbeat();
        this.heartbeatTimer = setInterval(() => {
            if (this.ws && this.ws.readyState === WebSocket.OPEN) {
                this.ws.send('PING');
            }
        }, 30000);
    }

    stopHeartbeat() {
        if (this.heartbeatTimer) {
            clearInterval(this.heartbeatTimer);
            this.heartbeatTimer = null;
        }
    }
}

export default VehicleWebSocket;
