package org.atlas.infrastructure.cache.redis.config;

import java.time.Duration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

@Configuration
@EnableCaching
public class RedisCacheConfig {

  public static final String DEFAULT_CACHE_NAME = "default";

  /**
   * This configuration will be applied to all caches in your application.
   */
  @Bean
  public RedisCacheConfiguration cacheConfiguration() {
    return RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(15))
        .disableCachingNullValues();
  }
}
