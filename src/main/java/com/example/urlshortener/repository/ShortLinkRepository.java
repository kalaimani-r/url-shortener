package com.example.urlshortener.repository;

import com.example.urlshortener.domain.ShortLink;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShortLinkRepository extends JpaRepository<ShortLink, Long> {

    Optional<ShortLink> findByShortCode(String shortCode);

    Optional<ShortLink> findByLongUrlHash(String longUrlHash);

    boolean existsByShortCode(String shortCode);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ShortLink s set s.clickCount = s.clickCount + 1 where s.shortCode = :code")
    int incrementClickCount(@Param("code") String shortCode);
}
