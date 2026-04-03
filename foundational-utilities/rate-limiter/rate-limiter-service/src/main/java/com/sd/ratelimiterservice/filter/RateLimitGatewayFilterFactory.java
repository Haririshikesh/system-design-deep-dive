package com.sd.ratelimiterservice.filter;

import com.sd.ratelimiterservice.strategy.RateLimitStrategy;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

/**
 * Custom GatewayFilterFactory using our GoF Strategy pattern.
 * This filter extracts the requested limits from application.yml 
 * and applies the rate-limiting strategy (e.g. Token Bucket) via Redis.
 */
@Component
@Slf4j
public class RateLimitGatewayFilterFactory extends AbstractGatewayFilterFactory<RateLimitGatewayFilterFactory.Config> {

    private final RateLimitStrategy rateLimitStrategy;

    public RateLimitGatewayFilterFactory(RateLimitStrategy rateLimitStrategy) {
        super(Config.class);
        this.rateLimitStrategy = rateLimitStrategy;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("capacity", "refillRate", "requestedTokens");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Determine key (e.g., using IP address or simple route ID)
            String key = Objects.requireNonNull(
                    exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress();

            log.debug("Checking rate limit for key: {}", key);

            return rateLimitStrategy.isAllowed(
                    key, 
                    config.getCapacity(), 
                    config.getRefillRate(), 
                    config.getRequestedTokens()
            ).flatMap(response -> {
                // Set standard rate limit headers
                exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(response.getRemainingTokens()));
                
                if (response.isAllowed()) {
                    return chain.filter(exchange);
                } else {
                    log.warn("Rate limit exceeded for key: {}", key);
                    exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    exchange.getResponse().getHeaders().add("X-RateLimit-Retry-After", "1"); // naive fallback
                    return exchange.getResponse().setComplete();
                }
            });
        };
    }

    @Data
    public static class Config {
        private int capacity;
        private int refillRate;
        private int requestedTokens = 1;
    }
}
