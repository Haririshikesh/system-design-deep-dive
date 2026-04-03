package com.sd.ratelimiterservice.strategy;

import com.sd.ratelimiterservice.dto.RateLimitResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Token Bucket implementation of the RateLimitStrategy.
 */
@Component
public class TokenBucketStrategy implements RateLimitStrategy {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final RedisScript<List> script;

    public TokenBucketStrategy(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.script = RedisScript.of(new ClassPathResource("scripts/token_bucket.lua"), List.class);
    }

    @Override
    public Mono<RateLimitResponse> isAllowed(String key, int capacity, int refillRate, int requestedTokens) {
        // Lua Script Keys
        // KEYS[1] : Key for the tokens
        // KEYS[2] : Key for the timestamp
        List<String> keys = List.of(
                "rate_limit:tokens:" + key,
                "rate_limit:timestamp:" + key
        );

        // Lua Script Args
        // ARGV[1] : capacity
        // ARGV[2] : refillRate
        // ARGV[3] : Unix time in seconds
        // ARGV[4] : requestedTokens
        String now = String.valueOf(Instant.now().getEpochSecond());
        
        return redisTemplate.execute(this.script, keys, List.of(
                String.valueOf(capacity),
                String.valueOf(refillRate),
                now,
                String.valueOf(requestedTokens)
        )).next().map(results -> {
            boolean allowed = (Long) results.get(0) == 1L;
            long tokensLeft = (Long) results.get(1);
            return RateLimitResponse.builder()
                    .isAllowed(allowed)
                    .remainingTokens(tokensLeft)
                    .build();
        });
    }
}
