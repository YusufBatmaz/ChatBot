package com.yusufbatmaz.chatbot.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yusufbatmaz.chatbot.model.UserProfile;

/**
 * UserProfile entity'si için JPA repository interface'i.
 * Kullanıcı profil bilgilerinin veritabanı işlemlerini yönetir.
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    
    /**
     * Kullanıcı ID'sine göre profil bilgilerini bulur
     * @param userId Kullanıcı ID'si
     * @return Kullanıcı profili (varsa)
     */
    Optional<UserProfile> findByUser_Id(UUID userId);
    
    /**
     * Kullanıcı ID'sine göre profil bilgilerinin var olup olmadığını kontrol eder
     * @param userId Kullanıcı ID'si
     * @return Profil varsa true, yoksa false
     */
    boolean existsByUser_Id(UUID userId);
}
