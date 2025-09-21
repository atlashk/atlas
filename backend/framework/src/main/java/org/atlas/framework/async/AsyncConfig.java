package org.atlas.framework.async;

import java.time.Duration;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration class for async operations with production-ready defaults.
 * Allows customization of thread pools, timeouts, and batch processing settings.
 */
@Getter
@Setter
@Builder
public class AsyncConfig {
  
  /**
   * Default timeout for async operations.
   */
  @Builder.Default
  private Duration defaultTimeout = Duration.ofMinutes(5);
  
  /**
   * Default batch size for processing large collections.
   */
  @Builder.Default
  private int defaultBatchSize = 100;
  
  /**
   * Maximum batch size to prevent memory issues.
   */
  @Builder.Default
  private int maxBatchSize = 1000;
  
  /**
   * Core pool size for custom thread pool.
   */
  @Builder.Default
  private int corePoolSize = Runtime.getRuntime().availableProcessors();
  
  /**
   * Maximum pool size for custom thread pool.
   */
  @Builder.Default
  private int maximumPoolSize = Runtime.getRuntime().availableProcessors() * 2;
  
  /**
   * Keep alive time for idle threads.
   */
  @Builder.Default
  private Duration keepAliveTime = Duration.ofMinutes(1);
  
  /**
   * Queue capacity for thread pool.
   */
  @Builder.Default
  private int queueCapacity = 1000;
  
  /**
   * Whether to enable metrics collection.
   */
  @Builder.Default
  private boolean metricsEnabled = true;
  
  /**
   * Whether to enable detailed logging.
   */
  @Builder.Default
  private boolean detailedLogging = false;
  
  /**
   * Creates a custom ThreadPoolExecutor based on configuration.
   */
  public ThreadPoolExecutor createThreadPoolExecutor() {
    return new ThreadPoolExecutor(
        corePoolSize,
        maximumPoolSize,
        keepAliveTime.toMillis(),
        TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<>(queueCapacity),
        r -> {
          Thread thread = new Thread(r, "async-util-" + System.currentTimeMillis());
          thread.setDaemon(true);
          return thread;
        },
        new ThreadPoolExecutor.CallerRunsPolicy() // Backpressure handling
    );
  }
  
  /**
   * Validates and adjusts batch size to be within acceptable limits.
   */
  public int getValidatedBatchSize(int requestedBatchSize) {
    if (requestedBatchSize <= 0) {
      return defaultBatchSize;
    }
    return Math.min(requestedBatchSize, maxBatchSize);
  }
  
  /**
   * Creates default production configuration.
   */
  public static AsyncConfig productionDefaults() {
    return AsyncConfig.builder()
        .defaultTimeout(Duration.ofMinutes(10))
        .defaultBatchSize(50)
        .maxBatchSize(500)
        .corePoolSize(Runtime.getRuntime().availableProcessors())
        .maximumPoolSize(Runtime.getRuntime().availableProcessors() * 4)
        .keepAliveTime(Duration.ofMinutes(2))
        .queueCapacity(2000)
        .metricsEnabled(true)
        .detailedLogging(false)
        .build();
  }
  
  /**
   * Creates development configuration with more verbose logging.
   */
  public static AsyncConfig developmentDefaults() {
    return AsyncConfig.builder()
        .defaultTimeout(Duration.ofMinutes(2))
        .defaultBatchSize(10)
        .maxBatchSize(100)
        .corePoolSize(2)
        .maximumPoolSize(4)
        .keepAliveTime(Duration.ofSeconds(30))
        .queueCapacity(100)
        .metricsEnabled(true)
        .detailedLogging(true)
        .build();
  }
}
