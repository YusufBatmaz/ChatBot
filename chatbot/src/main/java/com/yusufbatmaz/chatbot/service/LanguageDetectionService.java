package com.yusufbatmaz.chatbot.service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

/**
 * Kullanıcı mesajlarının dilini tespit eden servis sınıfı.
 * Basit anahtar kelime analizi ile Türkçe, Almanca ve İngilizce dillerini tespit eder.
 */
@Service
public class LanguageDetectionService {
    
    // Dil tespiti için anahtar kelimeler
    private static final Map<String, Pattern> LANGUAGE_PATTERNS = new HashMap<>();
    
    static {
        // Türkçe karakterler ve kelimeler
        LANGUAGE_PATTERNS.put("tr", Pattern.compile(
            "\\b(merhaba|selam|nasılsın|teşekkür|evet|hayır|tamam|güzel|iyi|kötü|" +
            "ne|nasıl|neden|kim|nerede|ne zaman|hangi|kaç|büyük|küçük|" +
            "ben|sen|o|biz|siz|onlar|bu|şu|o|şey|her|hiç|bazı|" +
            "ve|veya|ama|çünkü|eğer|ise|için|ile|gibi|kadar|" +
            "çok|az|daha|en|hem|ya|da|de|den|dan|in|ın|un|ün)\\b|" +
            "[çğıöşüÇĞIÖŞÜ]"
        ));
        
        // Almanca kelimeler
        LANGUAGE_PATTERNS.put("de", Pattern.compile(
            "\\b(hallo|guten|tag|morgen|abend|danke|bitte|ja|nein|okay|gut|schlecht|" +
            "was|wie|warum|wer|wo|wann|welche|wieviel|groß|klein|" +
            "ich|du|er|sie|es|wir|ihr|sie|das|der|die|ein|eine|" +
            "und|oder|aber|weil|wenn|dann|für|mit|wie|bis|" +
            "sehr|wenig|mehr|am|den|der|die|das|ein|eine|" +
            "ist|sind|war|waren|habe|hat|haben|hatte|hatten)\\b"
        ));
        
        // İngilizce kelimeler (varsayılan)
        LANGUAGE_PATTERNS.put("en", Pattern.compile(
            "\\b(hello|hi|how|are|you|thank|thanks|yes|no|okay|good|bad|" +
            "what|how|why|who|where|when|which|how|many|big|small|" +
            "i|you|he|she|it|we|they|this|that|the|a|an|" +
            "and|or|but|because|if|then|for|with|like|until|" +
            "very|little|more|most|am|is|are|was|were|have|has|had)\\b"
        ));
    }
    
