package com.yusufbatmaz.chatbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.Data;

/**
 * OpenRouter API konfigürasyonu için properties sınıfı.
 * application.properties'den değerleri otomatik olarak alır.
 */
@Configuration
@ConfigurationProperties(prefix = "openrouter.api")
@Data
public class ApiConfig {
    
    /**
     * OpenRouter API anahtarı
     */
    private String key;
    
    /**
     * OpenRouter API URL'i
     */
    private String url;
    
    /**
     * API timeout süresi (saniye)
     */
    private int timeout = 30;

    // WebClient bean'i ekleniyor
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(url)
                .defaultHeader("Authorization", "Bearer " + key)
                .build();
    }
} 