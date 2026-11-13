package com.example.urlshortener.web;

import com.example.urlshortener.dto.CreateShortLinkRequest;
import com.example.urlshortener.dto.LinkStatsResponse;
import com.example.urlshortener.dto.ShortLinkResponse;
import com.example.urlshortener.service.ShortLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/links")
@Tag(name = "Links", description = "Create and inspect short links")
public class ShortLinkApiController {

    private final ShortLinkService shortLinkService;

    public ShortLinkApiController(ShortLinkService shortLinkService) {
        this.shortLinkService = shortLinkService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a short link")
    public ShortLinkResponse create(@Valid @RequestBody CreateShortLinkRequest request) {
        return shortLinkService.create(request);
    }

    @GetMapping("/{shortCode}")
    @Operation(summary = "Get metadata for a short link")
    public ShortLinkResponse get(@PathVariable String shortCode) {
        return shortLinkService.getByCode(shortCode);
    }

    @GetMapping("/{shortCode}/stats")
    @Operation(summary = "Get click stats (counter + event rows)")
    public LinkStatsResponse stats(@PathVariable String shortCode) {
        return shortLinkService.getStats(shortCode);
    }
}