    /**
     * Verilen metnin dilini tespit eder
     * @param text Tespit edilecek metin
     * @return Dil kodu (tr, de, en)
     */
    public String detectLanguage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "en"; // Varsayılan dil
        }
        
        String lowerText = text.toLowerCase();
        Map<String, Integer> scores = new HashMap<>();
        
        // Her dil için puan hesapla
        for (Map.Entry<String, Pattern> entry : LANGUAGE_PATTERNS.entrySet()) {
            String language = entry.getKey();
            Pattern pattern = entry.getValue();
            
            int score = 0;
            // Pattern eşleşmelerini say
            java.util.regex.Matcher matcher = pattern.matcher(lowerText);
            while (matcher.find()) {
                score++;
            }
            
            // Türkçe karakterler için ek puan
            if ("tr".equals(language)) {
                score += countTurkishCharacters(lowerText);
            }
            
            scores.put(language, score);
        }
        
        // En yüksek puanlı dili döndür
        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("en");
    }
    
    /**
     * Türkçe karakterleri sayar
     * @param text Metin
     * @return Türkçe karakter sayısı
     */
    private int countTurkishCharacters(String text) {
        int count = 0;
        for (char c : text.toCharArray()) {
            if ("çğıöşüÇĞIÖŞÜ".indexOf(c) != -1) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Kullanıcının dil tercihini belirler
     * @param userMessage Kullanıcı mesajı
     * @param userPreferredLanguage Kullanıcının tercih ettiği dil
     * @param forceLanguage Zorla belirlenen dil (null ise otomatik tespit)
     * @return ChatBot'un yanıt vermesi gereken dil
     */
    public String determineResponseLanguage(String userMessage, String userPreferredLanguage, String forceLanguage) {
        // Zorunlu dil varsa onu kullan
        if (forceLanguage != null && !forceLanguage.trim().isEmpty()) {
            return normalizeLanguageCode(forceLanguage);
        }

        // Kullanıcı prompt'unda açık bir dil isteği var mı tespit et
        String override = extractExplicitOverrideLanguage(userMessage);
        if (override != null) {
            return normalizeLanguageCode(override);
        }

        // Varsayılan: kullanıcı tercih ettiği dil
        return normalizeLanguageCode(userPreferredLanguage);
    }

    /**
     * Kullanıcının prompt'unda açık bir dil talimatını tespit eder.
     * Örnekler: "respond in German", "please answer in English",
     * "Türkçe cevapla", "Lütfen Almanca yanıtla", "auf Deutsch bitte",
     * "language: de|en|tr".
     * Bulunamazsa null döner.
     */
    private String extractExplicitOverrideLanguage(String text) {
        if (text == null) return null;
        String lower = text.toLowerCase();

        // Basit kod notasyonu: language: xx
        java.util.regex.Matcher m = Pattern.compile("language\\s*:\\s*(en|de|tr)").matcher(lower);
        if (m.find()) {
            return m.group(1);
        }

        // İngilizce kalıplar
        if (Pattern.compile("(respond|reply|answer) in (english|german|turkish)").matcher(lower).find()) {
            java.util.regex.Matcher ml = Pattern.compile("(english|german|turkish)").matcher(lower);
            if (ml.find()) return getLanguageCode(ml.group(1));
        }

        // Türkçe kalıplar
        if (Pattern.compile("(türkçe|ingilizce|almanca).*(cevapla|yanıtla)|(?:cevap|yanıt).*?(türkçe|ingilizce|almanca)").matcher(lower).find()) {
            java.util.regex.Matcher mt = Pattern.compile("türkçe|ingilizce|almanca").matcher(lower);
            if (mt.find()) return getLanguageCode(mt.group(0));
        }

        // Almanca kalıplar
        if (Pattern.compile("(auf )?(deutsch|englisch|türkisch)( bitte)?").matcher(lower).find()) {
            java.util.regex.Matcher mg = Pattern.compile("deutsch|englisch|türkisch").matcher(lower);
            if (mg.find()) return getLanguageCode(mg.group(0));
        }

        return null;
    }
    
    /**
     * Dil kodunu tam dil adına çevirir
     * @param languageCode Dil kodu
     * @return Dil adı
     */
    public String getLanguageName(String languageCode) {
        String code = normalizeLanguageCode(languageCode);
        switch (code) {
            case "tr": return "Turkish";
            case "de": return "German";
            case "en": return "English";
            default: return "English";
        }
    }

    /**
     * Dil kodunu normalize eder (örn. tr-TR -> tr, en-US -> en)
     */
    public String normalizeLanguageCode(String languageCode) {
        if (languageCode == null || languageCode.isEmpty()) return "en";
        String lc = languageCode.toLowerCase();
        // Bölge kodlarını kaldır (xx-YY -> xx)
        int dash = lc.indexOf('-');
        if (dash > 0) {
            lc = lc.substring(0, dash);
        }
        switch (lc) {
            case "tr":
            case "tr_tr":
                return "tr";
            case "de":
            case "de_de":
                return "de";
            case "en":
            case "en_us":
            case "en_gb":
                return "en";
            default:
                return getLanguageCode(lc); // düşürülen ad/alias ise code'a çevir
        }
    }
    
    /**
     * Dil adını dil koduna çevirir
     * @param languageName Dil adı
     * @return Dil kodu
     */
    public String getLanguageCode(String languageName) {
        if (languageName == null) return "en";
        
        switch (languageName.toLowerCase()) {
            case "turkish":
            case "türkçe":
            case "turkce":
            case "türkisch":
            case "tr":
                return "tr";
            case "german":
            case "deutsch":
            case "almanca":
            case "de":
                return "de";
            case "english":
            case "ingilizce":
            case "englisch":
            case "en":
                return "en";
            default:
                return "en";
        }
    }

    // Son dil yönergesi kuralı kaldırıldı: Kullanıcı seçimi her zaman önceliklidir.
}
