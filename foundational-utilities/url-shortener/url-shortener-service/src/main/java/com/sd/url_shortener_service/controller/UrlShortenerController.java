package com.sd.url_shortener_service.controller;

import com.sd.url_shortener_service.dto.UrlRequestDto;
import com.sd.url_shortener_service.dto.UrlResponseDto;
import com.sd.url_shortener_service.service.UrlShortenerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;

    @PostMapping("/api/v1/urls")
    public ResponseEntity<UrlResponseDto> shortenUrl(@Valid @RequestBody UrlRequestDto request) {
        log.info("Request received to shorten URL: {}", request.getLongUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(urlShortenerService.shortenUrl(request));
    }

    @GetMapping("/{shortKey}")
    public ResponseEntity<Void> redirect(@PathVariable String shortKey) {
        log.info("Request received to redirect for key: {}", shortKey);
        String originalUrl = urlShortenerService.getOriginalUrl(shortKey);
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .location(URI.create(originalUrl))
                .build();
    }
}
