package com.example.urlshortener.web;

import com.example.urlshortener.service.ShortLinkService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hot read path — optimized with in-process cache (Caffeine). Uses 302 so analytics can run
 * before the browser caches the target.
 */
@RestController
public class RedirectController {

    private final ShortLinkService shortLinkService;

    public RedirectController(ShortLinkService shortLinkService) {
        this.shortLinkService = shortLinkService;
    }

    @GetMapping("/{shortCode:[0-9a-zA-Z_-]{1,16}}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode, HttpServletRequest request) {
        String longUrl = shortLinkService.resolveLongUrl(shortCode);
        shortLinkService.recordRedirect(
                shortCode,
                request.getHeader(HttpHeaders.USER_AGENT),
                request.getHeader(HttpHeaders.REFERER),
                clientIp(request));
        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, longUrl).build();
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
