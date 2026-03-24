package com.sd.url_shortener_service.repository;

import com.sd.url_shortener_service.model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<UrlMapping, Long> {
    Optional<UrlMapping> findByShortKey(String shortKey);

    @Query(value = "SELECT nextval('url_id_seq')", nativeQuery = true)
    Long getNextId();
}
