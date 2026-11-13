package com.example.urlshortener.exception;

public class LinkExpiredException extends RuntimeException {

    private final String shortCode;

    public LinkExpiredException(String shortCode) {
        super("Short link expired: " + shortCode);
        this.shortCode = shortCode;
    }

    public String getShortCode() {
        return shortCode;
    }
}
