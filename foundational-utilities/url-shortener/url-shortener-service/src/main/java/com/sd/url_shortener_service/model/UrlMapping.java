package com.sd.url_shortener_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;
import java.time.LocalDateTime;

@Entity
@Table(name = "url_mappings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlMapping implements Persistable<Long> {
    @Id
    private Long id;

    @Column(unique = true, nullable = false, length = 10)
    private String shortKey;

    @Column(nullable = false, length = 2048)
    private String originalUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    @Override
    public boolean isNew() {
        return true;
    }
}
