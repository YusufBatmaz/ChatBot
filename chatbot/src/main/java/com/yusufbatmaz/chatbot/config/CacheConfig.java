package com.yusufbatmaz.chatbot.config;

import java.time.Duration;
import java.util.List;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Caching konfigürasyonu.
 * Performans optimizasyonu için cache yönetimi sağlar.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Cache manager bean'i.
     * Basit in-memory cache kullanıyoruz.
     * 
     * @return CacheManager
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        
        // Cache isimlerini tanımlıyoruz
        cacheManager.setCacheNames(List.of(
            "users",           // Kullanıcı cache'i
            "chatHistory",     // Chat geçmişi cache'i
            "categories",      // Kategori cache'i
            "rateLimits"       // Rate limit cache'i
        ));
        
        return cacheManager;
    }

    /**
     * Cache TTL (Time To Live) değerleri.
     */
    public static class CacheTTL {
        public static final Duration USER_CACHE_TTL = Duration.ofMinutes(30);
        public static final Duration CHAT_HISTORY_CACHE_TTL = Duration.ofMinutes(10);
        public static final Duration CATEGORY_CACHE_TTL = Duration.ofHours(1);
        public static final Duration RATE_LIMIT_CACHE_TTL = Duration.ofMinutes(1);
    }
} 