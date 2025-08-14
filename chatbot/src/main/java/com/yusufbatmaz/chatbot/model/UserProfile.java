package com.yusufbatmaz.chatbot.model;

import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

/**
 * Kullanıcı profil bilgilerini ve tercihlerini temsil eden JPA entity'si.
 * Dil, kişilik, özellikler ve diğer kullanıcı ayarlarını içerir.
 */
@Entity
@Getter
@Setter
public class UserProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    
    // Kullanıcı tercih ettiği dil
    private String preferredLanguage = "en"; // Default: English
    
    // ChatBot'un kullanıcıya hitap etme şekli
    private String nickname;
    
    // Kullanıcının mesleği/öğrenci durumu
    private String occupation;
    
    // ChatBot'un kişiliği
    private String personality = "default";
    
    // ChatBot'un özellikleri (virgülle ayrılmış)
    @ElementCollection
    @CollectionTable(name = "user_profile_traits", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "trait")
    private Set<String> traits;
    
    // Ek bilgiler
    private String additionalInfo;
    
    // Yeni sohbetler için etkin mi
    private boolean enableForNewChats = true;
    
    // Kullanıcının ana dili (otomatik tespit için)
    private String nativeLanguage;
    
    // ChatBot'un yanıt dilini zorla değiştirme
    private boolean forceResponseLanguage = false;
    private String forcedResponseLanguage;
    
    public UserProfile() {}
    
    public UserProfile(User user) {
        this.user = user;
    }
    
    /**
     * Kullanıcının tercih ettiği dili döner
     */
    public String getPreferredLanguage() {
        return preferredLanguage != null ? preferredLanguage : "en";
    }
    
    /**
     * ChatBot'un yanıt vermesi gereken dili döner
     */
    public String getResponseLanguage() {
        if (forceResponseLanguage && forcedResponseLanguage != null) {
            return forcedResponseLanguage;
        }
        return getPreferredLanguage();
    }
    
    /**
     * Belirli bir özelliğin aktif olup olmadığını kontrol eder
     */
    public boolean hasTrait(String trait) {
        return traits != null && traits.contains(trait);
    }
}
