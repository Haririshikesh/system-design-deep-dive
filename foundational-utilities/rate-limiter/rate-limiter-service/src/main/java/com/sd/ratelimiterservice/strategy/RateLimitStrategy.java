package com.sd.ratelimiterservice.strategy;

import com.sd.ratelimiterservice.dto.RateLimitResponse;
import reactor.core.publisher.Mono;

/**
 * Strategy Pattern Interface for computing Rate Limits.
 * Implementing this allows us to easily switch between Token Bucket, Leaky Bucket,
 * or Fixed Window approaches without changing the filter logic.
 */
public interface RateLimitStrategy {
    
    /**
     * Checks if the request should be allowed.
     * 
     * @param key Identifies the specific limit bucket (e.g., IP address or User ID)
     * @param capacity The maximum number of tokens
     * @param refillRate The rate at which the bucket refills (tokens per second)
     * @param requestedTokens The number of tokens required for the current request
     * @return Mono<RateLimitResponse> containing the decision and remainder
     */
    Mono<RateLimitResponse> isAllowed(String key, int capacity, int refillRate, int requestedTokens);
}
