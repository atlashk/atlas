package org.atlas.infrastructure.event.handler.interceptor;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.domain.event.DomainEvent;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class IdempotencyEventHandlerInterceptor implements EventHandlerInterceptor {

  private final ApplicationConfigPort applicationConfigPort;
  private final RedisTemplate<String, Object> redisTemplate;

  private static final String PROCESSING_REDIS_VALUE = "processing";
  private static final Duration PROCESSING_TIMEOUT = Duration.ofMinutes(15);
  private static final String PROCESSED_REDIS_VALUE = "processed";
  private static final Duration PROCESSED_TTL = Duration.ofDays(7);

  @Override
  public void preHandle(DomainEvent event) {
    String redisKey = obtainRedisKey(event);
    String redisValue = (String) redisTemplate.opsForValue().get(redisKey);

    // Check if the event has already been processed
    if (PROCESSED_REDIS_VALUE.equals(redisValue)) {
      throw new IllegalStateException(
          String.format("Event %s has already been processed", event.getEventId()));
    }

    // Try to acquire a lock for processing
    if (Boolean.FALSE.equals(redisTemplate.opsForValue()
        .setIfAbsent(redisKey, PROCESSING_REDIS_VALUE, PROCESSING_TIMEOUT))) {
      // If the lock is already held, it means other instance has been processing the event
      throw new IllegalStateException(
          String.format("Event %s is already being processed by other instance",
              event.getEventId()));
    }
  }

  @Override
  public void postHandle(DomainEvent event) {
    String redisKey = obtainRedisKey(event);

    if (event.isProcessed()) {
      redisTemplate.opsForValue().set(redisKey, PROCESSED_REDIS_VALUE, PROCESSED_TTL);
    }

    // Release the lock of processing acquisition
    redisTemplate.delete(redisKey);
  }

  private String obtainRedisKey(DomainEvent event) {
    return String.format("event:%s:%s",
        event.getEventId(), applicationConfigPort.getApplicationName());
  }
}
