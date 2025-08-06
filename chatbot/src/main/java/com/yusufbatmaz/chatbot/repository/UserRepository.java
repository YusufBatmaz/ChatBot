package com.yusufbatmaz.chatbot.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yusufbatmaz.chatbot.model.User;

/**
 * Kullanıcı veritabanı işlemlerini gerçekleştiren repository arayüzü.
 * JpaRepository sayesinde temel CRUD işlemleri otomatik olarak sağlanır.
 */
public interface UserRepository extends JpaRepository<User, UUID> {
   
   
    /** Verilen email adresine sahip bir kullanıcı var mı kontrol eder. 
    email Kullanıcının email adresi alır ve true/false döner.**/
    boolean existsByEmail(String email);

    /**Email adresine göre kullanıcıyı bulur. email Kullanıcının email adresi alır 
    ve Optional<User> döner.*/
    Optional<User> findByEmail(String email);
}
