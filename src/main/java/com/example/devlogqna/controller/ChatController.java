package com.example.devlogqna.controller;

import com.example.devlogqna.dto.ChatMessage;
import com.example.devlogqna.service.ChatService;
import com.example.devlogqna.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ChatSessionService chatSessionService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessage message, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        String jsessionId = (String) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("JSESSIONID");
        String sessionId = (jsessionId != null) ? jsessionId : headerAccessor.getSessionId();

        // 차단된 세션 처리
        if (chatSessionService.isBanned(sessionId) || chatSessionService.isBanned(headerAccessor.getSessionId())) {
            ChatMessage banNotice = new ChatMessage();
            banNotice.setSender("시스템");
            banNotice.setContent("관리자에 의해 일시적으로 차단되었습니다.");
            banNotice.setTimestamp(Instant.now().toString());
            banNotice.setRoomId(message.getRoomId());
            banNotice.setSessionId(sessionId);

            messagingTemplate.convertAndSend("/topic/room/" + message.getRoomId(), banNotice);
            return;
        }

        String nickname = chatSessionService.getOrCreateName(sessionId);

        // 발신자 설정
        if (principal != null) {
            Authentication auth = (Authentication) principal;
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));
            message.setSender(isAdmin ? "관리자" : nickname);
        } else {
            message.setSender(nickname);
        }

        message.setSessionId(sessionId);
        message.setTimestamp(Instant.now().toString());
        chatService.sendMessage(message);
    }

    @GetMapping("/api/chat/{roomId}/messages")
    @ResponseBody
    public List<ChatMessage> getMessages(@PathVariable String roomId) {
        return chatService.getRecentMessages(roomId);
    }
}