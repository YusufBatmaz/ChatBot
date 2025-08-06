package com.yusufbatmaz.chatbot.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.reactive.function.client.WebClient;

import com.yusufbatmaz.chatbot.exception.DatabaseException;
import com.yusufbatmaz.chatbot.exception.ValidationException;
import com.yusufbatmaz.chatbot.model.ChatHistory;
import com.yusufbatmaz.chatbot.model.ChatMessage;
import com.yusufbatmaz.chatbot.model.User;
import com.yusufbatmaz.chatbot.repository.ChatHistoryRepository;

import lombok.RequiredArgsConstructor;
import reactor.netty.http.client.HttpClient;

/**
 * Chatbot ile ilgili iş mantığı ve servis işlemlerini barındırır.
 * OpenRouter API ile iletişim kurar, cevapları alır ve veritabanına kaydeder.
 */
@Service
@RequiredArgsConstructor
public class ChatService {

    // Logging için SLF4J logger kullanıyoruz
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private final ChatHistoryRepository chatHistoryRepository;

    // OpenRouter API anahtarı (güvenlik için kodda açık tutmak önerilmez)
    private static final String OPENAI_API_KEY = "Kendi Api Keyinizi Giriniz / You can enter your own Api Key";

    // OpenRouter API'ye istek atmak için WebClient nesnesi
    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://openrouter.ai/api/v1")
            .defaultHeader("Authorization", "Bearer " + OPENAI_API_KEY)
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(new ReactorClientHttpConnector(
                    HttpClient.create()
                            .responseTimeout(Duration.ofSeconds(30))))
            .build();

    /**
     * Kullanıcıdan gelen mesajı OpenRouter API'ye gönderir, cevabı alır,
     * kategorisini belirler ve veritabanına kaydeder.
     * @param chatMessage Kullanıcının mesajı
     * @param user Mesajı gönderen kullanıcı
     * @return Yapay zekadan gelen cevap
     */
    @Transactional
    public String ask(ChatMessage chatMessage, User user) {
        try {
            // Input validasyonu - gelen parametrelerin geçerliliğini kontrol ediyoruz
            validateChatRequest(chatMessage, user);
            
            String userMessage = chatMessage.getMessage();
            logger.info("Chat isteği alındı - Kullanıcı: {}, Mesaj: {}", user.getEmail(), userMessage);

            // OpenRouter API'ye gönderilecek istek gövdesi
            Map<String, Object> requestBody = Map.of(
                    "model", "deepseek/deepseek-chat-v3-0324:free",
                    "messages", List.of(
                            Map.of(
                                    "role", "user",
                                    "content", userMessage)));

            Map<String, Object> response;
            try {
                // API'ye POST isteği gönder ve cevabı al
                response = webClient.post()
                        .uri("/chat/completions")
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                        .block();
                        
            } catch (ResourceAccessException e) {
                // Dış API'ye erişim hatası (network, timeout, vb.)
                logger.error("OpenRouter API'ye erişim hatası", e);
                throw new ResourceAccessException("AI servisi şu anda kullanılamıyor. Lütfen daha sonra tekrar deneyin.");
                
            } catch (Exception e) {
                // Diğer API çağrısı hataları
                logger.error("API çağrısı sırasında hata oluştu", e);
                return "API çağrısı sırasında hata oluştu. Lütfen daha sonra tekrar deneyin.";
            }

            logger.debug("OpenRouter cevabı: {}", response);

            // API'den geçerli bir cevap alındı mı kontrolü
            if (response == null || !response.containsKey("choices")) {
                logger.warn("OpenRouter'dan geçersiz yanıt alındı");
                return "Yapay zekadan yanıt alınamadı. Lütfen daha sonra tekrar deneyin.";
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices.isEmpty()) {
                logger.warn("OpenRouter'dan boş yanıt alındı");
                return "Yapay zekadan boş yanıt döndü. Lütfen daha sonra tekrar deneyin.";
            }

            Map<String, Object> firstChoice = choices.get(0);
            @SuppressWarnings("unchecked")
            Map<String, Object> messageResponse = (Map<String, Object>) firstChoice.get("message");
            if (messageResponse == null || !messageResponse.containsKey("content")) {
                logger.warn("OpenRouter'dan geçersiz mesaj yanıtı alındı");
                return "Yapay zekadan geçersiz yanıt döndü. Lütfen daha sonra tekrar deneyin.";
            }

            String botResponse = (String) messageResponse.get("content");

            // Soru kategorisini belirle
            String questionCategory = determineQuestionCategory(userMessage);

            // Mesaj geçmişini veritabanına kaydet
            saveChatHistory(userMessage, botResponse, user, questionCategory);

            logger.info("Chat işlemi başarıyla tamamlandı - Kullanıcı: {}, Kategori: {}", user.getEmail(), questionCategory);
            return botResponse;

        } catch (ValidationException e) {
            // Validation hatası - kullanıcıdan gelen veriler geçersiz
            logger.warn("Chat isteği validasyon hatası: {}", e.getMessage());
            throw e;
            
        } catch (Exception e) {
            // Diğer tüm beklenmeyen hatalar
            logger.error("Chat işlemi sırasında beklenmeyen hata", e);
            throw new RuntimeException("Chat işlemi sırasında hata oluştu", e);
        }
    }

