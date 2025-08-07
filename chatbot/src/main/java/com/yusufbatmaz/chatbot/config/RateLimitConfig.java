package com.yusufbatmaz.chatbot.config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

/**
 * Basit rate limiting implementasyonu.
 * Kullanıcı başına API çağrı sayısını sınırlar.
 */
@Component
public class RateLimitConfig {

    // Kullanıcı başına maksimum API çağrı sayısı (dakika başına)
    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    
    // Kullanıcı başına çağrı sayılarını tutan map
    private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    
    // Son reset zamanını tutan map
    private final ConcurrentHashMap<String, Long> lastResetTimes = new ConcurrentHashMap<>();

    /**
     * Kullanıcının rate limit kontrolünü yapar.
     * 
     * @param userId Kullanıcı ID'si
     * @return Rate limit aşıldıysa true, aşılmadıysa false
     */
    public boolean isRateLimitExceeded(String userId) {
        long currentTime = System.currentTimeMillis();
        long oneMinuteAgo = currentTime - (60 * 1000); // 1 dakika öncesi
        
        // Son reset zamanını kontrol et
        Long lastReset = lastResetTimes.get(userId);
        if (lastReset == null || lastReset < oneMinuteAgo) {
            // 1 dakika geçmişse sayacı sıfırla
            requestCounts.put(userId, new AtomicInteger(0));
            lastResetTimes.put(userId, currentTime);
        }
        
        // Mevcut çağrı sayısını al ve artır
        AtomicInteger count = requestCounts.computeIfAbsent(userId, k -> new AtomicInteger(0));
        int currentCount = count.incrementAndGet();
        
        return currentCount > MAX_REQUESTS_PER_MINUTE;
    }

    /**
     * Kullanıcının kalan çağrı hakkını döner.
     * 
     * @param userId Kullanıcı ID'si
     * @return Kalan çağrı hakkı
     */
    public int getRemainingRequests(String userId) {
        AtomicInteger count = requestCounts.get(userId);
        if (count == null) {
            return MAX_REQUESTS_PER_MINUTE;
        }
        return Math.max(0, MAX_REQUESTS_PER_MINUTE - count.get());
    }

    /**
     * Rate limit bilgilerini döner.
     * 
     * @param userId Kullanıcı ID'si
     * @return Rate limit bilgileri
     */
    public RateLimitInfo getRateLimitInfo(String userId) {
        return new RateLimitInfo(
            getRemainingRequests(userId),
            MAX_REQUESTS_PER_MINUTE,
            isRateLimitExceeded(userId)
        );
    }

    /**
     * Rate limit bilgilerini tutan inner class.
     */
    public static class RateLimitInfo {
        private final int remainingRequests;
        private final int maxRequests;
        private final boolean exceeded;

        public RateLimitInfo(int remainingRequests, int maxRequests, boolean exceeded) {
            this.remainingRequests = remainingRequests;
            this.maxRequests = maxRequests;
            this.exceeded = exceeded;
        }

        public int getRemainingRequests() { return remainingRequests; }
        public int getMaxRequests() { return maxRequests; }
        public boolean isExceeded() { return exceeded; }
    }
} 