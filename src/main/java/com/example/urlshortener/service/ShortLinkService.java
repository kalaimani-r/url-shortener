package com.example.urlshortener.service;

import com.example.urlshortener.config.AppProperties;
import com.example.urlshortener.domain.ShortLink;
import com.example.urlshortener.dto.CreateShortLinkRequest;
import com.example.urlshortener.dto.LinkStatsResponse;
import com.example.urlshortener.dto.ShortLinkResponse;
import com.example.urlshortener.exception.AliasConflictException;
import com.example.urlshortener.exception.LinkExpiredException;
import com.example.urlshortener.exception.LinkNotFoundException;
import com.example.urlshortener.repository.ShortLinkRepository;
import com.example.urlshortener.util.Base62;
import com.example.urlshortener.util.UrlNormalizer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShortLinkService {

    private final ShortLinkRepository repository;
    private final AppProperties properties;
    private final AnalyticsService analyticsService;

    public ShortLinkService(
            ShortLinkRepository repository, AppProperties properties, AnalyticsService analyticsService) {
        this.repository = repository;
        this.properties = properties;
        this.analyticsService = analyticsService;
    }

    @Transactional
    public ShortLinkResponse create(CreateShortLinkRequest request) {
        String longUrl = UrlNormalizer.normalize(request.longUrl());
        String hash = UrlNormalizer.sha256Hex(longUrl);

        if (properties.isDeduplicateLongUrls() && request.customAlias() == null) {
            Optional<ShortLink> existing = repository.findByLongUrlHash(hash);
            if (existing.isPresent() && !existing.get().isExpired(Instant.now())) {
                return toResponse(existing.get());
            }
        }

        if (request.customAlias() != null && !request.customAlias().isBlank()) {
            String alias = request.customAlias().trim();
            if (repository.existsByShortCode(alias)) {
                throw new AliasConflictException(alias);
            }
            ShortLink link = new ShortLink();
            link.setShortCode(alias);
            link.setLongUrl(longUrl);
            link.setLongUrlHash(hash);
            link.setExpiresAt(resolveExpiry(request.ttlDays()));
            return toResponse(repository.save(link));
        }

        ShortLink link = new ShortLink();
        link.setLongUrl(longUrl);
        link.setLongUrlHash(hash);
        link.setExpiresAt(resolveExpiry(request.ttlDays()));
        link.setShortCode("tmp");
        ShortLink saved = repository.save(link);
        saved.setShortCode(Base62.encode(saved.getId()));
        return toResponse(repository.save(saved));
    }

    @Transactional(readOnly = true)
    public ShortLinkResponse getByCode(String shortCode) {
        ShortLink link = findActive(shortCode);
        return toResponse(link);
    }

    @Transactional(readOnly = true)
    public LinkStatsResponse getStats(String shortCode) {
        ShortLink link = findActive(shortCode);
        long events = analyticsService.countEvents(shortCode);
        return new LinkStatsResponse(
                link.getShortCode(),
                link.getLongUrl(),
                link.getClickCount(),
                events,
                link.getCreatedAt(),
                link.getExpiresAt());
    }

    @Cacheable(value = "redirectTargets", key = "#shortCode", unless = "#result == null")
    @Transactional(readOnly = true)
    public String resolveLongUrl(String shortCode) {
        ShortLink link = findActive(shortCode);
        return link.getLongUrl();
    }

    @CacheEvict(value = "redirectTargets", key = "#shortCode")
    @Transactional
    public void recordRedirect(String shortCode, String userAgent, String referrer, String clientIp) {
        repository.incrementClickCount(shortCode);
        analyticsService.recordClickAsync(shortCode, userAgent, referrer, clientIp);
    }

    private ShortLink findActive(String shortCode) {
        ShortLink link = repository
                .findByShortCode(shortCode)
                .orElseThrow(() -> new LinkNotFoundException(shortCode));
        if (link.isExpired(Instant.now())) {
            throw new LinkExpiredException(shortCode);
        }
        return link;
    }

    private Instant resolveExpiry(Integer ttlDays) {
        int days = ttlDays != null ? ttlDays : properties.getDefaultTtlDays();
        if (days <= 0) {
            return null;
        }
        return Instant.now().plus(days, ChronoUnit.DAYS);
    }

    private ShortLinkResponse toResponse(ShortLink link) {
        String shortUrl = properties.getBaseUrl().replaceAll("/$", "") + "/" + link.getShortCode();
        return new ShortLinkResponse(
                link.getShortCode(),
                shortUrl,
                link.getLongUrl(),
                link.getClickCount(),
                link.getCreatedAt(),
                link.getExpiresAt());
    }
}
