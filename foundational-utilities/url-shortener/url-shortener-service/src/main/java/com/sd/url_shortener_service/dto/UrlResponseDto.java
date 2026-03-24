package com.sd.url_shortener_service.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlResponseDto {
    private String shortUrl;
    private String originalUrl;
    private LocalDateTime expiresAt;
}
