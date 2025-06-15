package org.atlas.edge.gateway.springcloudgateway.ratelimiter;

import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimiterConfig {

    @Bean
    public RedisRateLimiter anonymousRedisRateLimiter() {
        return new RedisRateLimiter(1, 5);
    }

    @Bean
    public RedisRateLimiter userRedisRateLimiter() {
        return new RedisRateLimiter(1, 10);
    }
}
