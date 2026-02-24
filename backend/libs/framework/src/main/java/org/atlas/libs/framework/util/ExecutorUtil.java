package org.atlas.libs.framework.util;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class ExecutorUtil {

  /**
   * Gracefully shuts down an ExecutorService with a default timeout of 30 seconds.
   *
   * @param executorService the executor service to shut down
   */
  public static void gracefulShutdown(ExecutorService executorService) {
    gracefulShutdown(executorService, 30, TimeUnit.SECONDS);
  }

  /**
   * Gracefully shuts down an ExecutorService with a configurable timeout.
   *
   * @param executorService the executor service to shut down
   * @param timeout         the maximum time to wait for shutdown
   * @param timeUnit        the time unit of the timeout argument
   */
  public static void gracefulShutdown(ExecutorService executorService, long timeout,
      TimeUnit timeUnit) {
    if (executorService == null) {
      log.warn("ExecutorService is null, skipping shutdown");
      return;
    }

    if (executorService.isShutdown()) {
      log.debug("ExecutorService is already shutdown");
      return;
    }

    log.debug("Initiating graceful shutdown of ExecutorService");

    // Initiates an orderly shutdown of the executor service.
    // It prevents new tasks from being submitted, but allows previously submitted tasks to finish.
    executorService.shutdown();

    try {
      // Waits for the tasks to complete, with the specified timeout.
      // If the tasks do not finish in the given time, it returns false.
      if (!executorService.awaitTermination(timeout, timeUnit)) {
        log.warn("ExecutorService did not terminate within {} {}, forcing shutdown", timeout,
            timeUnit);
        // If tasks did not finish within the timeout, force a shutdown immediately.
        // It attempts to stop all running tasks and halts the executor service.
        executorService.shutdownNow();

        // Wait a bit more for tasks to respond to being cancelled
        if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
          log.error("ExecutorService did not terminate even after forced shutdown");
        }
      } else {
        log.debug("ExecutorService shutdown completed successfully");
      }
    } catch (InterruptedException e) {
      // If the current thread is interrupted while waiting for tasks to finish,
      // it catches the InterruptedException, restores the interrupt status,
      // and forces the shutdown of the executor service.
      log.warn("Thread interrupted during ExecutorService shutdown, forcing immediate shutdown");
      Thread.currentThread().interrupt();
      executorService.shutdownNow();
    }
  }

  /**
   * Gets current thread pool statistics for monitoring.
   */
  public static ThreadPoolStats getThreadPoolStats(Executor executor) {
    if (executor instanceof ThreadPoolExecutor tpe) {
      return ThreadPoolStats.builder()
          .activeCount(tpe.getActiveCount())
          .poolSize(tpe.getPoolSize())
          .corePoolSize(tpe.getCorePoolSize())
          .maximumPoolSize(tpe.getMaximumPoolSize())
          .queueSize(tpe.getQueue().size())
          .completedTaskCount(tpe.getCompletedTaskCount())
          .build();
    } else if (executor instanceof ForkJoinPool fjp) {
      return ThreadPoolStats.builder()
          .activeCount(fjp.getActiveThreadCount())
          .poolSize(fjp.getPoolSize())
          .queueSize(fjp.getQueuedSubmissionCount())
          .build();
    }
    return ThreadPoolStats.builder().build();
  }

  /**
   * Statistics for thread pool monitoring in production environments. Provides insights into thread
   * pool performance and resource utilization.
   */
  @Getter
  @Setter
  @Builder
  public static class ThreadPoolStats {

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
     *
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
     *
     * @return true if utilization is above 80%
     */
    public boolean isHighLoad() {
      return getUtilizationPercentage() > 80.0;
    }

    /**
     * Checks if the queue is backing up with tasks.
     *
     * @return true if queue size is significant relative to pool size
     */
    public boolean isQueueBackingUp() {
      return queueSize > poolSize * 2;
    }
  }
}
