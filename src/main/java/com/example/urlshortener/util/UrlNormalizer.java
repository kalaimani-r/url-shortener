package com.example.urlshortener.util;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class UrlNormalizer {

    private UrlNormalizer() {}

    public static String normalize(String raw) {
        String trimmed = raw.trim();
        if (!trimmed.matches("(?i)^https?://.*")) {
            trimmed = "https://" + trimmed;
        }
        URI uri = URI.create(trimmed);
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException("URL must have a host");
        }
        return uri.toString();
    }

    public static String sha256Hex(String normalizedUrl) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalizedUrl.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
