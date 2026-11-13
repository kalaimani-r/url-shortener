package com.example.urlshortener.exception;

public class LinkNotFoundException extends RuntimeException {

    private final String shortCode;

    public LinkNotFoundException(String shortCode) {
        super("Short link not found: " + shortCode);
        this.shortCode = shortCode;
    }

    public String getShortCode() {
        return shortCode;
    }
}
