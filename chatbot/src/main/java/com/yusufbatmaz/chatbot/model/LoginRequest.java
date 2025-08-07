package com.yusufbatmaz.chatbot.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Login isteği için kullanılan DTO sınıfı.
 * Sadece email ve password alanlarını içerir.
 */
@Data
public class LoginRequest {
    
    @NotBlank(message = "Email boş olamaz")
    @Email(message = "Geçerli bir email adresi giriniz")
    private String email;
    
    @NotBlank(message = "Şifre boş olamaz")
    private String password;
} 