package com.example.urlshortener.service;

import com.example.urlshortener.domain.ClickEvent;
import com.example.urlshortener.repository.ClickEventRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {

    private final ClickEventRepository clickEventRepository;

    public AnalyticsService(ClickEventRepository clickEventRepository) {
        this.clickEventRepository = clickEventRepository;
    }

    @Async("clickEventExecutor")
    @Transactional
    public void recordClickAsync(String shortCode, String userAgent, String referrer, String clientIp) {
        ClickEvent event = new ClickEvent();
        event.setShortCode(shortCode);
        event.setUserAgent(truncate(userAgent, 512));
        event.setReferrer(truncate(referrer, 2048));
        event.setClientIp(truncate(clientIp, 45));
        clickEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public long countEvents(String shortCode) {
        return clickEventRepository.countByShortCode(shortCode);
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
