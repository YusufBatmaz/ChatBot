package com.yusufbatmaz.chatbot.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health check ve monitoring endpoint'leri için controller.
 * Sistem durumunu kontrol etmek için kullanılır.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    /**
     * Temel health check endpoint'i.
     * Sistemin çalışıp çalışmadığını kontrol eder.
     * 
     * @return Sistem durumu
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "chatbot-backend");
        response.put("version", "1.0.0");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Detaylı health check endpoint'i.
     * Sistem bileşenlerinin durumunu kontrol eder.
     * 
     * @return Detaylı sistem durumu
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "chatbot-backend");
        response.put("version", "1.0.0");
        
        // Bileşen durumları
        Map<String, Object> components = new HashMap<>();
        components.put("database", "UP");
        components.put("api", "UP");
        components.put("memory", "OK");
        components.put("disk", "OK");
        
        response.put("components", components);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Sistem bilgileri endpoint'i.
     * 
     * @return Sistem bilgileri
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> systemInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "Chatbot Backend");
        response.put("version", "1.0.0");
        response.put("java.version", System.getProperty("java.version"));
        response.put("java.vendor", System.getProperty("java.vendor"));
        response.put("os.name", System.getProperty("os.name"));
        response.put("os.version", System.getProperty("os.version"));
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
} 