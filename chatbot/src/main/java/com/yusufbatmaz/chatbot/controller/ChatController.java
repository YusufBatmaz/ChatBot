package com.yusufbatmaz.chatbot.controller;

import java.util.UUID;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.yusufbatmaz.chatbot.config.RateLimitConfig;
import com.yusufbatmaz.chatbot.exception.NotFoundException;
import com.yusufbatmaz.chatbot.model.ChatMessage;
import com.yusufbatmaz.chatbot.model.User;
import com.yusufbatmaz.chatbot.service.ChatService;
import com.yusufbatmaz.chatbot.service.UserService;

import lombok.RequiredArgsConstructor;

/**
 * Chatbot ile ilgili HTTP isteklerini karşılayan controller sınıfı.
 * Frontend'den gelen chat isteklerini alır, ChatService ile iletişim kurar ve cevap döner.
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "http://localhost:3000", "http://localhost:3001",
        "http://127.0.0.1:3000", "http://127.0.0.1:3001",
        "http://localhost:5174"
})
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final RateLimitConfig rateLimitConfig;
    private final WebClient webClient;

    /**
     * Kullanıcıdan gelen chat mesajını alır, ilgili kullanıcıyı bulur ve ChatService'e iletir.
     * ChatService'den dönen cevabı frontend'e iletir.
     *
     * @param chatMessage Kullanıcının gönderdiği mesaj
     * @param userId Mesajı gönderen kullanıcının UUID'si (zorunlu)
     * @return Botun cevabı
     */
    @PostMapping
    public ResponseEntity<?> chat(@RequestBody ChatMessage chatMessage, @RequestParam(required = false) String userId) {
        System.out.println("API çağrısı alındı: " + chatMessage.getMessage());

        User user;
        if (userId != null && !userId.trim().isEmpty()) {
            try {
                // String UUID'yi UUID objesine çeviriyoruz
                UUID userUuid = UUID.fromString(userId);
                
                // Kullanıcıyı veritabanından bul
                user = userService.getUserById(userUuid)
                        .orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı: " + userId));
                        
            } catch (IllegalArgumentException e) {
                // UUID format hatası - geçersiz UUID string'i
                System.err.println("Geçersiz userId: " + userId);
                throw new NotFoundException("Geçersiz kullanıcı ID'si");
                
            } catch (NotFoundException e) {
                // Kullanıcı bulunamadı hatası
                throw e;
            }
        } else {
            // userId parametresi eksik
            throw new NotFoundException("Kullanıcı ID'si gerekli");
        }

        // Rate limiting kontrolü
        if (rateLimitConfig.isRateLimitExceeded(userId)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Rate limit aşıldı. Lütfen bir dakika bekleyin.");
        }

        // ChatService ile bot cevabını al (ChatService'de exception handling var)
        String botResponse = chatService.ask(chatMessage, user);
        System.out.println("Bot cevabı: " + botResponse);
        return ResponseEntity.ok(botResponse);
    }

    /**
     * Backend'in çalışıp çalışmadığını test etmek için basit bir endpoint.
     * @return "Backend is running!" mesajı
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Backend is running!");
    }
    
    /**
     * OpenRouter API key'ini test etmek için endpoint.
     * @return API key durumu
     */
    @GetMapping("/test-api")
    public ResponseEntity<String> testApi() {
        try {
            // Basit bir test isteği gönder
            Map<String, Object> testRequest = Map.of(
                "model", "deepseek/deepseek-chat-v3-0324:free",
                "messages", List.of(Map.of("role", "user", "content", "test"))
            );
            
            Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(testRequest)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
                    
            return ResponseEntity.ok("API Key çalışıyor! Response: " + response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("API Key hatası: " + e.getMessage());
        }
    }

    /**
     * Rate limit bilgilerini döndüren endpoint.
     * @param userId Kullanıcı ID'si
     * @return Rate limit bilgileri
     */
    @GetMapping("/rate-limit")
    public ResponseEntity<RateLimitConfig.RateLimitInfo> getRateLimitInfo(@RequestParam String userId) {
        RateLimitConfig.RateLimitInfo info = rateLimitConfig.getRateLimitInfo(userId);
        return ResponseEntity.ok(info);
    }
}
