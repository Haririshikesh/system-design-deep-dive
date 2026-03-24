package com.sd.url_shortener_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.shortener")
@Data
public class AppConfig {
    private String baseUrl;
    private int keyLength;
    private int ttlDays;
}
