package com.yusufbatmaz.chatbot.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Şifre hashleme ve doğrulama işlemlerini yöneten service.
 * BCrypt algoritması kullanarak güvenli şifre hashleme sağlar.
 */
@Service
public class PasswordService {
    
    // BCrypt encoder - şifreleri hashlemek için kullanılır
    private final BCryptPasswordEncoder passwordEncoder;
    
    public PasswordService() {
        // BCrypt encoder'ı oluşturuyoruz
        // strength: 10 (güvenlik seviyesi, 10-12 arası önerilen)
        this.passwordEncoder = new BCryptPasswordEncoder(10);
    }
    
    /**
     * Plain text şifreyi BCrypt ile hashler.
     * @param plainPassword Hashlenecek şifre
     * @return Hashed şifre
     */
    public String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Şifre boş olamaz");
        }
        return passwordEncoder.encode(plainPassword);
    }
    
    /**
     * Plain text şifreyi hashed şifre ile karşılaştırır.
     * @param plainPassword Kullanıcının girdiği şifre
     * @param hashedPassword Database'deki hashed şifre
     * @return Eşleşirse true, değilse false
     */
    public boolean matches(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }
    
    /**
     * Bir string'in BCrypt hash'i olup olmadığını kontrol eder.
     * @param hash Kontrol edilecek string
     * @return BCrypt hash'i ise true, değilse false
     */
    public boolean isBCryptHash(String hash) {
        if (hash == null || hash.trim().isEmpty()) {
            return false;
        }
        // BCrypt hash'leri $2a$ ile başlar
        return hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$");
    }
} 