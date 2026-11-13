package com.example.urlshortener.exception;

public class AliasConflictException extends RuntimeException {

    private final String alias;

    public AliasConflictException(String alias) {
        super("Custom alias already in use: " + alias);
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }
}
