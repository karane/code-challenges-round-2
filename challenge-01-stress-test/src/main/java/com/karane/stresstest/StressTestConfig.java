package com.karane.stresstest;

import java.time.Duration;
import java.util.Map;

public record StressTestConfig(
        String url,
        String method,
        int concurrentUsers,
        int totalRequests,
        Duration timeout,
        String body,
        java.util.Map<String, String> headers
) {
    public StressTestConfig {
        if (url == null || url.isBlank()) throw new IllegalArgumentException("URL must not be blank");
        if (concurrentUsers < 1) throw new IllegalArgumentException("concurrentUsers must be >= 1");
        if (totalRequests < 1) throw new IllegalArgumentException("totalRequests must be >= 1");
        if (timeout == null || timeout.isNegative() || timeout.isZero())
            throw new IllegalArgumentException("timeout must be positive");

        method = method == null ? "GET" : method.toUpperCase();
        headers = headers == null ? Map.of() : java.util.Collections.unmodifiableMap(headers);
    }

    public static Builder builder(String url) {
        return new Builder(url);
    }

    public static final class Builder {
        private final String url;
        private String method = "GET";
        private int concurrentUsers = 10;
        private int totalRequests = 100;
        private Duration timeout = Duration.ofSeconds(10);
        private String body = null;
        private java.util.Map<String, String> headers = new java.util.HashMap<>();

        private Builder(String url) { 
            this.url = url; 
        }

        public Builder method(String method) { 
            this.method = method; return this; 
        }

        public Builder concurrentUsers(int n) { 
            this.concurrentUsers = n; return this; 
        }

        public Builder totalRequests(int n) { 
            this.totalRequests = n; return this; 
        }
        
        public Builder timeout(Duration d) {
            this.timeout = d;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder header(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        public StressTestConfig build() {
            return new StressTestConfig(
                    url, method, concurrentUsers, totalRequests, timeout, body, headers
            );
        }
    }
}
