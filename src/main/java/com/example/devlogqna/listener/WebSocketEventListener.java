package com.example.devlogqna.listener;

import com.example.devlogqna.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final ChatSessionService chatSessionService;

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        chatSessionService.removeSession(event.getSessionId());
    }
}
