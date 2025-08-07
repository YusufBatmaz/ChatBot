package com.yusufbatmaz.chatbot.controller;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yusufbatmaz.chatbot.exception.NotFoundException;
import com.yusufbatmaz.chatbot.model.LoginRequest;
import com.yusufbatmaz.chatbot.model.User;
import com.yusufbatmaz.chatbot.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Kullanıcı ile ilgili HTTP isteklerini karşılayan controller sınıfı.
 * Frontend'den gelen istekleri alır, UserService ile iletişim kurar ve cevap döner.
 */
@CrossOrigin(origins = "http://localhost:5174")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Kullanıcı kaydı için endpoint.
     * Frontend'den gelen kullanıcı bilgilerini alır, yeni kullanıcı oluşturur.
     * Eğer aynı email ile kayıt varsa 409 (Conflict) döner.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        // @Valid annotation'ı ile Bean Validation çalışır
        // Eğer validation hatası varsa MethodArgumentNotValidException fırlatılır
        
        // Aynı email ile kayıt kontrolü
        if (userService.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Bu email ile zaten kayıtlı bir kullanıcı var.");
        }
        
        // Yeni kullanıcı için ID null olmalı
        user.setId(null);
        
        // Kullanıcıyı oluştur (UserService'de validation ve exception handling var)
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }

    /**
     * ID ile kullanıcıyı getirir.
     * @param id Kullanıcı UUID
     * @return Kullanıcı veya 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable UUID id) {
        // Kullanıcıyı ID ile bul, bulunamazsa NotFoundException fırlat
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı: " + id));
    }

    /**
     * Email ile kullanıcıyı getirir.
     * @param email Kullanıcı email
     * @return Kullanıcı veya 404
     */
    @GetMapping("/email")
    public ResponseEntity<User> getUserByEmail(@RequestParam String email) {
        // Kullanıcıyı email ile bul, bulunamazsa 404 döndür
        return userService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Kullanıcı girişi için endpoint.
     * Email ve şifre ile kullanıcıyı kontrol eder.
     * @param user Giriş yapan kullanıcı bilgileri (email ve password gerekli)
     * @return Başarılıysa kullanıcı, değilse 401 (Unauthorized)
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        // @Valid annotation'ı ile Bean Validation çalışır
        
        // Email ve şifre null kontrolü (ekstra güvenlik)
        if (loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email ve şifre gerekli.");
        }
        
        // Kullanıcı doğrulama (UserService'de exception handling var)
        Optional<User> authenticatedUser = userService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
        
        if (authenticatedUser.isPresent()) {
            // Giriş başarılı
            return ResponseEntity.ok(authenticatedUser.get());
        } else {
            // Giriş başarısız - güvenlik için genel mesaj
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email veya şifre yanlış.");
        }
    }
}
