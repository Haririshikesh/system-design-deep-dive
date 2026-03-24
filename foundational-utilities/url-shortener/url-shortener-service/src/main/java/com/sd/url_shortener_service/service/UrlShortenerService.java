package com.sd.url_shortener_service.service;

import com.sd.url_shortener_service.config.AppConfig;
import com.sd.url_shortener_service.dto.UrlRequestDto;
import com.sd.url_shortener_service.dto.UrlResponseDto;
import com.sd.url_shortener_service.exception.ResourceNotFoundException;
import com.sd.url_shortener_service.model.UrlMapping;
import com.sd.url_shortener_service.repository.UrlRepository;
import com.sd.url_shortener_service.util.Base62Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlShortenerService {

    private final UrlRepository urlRepository;
    private final StringRedisTemplate redisTemplate;
    private final AppConfig appConfig;

    @Transactional
    public UrlResponseDto shortenUrl(UrlRequestDto request) {
        log.info("Shortening URL: {}", request.getLongUrl());

        // 1. Fetch next ID from sequence first to avoid null constraint on shortKey
        Long id = urlRepository.getNextId();
        String shortKey = Base62Util.encode(id);

        UrlMapping mapping = UrlMapping.builder()
                .id(id)
                .shortKey(shortKey)
                .originalUrl(request.getLongUrl())
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(appConfig.getTtlDays()))
                .build();

        // 2. Save with ID and shortKey already set
        mapping = urlRepository.save(mapping);

        // 3. Cache in Redis
        redisTemplate.opsForValue().set(
                shortKey,
                request.getLongUrl(),
                appConfig.getTtlDays(),
                TimeUnit.DAYS
        );

        return UrlResponseDto.builder()
                .shortUrl(appConfig.getBaseUrl() + shortKey)
                .originalUrl(mapping.getOriginalUrl())
                .expiresAt(mapping.getExpiresAt())
                .build();
    }

    public String getOriginalUrl(String shortKey) {
        log.info("Fetching original URL for key: {}", shortKey);

        // 1. Check Redis Cache
        String cachedUrl = redisTemplate.opsForValue().get(shortKey);
        if (cachedUrl != null) {
            log.info("Cache hit for key: {}", shortKey);
            return cachedUrl;
        }

        // 2. Check Database
        log.info("Cache miss for key: {}. Checking database...", shortKey);
        UrlMapping mapping = urlRepository.findByShortKey(shortKey)
                .orElseThrow(() -> new ResourceNotFoundException("Short URL not found: " + shortKey));

        // 3. Update Cache
        redisTemplate.opsForValue().set(
                shortKey,
                mapping.getOriginalUrl(),
                appConfig.getTtlDays(),
                TimeUnit.DAYS
        );

        return mapping.getOriginalUrl();
    }
}
