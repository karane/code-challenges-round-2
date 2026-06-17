package com.karane.stresstest;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class StressTestConfigTest {

    @Test
    void buildsWithDefaults() {
        StressTestConfig config = StressTestConfig.builder("http://example.com").build();
        
        assertEquals("http://example.com", config.url());
        assertEquals("GET", config.method());
        assertEquals(10, config.concurrentUsers());
        assertEquals(100, config.totalRequests());
        assertEquals(Duration.ofSeconds(10), config.timeout());
    }

    @Test
    void normalizesMethodToUpperCase() {
        StressTestConfig config = StressTestConfig.builder("http://example.com").method("post").build();
        assertEquals("POST", config.method());
    }

    @Test
    void rejectsBlankUrl() {
        assertThrows(IllegalArgumentException.class, () ->
                StressTestConfig.builder("  ").build());
    }

    @Test
    void rejectsZeroConcurrentUsers() {
        assertThrows(IllegalArgumentException.class, () ->
                StressTestConfig.builder("http://example.com").concurrentUsers(0).build());
    }

    @Test
    void rejectsZeroTotalRequests() {
        assertThrows(IllegalArgumentException.class, () ->
                StressTestConfig.builder("http://example.com").totalRequests(0).build());
    }

    @Test
    void rejectsZeroTimeout() {
        assertThrows(IllegalArgumentException.class, () ->
                StressTestConfig.builder("http://example.com").timeout(Duration.ZERO).build());
    }

    @Test
    void storesHeaders() {
        StressTestConfig config = StressTestConfig.builder("http://example.com")
                .header("Authorization", "Bearer token")
                .build();
        assertEquals("Bearer token", config.headers().get("Authorization"));
    }
}
