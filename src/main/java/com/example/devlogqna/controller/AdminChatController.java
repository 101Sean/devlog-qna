package com.example.devlogqna.controller;

import com.example.devlogqna.service.ChatService;
import com.example.devlogqna.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/chat")
@RequiredArgsConstructor
public class AdminChatController {

    private final ChatService chatService;
    private final ChatSessionService chatSessionService;

    @PostMapping("/ban/{sessionId}")
    public ResponseEntity<?> banUser(@PathVariable String sessionId) {
        chatSessionService.banSession(sessionId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<?> deleteMessage(@PathVariable String messageId) {
        boolean deleted = chatService.deleteMessage("main", messageId);
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
