package com.sd.url_shortener_service.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlRequestDto {
    @NotBlank(message = "URL cannot be empty")
    @URL(message = "Invalid URL format")
    private String longUrl;
}
