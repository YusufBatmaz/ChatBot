package com.yusufbatmaz.chatbot.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.reactive.function.client.WebClient;

import com.yusufbatmaz.chatbot.config.ApiConfig;
import com.yusufbatmaz.chatbot.exception.DatabaseException;
import com.yusufbatmaz.chatbot.exception.ValidationException;
import com.yusufbatmaz.chatbot.model.ChatHistory;
import com.yusufbatmaz.chatbot.model.ChatMessage;
import com.yusufbatmaz.chatbot.model.User;
import com.yusufbatmaz.chatbot.repository.ChatHistoryRepository;

import reactor.netty.http.client.HttpClient;

/**
 * Chatbot ile ilgili iş mantığı ve servis işlemlerini barındırır.
 * OpenRouter API ile iletişim kurar, cevapları alır ve veritabanına kaydeder.
 */
@Service
public class ChatService {

    // Logging için SLF4J logger kullanıyoruz
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private final ChatHistoryRepository chatHistoryRepository;
    private final LanguageDetectionService languageDetectionService;
    private final UserProfileService userProfileService;

    // OpenRouter API'ye istek atmak için WebClient nesnesi
    private final WebClient webClient;

    /**
     * Constructor - WebClient'ı configuration ile oluşturuyoruz
     */
    public ChatService(ChatHistoryRepository chatHistoryRepository, 
                      LanguageDetectionService languageDetectionService,
                      UserProfileService userProfileService,
                      ApiConfig apiConfig) {
        this.chatHistoryRepository = chatHistoryRepository;
        this.languageDetectionService = languageDetectionService;
        this.userProfileService = userProfileService;
        
        // WebClient'ı configuration'dan gelen değerlerle oluşturuyoruz
        this.webClient = WebClient.builder()
                .baseUrl(apiConfig.getUrl())
                .defaultHeader("Authorization", "Bearer " + apiConfig.getKey())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .responseTimeout(Duration.ofSeconds(apiConfig.getTimeout()))))
                .build();
    }

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

            // Kullanıcı profil bilgilerini al
            String userPreferredLanguage = userProfileService.getResponseLanguage(user.getId());
            String detectedLanguage = languageDetectionService.detectLanguage(userMessage);
            String responseLanguage = languageDetectionService.determineResponseLanguage(
                userMessage, userPreferredLanguage, null);

            // Dil seçimleri için debug logları
            logger.debug("Language - preferred: {}, detected: {}, response: {}",
                    userPreferredLanguage, detectedLanguage, responseLanguage);
            
            // Kullanıcının ana dilini güncelle (ilk kez tespit ediliyorsa)
            if (userProfileService.getUserProfile(user.getId()).getNativeLanguage() == null) {
                userProfileService.updateNativeLanguage(user.getId(), detectedLanguage);
            }
            
            // ChatBot için sistem mesajı oluştur
            String systemMessage = createSystemMessage(user.getId(), responseLanguage);
            
            // OpenRouter API'ye gönderilecek istek gövdesi
            String directiveUser = buildDirectiveUserMessage(responseLanguage);
            Map<String, Object> requestBody = Map.of(
                    "model", "deepseek/deepseek-chat-v3-0324:free",
                    "messages", List.of(
                            Map.of("role", "system", "content", systemMessage),
                            // Dil politikası için ek kullanıcı talimatı (uyumluluğu artırır)
                            Map.of("role", "user", "content", directiveUser),
                            Map.of("role", "user", "content", userMessage)));

            logger.info("OpenRouter API'ye istek gönderiliyor...");
            logger.debug("Request body: {}", requestBody);
            
            Map<String, Object> response;
            try {
                // API'ye POST isteği gönder ve cevabı al
                response = webClient.post()
                        .uri("/chat/completions")
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                        .block();
                        
                logger.info("OpenRouter API'den yanıt alındı");
                        
            } catch (ResourceAccessException e) {
                // Dış API'ye erişim hatası (network, timeout, vb.)
                logger.error("OpenRouter API'ye erişim hatası: {}", e.getMessage(), e);
                throw new ResourceAccessException("AI servisi şu anda kullanılamıyor. Lütfen daha sonra tekrar deneyin.");
                
            } catch (Exception e) {
                // Diğer API çağrısı hataları
                logger.error("API çağrısı sırasında hata oluştu: {}", e.getMessage(), e);
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

            // Bot response'unu 2000 karakterle sınırla
            if (botResponse.length() > 2000) {
                botResponse = botResponse.substring(0, 1997) + "...";
                logger.info("Bot response 2000 karakterle sınırlandı");
            }

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
        
        // Mesaj uzunluk kontrolü (API limitleri için)
        if (chatMessage.getMessage().length() > 4000) {
            throw new ValidationException("Mesaj çok uzun. Lütfen daha kısa bir mesaj yazın.");
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
     * ChatBot için sistem mesajı oluşturur
     * @param userId Kullanıcı ID'si
     * @param responseLanguage Yanıt dili
     * @return Sistem mesajı
     */
    private String createSystemMessage(UUID userId, String responseLanguage) {
        StringBuilder systemMessage = new StringBuilder();
        
        // Dil talimatı
        systemMessage.append("You are a helpful AI assistant. ");
        systemMessage.append("Always respond in ").append(languageDetectionService.getLanguageName(responseLanguage)).append(". ");
        
        // Kullanıcı profil bilgileri
        String userProfile = userProfileService.getFormattedProfileForBot(userId);
        if (!userProfile.isEmpty()) {
            systemMessage.append("\n\nUser profile:\n").append(userProfile);
        }
        
        // Kişilik ve özellikler
        String personality = userProfileService.getPersonality(userId);
        if (!"default".equals(personality)) {
            systemMessage.append("\n\nPersonality: ").append(personality);
        }
        
        Set<String> traits = userProfileService.getTraits(userId);
        if (traits != null && !traits.isEmpty()) {
            systemMessage.append("\n\nTraits: ").append(String.join(", ", traits));
        }
        
        systemMessage.append("\n\nCRITICAL LANGUAGE POLICY:\n")
                     .append("- Default and preferred response language: ")
                     .append(languageDetectionService.getLanguageName(responseLanguage)).append(".\n")
                     .append("- Always reply ONLY in this language.\n")
                     .append("- If the user writes in another language, translate their intent and respond in the preferred language. Do NOT switch languages.\n")
                     .append("- Exception: if the user EXPLICITLY requests a different language in the prompt (e.g., 'respond in German'), follow that for that message only, then revert to the preferred language.")
                     .append("\n\n").append(getLocalizedDirective(responseLanguage));
        
        return systemMessage.toString();
    }

    /**
     * Seçilen dilde katı yönergeyi döndürür
     */
    private String getLocalizedDirective(String code) {
        String c = languageDetectionService.normalizeLanguageCode(code);
        switch (c) {
            case "tr":
                return "Kritik Dil Politikası: Sadece Türkçe yanıt ver. Kullanıcı başka bir dilde yazsa bile Türkçe cevapla. Sadece kullanıcı açıkça başka bir dil isterse o mesaj için o dilde yanıt ver, ardından Türkçeye dön.";
            case "de":
                return "Kritische Sprachrichtlinie: Antworte ausschließlich auf Deutsch. Auch wenn der Nutzer in einer anderen Sprache schreibt, antworte auf Deutsch. Nur wenn ausdrücklich eine andere Sprache verlangt wird, antworte für diese Nachricht so und kehre dann zu Deutsch zurück.";
            case "en":
            default:
                return "Critical Language Policy: Respond only in English. If the user writes in another language, answer in English. Only if they explicitly ask for a different language, comply for that message then revert to English.";
        }
    }

    /**
     * Kullanıcı rolünde kısa bir direktif mesajı üretir (hem İngilizce hem hedef dilde)
     */
    private String buildDirectiveUserMessage(String code) {
        String c = languageDetectionService.normalizeLanguageCode(code);
        switch (c) {
            case "tr":
                return "IMPORTANT: Only respond in Turkish. Sadece Türkçe yanıt ver.";
            case "de":
                return "IMPORTANT: Only respond in German. Antworte ausschließlich auf Deutsch.";
            case "en":
            default:
                return "IMPORTANT: Only respond in English.";
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
