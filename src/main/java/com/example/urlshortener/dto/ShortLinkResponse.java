package com.example.urlshortener.dto;

import java.time.Instant;

public record ShortLinkResponse(
        String shortCode,
        String shortUrl,
        String longUrl,
        long clickCount,
        Instant createdAt,
        Instant expiresAt) {}
