package com.yusufbatmaz.chatbot.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.yusufbatmaz.chatbot.model.UserProfile;
import com.yusufbatmaz.chatbot.model.UserProfileDTO;
import com.yusufbatmaz.chatbot.service.UserProfileService;

/**
 * Kullanıcı profil bilgilerini yöneten REST controller.
 * Dil tercihleri, kişilik ayarları ve profil güncellemelerini işler.
 */
@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:5173")
public class UserProfileController {
    
    private final UserProfileService userProfileService;
    
    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }
    
    /**
     * Kullanıcı profil bilgilerini getirir
     * @param userId Kullanıcı ID'si
     * @return Kullanıcı profili
     */
    @GetMapping("/{userId}")
    public ResponseEntity<com.yusufbatmaz.chatbot.model.UserProfileDTO> getUserProfile(@PathVariable UUID userId) {
        var profile = userProfileService.getUserProfileDto(userId);
        return ResponseEntity.ok(profile);
    }
    
    /**
     * Kullanıcı profil bilgilerini günceller
     * @param userId Kullanıcı ID'si
     * @param profile Güncellenecek profil bilgileri
     * @return Güncellenmiş profil
     */
    @PutMapping("/{userId}")
    public ResponseEntity<UserProfile> updateUserProfile(
            @PathVariable UUID userId, 
            @RequestBody UserProfileDTO profileDto) {
        UserProfile updatedProfile = userProfileService.updateUserProfile(userId, profileDto);
        return ResponseEntity.ok(updatedProfile);
    }
    
    /**
     * Kullanıcının tercih ettiği dili günceller
     * @param userId Kullanıcı ID'si
     * @param language Yeni dil kodu
     * @return Güncellenmiş profil
     */
    @PutMapping("/{userId}/language")
    public ResponseEntity<UserProfile> updatePreferredLanguage(
            @PathVariable UUID userId, 
            @RequestParam String language) {
        UserProfile updatedProfile = userProfileService.updatePreferredLanguage(userId, language);
        return ResponseEntity.ok(updatedProfile);
    }
    
    /**
     * Kullanıcının ChatBot yanıt dilini zorla değiştirir
     * @param userId Kullanıcı ID'si
     * @param language Zorlanacak dil
     * @param force Zorlama aktif mi
     * @return Başarı mesajı
     */
    @PutMapping("/{userId}/force-language")
    public ResponseEntity<String> setForcedResponseLanguage(
            @PathVariable UUID userId, 
            @RequestParam String language, 
            @RequestParam boolean force) {
        userProfileService.setForcedResponseLanguage(userId, language, force);
        return ResponseEntity.ok("Language preference updated successfully");
    }
    
    /**
     * Kullanıcının ChatBot yanıt dilini döner
     * @param userId Kullanıcı ID'si
     * @return Yanıt dili
     */
    @GetMapping("/{userId}/response-language")
    public ResponseEntity<String> getResponseLanguage(@PathVariable UUID userId) {
        String responseLanguage = userProfileService.getResponseLanguage(userId);
        return ResponseEntity.ok(responseLanguage);
    }
    
    /**
     * Kullanıcının ChatBot kişilik ayarlarını döner
     * @param userId Kullanıcı ID'si
     * @return Kişilik ayarları
     */
    @GetMapping("/{userId}/personality")
    public ResponseEntity<String> getPersonality(@PathVariable UUID userId) {
        String personality = userProfileService.getPersonality(userId);
        return ResponseEntity.ok(personality);
    }
    
    /**
     * Kullanıcının ChatBot özelliklerini döner
     * @param userId Kullanıcı ID'si
     * @return ChatBot özellikleri
     */
    @GetMapping("/{userId}/traits")
    public ResponseEntity<java.util.Set<String>> getTraits(@PathVariable UUID userId) {
        java.util.Set<String> traits = userProfileService.getTraits(userId);
        return ResponseEntity.ok(traits);
    }
}
