package com.cw.scheduler.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key, int capacity, int refillTokens, int refillDurationSeconds) {
        return cache.computeIfAbsent(key, k -> newBucket(capacity, refillTokens, refillDurationSeconds));
    }

    private Bucket newBucket(int capacity, int refillTokens, int refillDurationSeconds) {
        Refill refill = Refill.intervally(refillTokens, Duration.ofSeconds(refillDurationSeconds));
        Bandwidth limit = Bandwidth.classic(capacity, refill);

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

}
