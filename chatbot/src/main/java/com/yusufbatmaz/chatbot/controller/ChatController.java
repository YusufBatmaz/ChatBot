package com.yusufbatmaz.chatbot.controller;

import com.yusufbatmaz.chatbot.model.ChatMessage;
import com.yusufbatmaz.chatbot.model.User;
import com.yusufbatmaz.chatbot.service.ChatService;
import com.yusufbatmaz.chatbot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://127.0.0.1:3000", "http://127.0.0.1:3001", "http://localhost:5174"})
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<String> chat(@RequestBody ChatMessage chatMessage, @RequestParam(required = false) String userId) {
        System.out.println("API çağrısı alındı: " + chatMessage.getMessage());
        
        User user;
        if (userId != null && !userId.trim().isEmpty()) {
            try {
                UUID userUuid = UUID.fromString(userId);
                user = userService.getUserById(userUuid)
                        .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + userId));
            } catch (IllegalArgumentException e) {
                System.err.println("Geçersiz userId: " + userId);
                throw new RuntimeException("Geçersiz kullanıcı ID'si");
            }
        } else {
            throw new RuntimeException("Kullanıcı ID'si gerekli");
        }

        String botResponse = chatService.ask(chatMessage, user);
        System.out.println("Bot cevabı: " + botResponse);
        return ResponseEntity.ok(botResponse);
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Backend is running!");
    }

    @PostMapping("/test2")
    public ResponseEntity<String> testChat() {
        ChatMessage msg = new ChatMessage();
        msg.setMessage("Merhaba");
        User user = new User();
        user.setId(UUID.randomUUID());
        String response = chatService.ask(msg, user);
        return ResponseEntity.ok(response);
    }
}
