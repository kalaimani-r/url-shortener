package com.example.urlshortener.dto;

import java.time.Instant;

public record LinkStatsResponse(
        String shortCode,
        String longUrl,
        long clickCount,
        long recordedEvents,
        Instant createdAt,
        Instant expiresAt) {}
