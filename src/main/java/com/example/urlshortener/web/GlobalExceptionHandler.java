package com.example.urlshortener.web;

import com.example.urlshortener.dto.ErrorResponse;
import com.example.urlshortener.exception.AliasConflictException;
import com.example.urlshortener.exception.LinkExpiredException;
import com.example.urlshortener.exception.LinkNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LinkNotFoundException.class)
    ResponseEntity<ErrorResponse> notFound(LinkNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of("not_found", ex.getMessage()));
    }

    @ExceptionHandler(LinkExpiredException.class)
    ResponseEntity<ErrorResponse> expired(LinkExpiredException ex) {
        return ResponseEntity.status(HttpStatus.GONE).body(ErrorResponse.of("expired", ex.getMessage()));
    }

    @ExceptionHandler(AliasConflictException.class)
    ResponseEntity<ErrorResponse> conflict(AliasConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.of("alias_conflict", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ErrorResponse.of("bad_request", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("validation failed");
        return ResponseEntity.badRequest().body(ErrorResponse.of("validation_error", message));
    }
}
