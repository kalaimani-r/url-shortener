package com.example.urlshortener.repository;

import com.example.urlshortener.domain.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    long countByShortCode(String shortCode);
}
