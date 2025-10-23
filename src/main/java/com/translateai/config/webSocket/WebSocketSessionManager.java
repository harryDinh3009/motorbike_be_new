package com.translateai.config.webSocket;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {

    private final Map<String, SessionWebSocketInfo> sessionInfoMap = new ConcurrentHashMap<>();

    public void storeSessionInfo(String sessionId, SessionWebSocketInfo sessionWebSocketInfo) {
        sessionInfoMap.put(sessionId, sessionWebSocketInfo);
    }

    public SessionWebSocketInfo getSessionInfo(String sessionId) {
        return sessionInfoMap.get(sessionId);
    }

    public void removeSessionInfo(String sessionId) {
        sessionInfoMap.remove(sessionId);
    }

}
