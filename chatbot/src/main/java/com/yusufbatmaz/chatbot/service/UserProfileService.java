package com.yusufbatmaz.chatbot.service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yusufbatmaz.chatbot.exception.NotFoundException;
import com.yusufbatmaz.chatbot.model.User;
import com.yusufbatmaz.chatbot.model.UserProfile;
import com.yusufbatmaz.chatbot.model.UserProfileDTO;
import com.yusufbatmaz.chatbot.repository.UserProfileRepository;
import com.yusufbatmaz.chatbot.repository.UserRepository;

/**
 * Kullanıcı profil bilgilerini yöneten servis sınıfı.
 * Dil tespiti, profil güncelleme ve kişiselleştirme işlemlerini gerçekleştirir.
 */
@Service
public class UserProfileService {
    
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    
    public UserProfileService(UserProfileRepository userProfileRepository, UserRepository userRepository) {
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Kullanıcı ID'sine göre profil bilgilerini getirir
     * @param userId Kullanıcı ID'si
     * @return Kullanıcı profili
     */
    public UserProfile getUserProfile(UUID userId) {
        return userProfileRepository.findByUser_Id(userId)
                .orElseGet(() -> createDefaultProfile(userId));
    }
    
    /**
     * Kullanıcı için varsayılan profil oluşturur
     * @param userId Kullanıcı ID'si
     * @return Oluşturulan profil
     */
    private UserProfile createDefaultProfile(UUID userId) {
        // Ensure the user exists for association (but do not persist profile here)
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found for profile creation"));

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setPreferredLanguage("en");
        profile.setPersonality("default");
        profile.setTraits(new HashSet<>());
        profile.setEnableForNewChats(true);
        return profile;
    }
    
    /**
     * Kullanıcı profil bilgilerini günceller
     * @param userId Kullanıcı ID'si
     * @param profile Güncellenecek profil bilgileri
     * @return Güncellenmiş profil
     */
    @Transactional
    public UserProfile updateUserProfile(UUID userId, UserProfileDTO profile) {
        // Always attach the managed existing user entity
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found for profile update"));

        // Ensure a persistent profile entity exists for this user
        UserProfile existingProfile = userProfileRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    UserProfile created = new UserProfile();
                    created.setUser(user);
                    created.setPreferredLanguage("en");
                    created.setPersonality("default");
                    created.setTraits(new HashSet<>());
                    created.setEnableForNewChats(true);
                    return userProfileRepository.save(created);
                });

        // Always ensure the profile points to the managed user (avoid accidental new user creation)
        existingProfile.setUser(user);
        
        // Profil bilgilerini güncelle
        if (profile.getNickname() != null) {
            existingProfile.setNickname(profile.getNickname());
        }
        if (profile.getOccupation() != null) {
            existingProfile.setOccupation(profile.getOccupation());
        }
        if (profile.getPersonality() != null) {
            existingProfile.setPersonality(profile.getPersonality());
        }
        if (profile.getTraits() != null) {
            existingProfile.setTraits(profile.getTraits());
        }
        if (profile.getAdditionalInfo() != null) {
            existingProfile.setAdditionalInfo(profile.getAdditionalInfo());
        }
        if (profile.getPreferredLanguage() != null) {
            existingProfile.setPreferredLanguage(profile.getPreferredLanguage());
        }
        if (profile.getEnableForNewChats() != null) {
            existingProfile.setEnableForNewChats(profile.getEnableForNewChats());
        }
        
        return userProfileRepository.save(existingProfile);
    }
    
    /**
     * Kullanıcının tercih ettiği dili günceller
     * @param userId Kullanıcı ID'si
     * @param language Yeni dil kodu
     * @return Güncellenmiş profil
     */
    @Transactional
    public UserProfile updatePreferredLanguage(UUID userId, String language) {
        UserProfile profile = getUserProfile(userId);
        profile.setPreferredLanguage(language);
        return userProfileRepository.save(profile);
    }
    
    /**
     * Kullanıcının ana dilini otomatik tespit eder ve kaydeder
     * @param userId Kullanıcı ID'si
     * @param detectedLanguage Tespit edilen dil
     */
    @Transactional
    public void updateNativeLanguage(UUID userId, String detectedLanguage) {
        UserProfile profile = getUserProfile(userId);
        profile.setNativeLanguage(detectedLanguage);
        userProfileRepository.save(profile);
    }
    
    /**
     * Kullanıcının ChatBot yanıt dilini zorla değiştirir
     * @param userId Kullanıcı ID'si
     * @param language Zorlanacak dil
     * @param force Zorlama aktif mi
     */
    @Transactional
    public void setForcedResponseLanguage(UUID userId, String language, boolean force) {
        UserProfile profile = getUserProfile(userId);
        profile.setForceResponseLanguage(force);
        if (force) {
            profile.setForcedResponseLanguage(language);
        } else {
            profile.setForcedResponseLanguage(null);
        }
        userProfileRepository.save(profile);
    }
    
    /**
     * Kullanıcının ChatBot yanıt dilini döner
     * @param userId Kullanıcı ID'si
     * @return Yanıt dili
     */
    public String getResponseLanguage(UUID userId) {
        UserProfile profile = getUserProfile(userId);
        return profile.getResponseLanguage();
    }
    
    /**
     * Kullanıcının ChatBot kişilik ayarlarını döner
     * @param userId Kullanıcı ID'si
     * @return Kişilik ayarları
     */
    public String getPersonality(UUID userId) {
        UserProfile profile = getUserProfile(userId);
        return profile.getPersonality();
    }
    
    /**
     * Kullanıcının ChatBot özelliklerini döner
     * @param userId Kullanıcı ID'si
     * @return ChatBot özellikleri
     */
    public Set<String> getTraits(UUID userId) {
        UserProfile profile = getUserProfile(userId);
        return profile.getTraits() != null ? profile.getTraits() : new HashSet<>();
    }
    
    /**
     * Kullanıcının profil bilgilerini ChatBot için formatlar
     * @param userId Kullanıcı ID'si
     * @return Formatlanmış profil bilgileri
     */
    public String getFormattedProfileForBot(UUID userId) {
        UserProfile profile = getUserProfile(userId);
        StringBuilder sb = new StringBuilder();
        
        if (profile.getNickname() != null) {
            sb.append("Call me: ").append(profile.getNickname()).append("\n");
        }
        
        if (profile.getOccupation() != null) {
            sb.append("I am: ").append(profile.getOccupation()).append("\n");
        }
        
        if (profile.getAdditionalInfo() != null) {
            sb.append("Additional info: ").append(profile.getAdditionalInfo()).append("\n");
        }
        
        if (profile.getTraits() != null && !profile.getTraits().isEmpty()) {
            sb.append("My preferred traits: ").append(String.join(", ", profile.getTraits())).append("\n");
        }
        
        return sb.toString();
    }

    /**
     * UserProfile entity'sini dış dünyaya açmak için DTO'ya dönüştürür
     */
    public UserProfileDTO toDto(UserProfile profile) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setNickname(profile.getNickname());
        dto.setOccupation(profile.getOccupation());
        dto.setPersonality(profile.getPersonality());
        dto.setTraits(profile.getTraits());
        dto.setAdditionalInfo(profile.getAdditionalInfo());
        dto.setPreferredLanguage(profile.getPreferredLanguage());
        dto.setEnableForNewChats(profile.isEnableForNewChats());
        return dto;
    }

    public UserProfileDTO getUserProfileDto(UUID userId) {
        return toDto(getUserProfile(userId));
    }
}