    /**
     * Chat isteği validasyonu yapar.
     * Gelen parametrelerin null/boş olup olmadığını kontrol eder.
     */
    private void validateChatRequest(ChatMessage chatMessage, User user) {
        // Chat mesajı null kontrolü
        if (chatMessage == null) {
            throw new ValidationException("Chat mesajı boş olamaz");
        }
        
        // Mesaj içeriği null/boş kontrolü
        if (chatMessage.getMessage() == null || chatMessage.getMessage().trim().isEmpty()) {
            throw new ValidationException("Mesaj içeriği boş olamaz");
        }
        
        // Kullanıcı null kontrolü
        if (user == null) {
            throw new ValidationException("Kullanıcı bilgisi gerekli");
        }
        
        // Kullanıcı ID null kontrolü
        if (user.getId() == null) {
            throw new ValidationException("Geçerli kullanıcı ID'si gerekli");
        }
    }

    /**
     * Chat geçmişini veritabanına kaydeder.
     * Hata durumunda DatabaseException fırlatır.
     */
    private void saveChatHistory(String userMessage, String botResponse, User user, String questionCategory) {
        try {
            // ChatHistory objesi oluşturuyoruz
            ChatHistory history = new ChatHistory();
            history.setUserMessage(userMessage);
            history.setBotResponse(botResponse);
            history.setTimestamp(LocalDateTime.now());
            history.setUser(user);
            history.setQuestionCategory(questionCategory);

            // Veritabanına kaydediyoruz
            chatHistoryRepository.save(history);
            logger.debug("Chat geçmişi kaydedildi - Kullanıcı: {}, Kategori: {}", user.getEmail(), questionCategory);
            
        } catch (Exception e) {
            // Veritabanı kaydetme hatası
            logger.error("Chat geçmişi kaydedilirken hata oluştu", e);
            throw new DatabaseException("Chat geçmişi kaydedilirken hata oluştu", e);
        }
    }

    /**
     * Anahtar kelime ve kategori eşleşmelerini tutan map.
     * Her anahtar, virgülle ayrılmış anahtar kelimeleri ve karşılık gelen kategoriyi içerir.
     */
    private static final Map<String, String> CATEGORY_KEYWORDS = new LinkedHashMap<>();
    static {
        CATEGORY_KEYWORDS.put("merhaba,selam,hi,hello", "SELAMLAMA");
        CATEGORY_KEYWORDS.put("nasılsın,how are you", "SELAMLAMA");
        CATEGORY_KEYWORDS.put("teşekkür,thank", "TEŞEKKÜR");
        CATEGORY_KEYWORDS.put("ne yapıyorsun,what do you do", "BİLGİ");
        CATEGORY_KEYWORDS.put("kimsin,who are you", "BİLGİ");
        CATEGORY_KEYWORDS.put("hava,weather", "HAVA_DURUMU");
        CATEGORY_KEYWORDS.put("saat,time", "ZAMAN");
        CATEGORY_KEYWORDS.put("tarih,date", "ZAMAN");
        CATEGORY_KEYWORDS.put("yardım,help", "YARDIM");
        CATEGORY_KEYWORDS.put("nasıl,how", "NASIL");
        CATEGORY_KEYWORDS.put("neden,why", "NEDEN");
        CATEGORY_KEYWORDS.put("ne,what", "SORU");
    }

    /**
     * Kullanıcının mesajına göre soru kategorisini belirler.
     * Anahtar kelimeleri kontrol ederek uygun kategoriyi döner.
     * @param userMessage Kullanıcının mesajı
     * @return Belirlenen kategori
     */
    private String determineQuestionCategory(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();
        
        // Her kategori için anahtar kelimeleri kontrol ediyoruz
        for (Map.Entry<String, String> entry : CATEGORY_KEYWORDS.entrySet()) {
            String[] keywords = entry.getKey().split(",");
            for (String keyword : keywords) {
                if (lowerMessage.contains(keyword.trim())) {
                    return entry.getValue();
                }
            }
        }
        
        // Hiçbir kategoriye uymuyorsa "GENEL" döndürüyoruz
        return "GENEL";
    }
}
