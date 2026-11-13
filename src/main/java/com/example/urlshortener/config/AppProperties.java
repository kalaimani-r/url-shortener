package com.example.urlshortener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /** Public base URL used when building short links (e.g. https://go.example.com). */
    private String baseUrl = "http://localhost:8080";

    /** If true, return existing short link for the same long URL (content dedup). */
    private boolean deduplicateLongUrls = true;

    /** Default TTL in days; 0 = no expiry. */
    private int defaultTtlDays = 0;

    private Cache cache = new Cache();
    private RateLimit rateLimit = new RateLimit();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public boolean isDeduplicateLongUrls() {
        return deduplicateLongUrls;
    }

    public void setDeduplicateLongUrls(boolean deduplicateLongUrls) {
        this.deduplicateLongUrls = deduplicateLongUrls;
    }

    public int getDefaultTtlDays() {
        return defaultTtlDays;
    }

    public void setDefaultTtlDays(int defaultTtlDays) {
        this.defaultTtlDays = defaultTtlDays;
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimit rateLimit) {
        this.rateLimit = rateLimit;
    }

    public static class Cache {
        private long redirectTtlSeconds = 300;
        private long maximumSize = 10_000;

        public long getRedirectTtlSeconds() {
            return redirectTtlSeconds;
        }

        public void setRedirectTtlSeconds(long redirectTtlSeconds) {
            this.redirectTtlSeconds = redirectTtlSeconds;
        }

        public long getMaximumSize() {
            return maximumSize;
        }

        public void setMaximumSize(long maximumSize) {
            this.maximumSize = maximumSize;
        }
    }

    public static class RateLimit {
        private boolean enabled = true;
        private int requestsPerMinute = 60;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getRequestsPerMinute() {
            return requestsPerMinute;
        }

        public void setRequestsPerMinute(int requestsPerMinute) {
            this.requestsPerMinute = requestsPerMinute;
        }
    }
}
