package com.yusufbatmaz.chatbot.service;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.yusufbatmaz.chatbot.exception.DatabaseException;
import com.yusufbatmaz.chatbot.exception.ValidationException;
import com.yusufbatmaz.chatbot.model.User;
import com.yusufbatmaz.chatbot.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Kullanıcı ile ilgili iş mantığı ve servis işlemlerini barındırır.
 * Controller'dan gelen istekleri işler ve repository ile veritabanı arasında köprü olur.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    /**
     * Yeni kullanıcı oluşturur ve veritabanına kaydeder.
     * @param user Kayıt olacak kullanıcı bilgileri
     * @return Kayıt edilen kullanıcı
     */
    public User createUser(User user) {
        try {
            validateUserForRegistration(user);
            user.setId(null); // Yeni kullanıcıda id null olmalı
            User createdUser = userRepository.save(user);
            logger.info("Yeni kullanıcı oluşturuldu: {}", createdUser.getEmail());
            return createdUser;
        } catch (DataIntegrityViolationException e) {
            logger.error("Kullanıcı oluşturulurken veri bütünlüğü hatası", e);
            throw new DatabaseException("Kullanıcı kaydedilirken hata oluştu", e);
        } catch (Exception e) {
            logger.error("Kullanıcı oluşturulurken beklenmeyen hata", e);
            throw new DatabaseException("Kullanıcı oluşturulurken hata oluştu", e);
        }
    }

    /**
     * ID'ye göre kullanıcıyı bulur.
     * @param id Kullanıcı UUID
     * @return Optional<User>
     */
    public Optional<User> getUserById(UUID id) {
        if (id == null) {
            throw new ValidationException("Kullanıcı ID'si boş olamaz");
        }
        return userRepository.findById(id);
    }

    /**
     * Email adresine sahip bir kullanıcı var mı kontrol eder.
     * @param email Kullanıcı email
     * @return true/false
     */
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email adresi boş olamaz");
        }
        return userRepository.existsByEmail(email);
    }

    /**
     * Email adresine göre kullanıcıyı bulur.
     * @param email Kullanıcının email adresi
     * @return Bulunan kullanıcıyı Optional<User> olarak döner
     */
    public Optional<User> getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email adresi boş olamaz");
        }
        return userRepository.findByEmail(email);
    }

    /**
     * Email ve şifre ile kullanıcı girişi kontrol eder.
     * @param email Kullanıcının email adresi
     * @param password Kullanıcının şifresi
     * @return Giriş başarılıysa kullanıcı, değilse Optional.empty()
     */
    public Optional<User> authenticateUser(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email adresi boş olamaz");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Şifre boş olamaz");
        }

        try {
            Optional<User> user = userRepository.findByEmail(email);
            if (user.isPresent() && user.get().getPassword() != null && 
                user.get().getPassword().equals(password)) {
                logger.info("Kullanıcı başarıyla giriş yaptı: {}", email);
                return user;
            } else {
                logger.warn("Başarısız giriş denemesi: {}", email);
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Kullanıcı doğrulama sırasında hata", e);
            throw new DatabaseException("Kullanıcı doğrulama sırasında hata oluştu", e);
        }
    }

    /**
     * Kullanıcı kayıt validasyonu yapar.
     * @param user Kayıt olacak kullanıcı
     */
    private void validateUserForRegistration(User user) {
        if (user == null) {
            throw new ValidationException("Kullanıcı bilgileri boş olamaz");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email adresi boş olamaz");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new ValidationException("Şifre boş olamaz");
        }
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            throw new ValidationException("Ad boş olamaz");
        }
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            throw new ValidationException("Soyad boş olamaz");
        }
        
        // Email format kontrolü (basit)
        if (!user.getEmail().contains("@")) {
            throw new ValidationException("Geçerli bir email adresi giriniz");
        }
        
        // Şifre uzunluk kontrolü
        if (user.getPassword().length() < 6) {
            throw new ValidationException("Şifre en az 6 karakter olmalıdır");
        }
    }
}
