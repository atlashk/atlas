package org.atlas.framework.async;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Statistics for thread pool monitoring in production environments.
 * Provides insights into thread pool performance and resource utilization.
 */
@Getter
@Setter
@Builder
public class ThreadPoolStats {
  
  /**
   * Number of threads currently executing tasks.
   */
  @Builder.Default
  private int activeCount = 0;
  
  /**
   * Current number of threads in the pool.
   */
  @Builder.Default
  private int poolSize = 0;
  
  /**
   * Core number of threads in the pool.
   */
  @Builder.Default
  private int corePoolSize = 0;
  
  /**
   * Maximum allowed number of threads in the pool.
   */
  @Builder.Default
  private int maximumPoolSize = 0;
  
  /**
   * Number of tasks in the queue waiting to be executed.
   */
  @Builder.Default
  private int queueSize = 0;
  
  /**
   * Total number of tasks that have completed execution.
   */
  @Builder.Default
  private long completedTaskCount = 0;
  
  /**
   * Calculates the utilization percentage of the thread pool.
   * @return utilization percentage (0-100)
   */
  public double getUtilizationPercentage() {
    if (poolSize == 0) {
      return 0.0;
    }
    return (double) activeCount / poolSize * 100.0;
  }
  
  /**
   * Checks if the thread pool is under high load.
   * @return true if utilization is above 80%
   */
  public boolean isHighLoad() {
    return getUtilizationPercentage() > 80.0;
  }
  
  /**
   * Checks if the queue is backing up with tasks.
   * @return true if queue size is significant relative to pool size
   */
  public boolean isQueueBackingUp() {
    return queueSize > poolSize * 2;
  }
}