package org.atlas.infrastructure.resilience.resilience4j;

import io.github.resilience4j.common.bulkhead.configuration.BulkheadConfigCustomizer;
import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import io.github.resilience4j.common.retry.configuration.RetryConfigCustomizer;
import io.github.resilience4j.common.timelimiter.configuration.TimeLimiterConfigCustomizer;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import org.atlas.framework.domain.exception.DomainException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Resilience4jConfig {

  @Bean
  @SuppressWarnings("unchecked")
  public RetryConfigCustomizer retryConfigCustomizer() {
    return RetryConfigCustomizer.of("default", builder -> {
      builder
          .maxAttempts(4)
          .waitDuration(Duration.ofMillis(1000))
          .retryExceptions(IOException.class, TimeoutException.class)
          .ignoreExceptions(DomainException.class);
    });
  }

  @Bean
  public CircuitBreakerConfigCustomizer circuitBreakerConfigCustomizer() {
    return CircuitBreakerConfigCustomizer.of("default", builder -> {
      builder
          // Number of calls to consider when calculating failure rate
          .slidingWindowSize(10)
          // Minimum number of calls required in the sliding window before evaluating the failure rate.
          // Here it will evaluate the last 10 calls but only if at least 5 calls have been made.
          .minimumNumberOfCalls(5)
          // If more than 50% of calls in the window fail, the Circuit Breaker will open
          .failureRateThreshold(50)
          // Circuit Breaker will remain open for 10 seconds (10000 ms)
          // after opening before transitioning to half-open state
          .waitDurationInOpenState(Duration.ofMillis(10000))
          // In half-open state, allow 2 calls to test if the underlying service has recovered
          .permittedNumberOfCallsInHalfOpenState(2)
          .recordExceptions(IOException.class, TimeoutException.class)
          .ignoreExceptions(DomainException.class);
    });
  }

  @Bean
  public BulkheadConfigCustomizer bulkheadConfigCustomizer() {
    return BulkheadConfigCustomizer.of("default", builder -> {
      builder
          .maxConcurrentCalls(5);
    });
  }

  @Bean
  public TimeLimiterConfigCustomizer timeLimiterConfigCustomizer() {
    return TimeLimiterConfigCustomizer.of("default", builder -> {
      builder
          .timeoutDuration(Duration.ofSeconds(30));
    });
  }
}
