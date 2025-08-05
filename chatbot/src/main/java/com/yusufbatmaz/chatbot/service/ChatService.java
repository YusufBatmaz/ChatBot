package com.yusufbatmaz.chatbot.service;

import com.yusufbatmaz.chatbot.model.ChatMessage;
import com.yusufbatmaz.chatbot.model.ChatHistory;
import com.yusufbatmaz.chatbot.model.User;
import com.yusufbatmaz.chatbot.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import java.time.Duration;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatHistoryRepository chatHistoryRepository;

    private static final String OPENAI_API_KEY = "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";

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://openrouter.ai/api/v1")
            .defaultHeader("Authorization", "Bearer " + OPENAI_API_KEY)
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(new ReactorClientHttpConnector(
                    HttpClient.create()
                            .responseTimeout(Duration.ofSeconds(30))))
            .build();
    
    @Transactional
    public String ask(ChatMessage chatMessage, User user) {
        if (chatMessage == null || chatMessage.getMessage() == null || chatMessage.getMessage().isBlank()) {
            return "Lütfen bir mesaj girin.";
        }

        String userMessage = chatMessage.getMessage();

        Map<String, Object> requestBody = Map.of(
                "model", "deepseek/deepseek-chat-v3-0324:free",
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", userMessage)));

        Map<String, Object> response;
        try {
            response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
        } catch (Exception e) {
            System.err.println("API çağrısı sırasında hata oluştu: " + e.getMessage());
            return "API çağrısı sırasında hata oluştu: " + e.getMessage();
        }

        System.out.println("OpenRouter cevabı:");
        System.out.println(response);

        if (response == null || !response.containsKey("choices")) {
            return "Yapay zekadan yanıt alınamadı.";
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices.isEmpty()) {
            return "Yapay zekadan boş yanıt döndü.";
        }

        Map<String, Object> firstChoice = choices.get(0);
        @SuppressWarnings("unchecked")
        Map<String, Object> messageResponse = (Map<String, Object>) firstChoice.get("message");
        if (messageResponse == null || !messageResponse.containsKey("content")) {
            return "Yapay zekadan geçersiz yanıt döndü.";
        }

        String botResponse = (String) messageResponse.get("content");

        // Soru kategorisini belirle
        String questionCategory = determineQuestionCategory(userMessage);

        ChatHistory history = new ChatHistory();
        history.setUserMessage(userMessage);
        history.setBotResponse(botResponse);
        history.setTimestamp(LocalDateTime.now());
        history.setUser(user);
        history.setQuestionCategory(questionCategory);

        chatHistoryRepository.save(history);
        System.out.println("Mesaj ve cevap veritabanına kaydedildi. Kategori: " + questionCategory);

        return botResponse;
    }

    private String determineQuestionCategory(String userMessage) {
        String message = userMessage.toLowerCase();
        
        if (message.contains("merhaba") || message.contains("selam") || message.contains("hi") || message.contains("hello")) {
            return "SELAMLAMA";
        } else if (message.contains("nasılsın") || message.contains("how are you")) {
            return "SELAMLAMA";
        } else if (message.contains("teşekkür") || message.contains("thank")) {
            return "TEŞEKKÜR";
        } else if (message.contains("ne yapıyorsun") || message.contains("what do you do")) {
            return "BİLGİ";
        } else if (message.contains("kimsin") || message.contains("who are you")) {
            return "BİLGİ";
        } else if (message.contains("hava") || message.contains("weather")) {
            return "HAVA_DURUMU";
        } else if (message.contains("saat") || message.contains("time")) {
            return "ZAMAN";
        } else if (message.contains("tarih") || message.contains("date")) {
            return "ZAMAN";
        } else if (message.contains("yardım") || message.contains("help")) {
            return "YARDIM";
        } else if (message.contains("nasıl") || message.contains("how")) {
            return "NASIL";
        } else if (message.contains("neden") || message.contains("why")) {
            return "NEDEN";
        } else if (message.contains("ne") || message.contains("what")) {
            return "SORU";
        } else {
            return "GENEL";
        }
    }
}
