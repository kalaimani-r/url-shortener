package com.example.urlshortener.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateShortLinkRequest(
        @Size(min = 1, max = 2048) String longUrl,
        @Pattern(regexp = "^[0-9a-zA-Z_-]{3,16}$", message = "customAlias must be 3-16 alphanumeric chars")
        String customAlias,
        Integer ttlDays) {}
