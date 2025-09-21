package org.atlas.framework.async;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Production-ready utility class for asynchronous operations with enhanced error handling,
 * configurable thread pools, timeout support, and monitoring capabilities.
 */
@Slf4j
@UtilityClass
public class AsyncUtil {

  private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(15);
  private static final int DEFAULT_BATCH_SIZE = 100;
  private static final Executor DEFAULT_EXECUTOR = ForkJoinPool.commonPool();

  // ========== Single Task Execution ==========

  /**
   * Executes a single async task with default executor.
   */
  public static void executeAsync(AsyncTask task) {
    executeAsync(task, DEFAULT_EXECUTOR);
  }

  /**
   * Executes a single async task with custom executor.
   */
  public static void executeAsync(AsyncTask task, Executor executor) {
    long startTime = System.currentTimeMillis();
    CompletableFuture.runAsync(() -> {
      try {
        task.run();
        logMetrics("single_task", 1, System.currentTimeMillis() - startTime, true);
      } catch (Exception e) {
        log.error("Error executing async task", e);
        logMetrics("single_task", 1, System.currentTimeMillis() - startTime, false);
        throw new CompletionException(e);
      }
    }, executor);
  }

  // ========== Multiple Tasks Execution ==========

  /**
   * Executes a list of AsyncTask objects using default settings.
   */
  public static CompletableFuture<Void> executeAsync(List<AsyncTask> tasks) {
    return executeAsync(tasks, DEFAULT_TIMEOUT, DEFAULT_EXECUTOR, DEFAULT_BATCH_SIZE);
  }

  /**
   * Executes a list of AsyncTask objects with full configuration options.
   */
  public static CompletableFuture<Void> executeAsync(List<AsyncTask> tasks, Duration timeout,
      Executor executor, int batchSize) {
    if (tasks == null || tasks.isEmpty()) {
      return CompletableFuture.completedFuture(null);
    }

    long startTime = System.currentTimeMillis();
    log.debug("Starting async execution for {} tasks with batch size {}", tasks.size(), batchSize);

    List<CompletableFuture<Void>> batchFutures = new ArrayList<>();

    for (int i = 0; i < tasks.size(); i += batchSize) {
      int endIndex = Math.min(i + batchSize, tasks.size());
      List<AsyncTask> batch = tasks.subList(i, endIndex);

      CompletableFuture<Void> batchFuture = processTaskBatch(batch, executor);
      batchFutures.add(batchFuture);
    }

    return CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0]))
        .orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
        .whenComplete((result, throwable) -> {
          long duration = System.currentTimeMillis() - startTime;
          if (throwable != null) {
            if (throwable instanceof TimeoutException) {
              log.error("Task execution timed out after {}ms for {} tasks", duration, tasks.size());
            } else {
              log.error("Task execution failed after {}ms for {} tasks", duration, tasks.size(),
                  throwable);
            }
            logMetrics("task_execution", tasks.size(), duration, false);
          } else {
            log.debug("Task execution completed successfully in {}ms for {} tasks", duration,
                tasks.size());
            logMetrics("task_execution", tasks.size(), duration, true);
          }
        });
  }

  // ========== Monitoring ==========

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

  private static CompletableFuture<Void> processTaskBatch(List<AsyncTask> batch,
      Executor executor) {
    List<CompletableFuture<Void>> futures = batch.stream()
        .map(task -> CompletableFuture.runAsync(() -> {
              try {
                task.run();
              } catch (Exception e) {
                log.error("Error executing async task", e);
                throw new CompletionException(e);
              }
            }, executor)
            .whenComplete((result, throwable) -> {
              if (throwable != null) {
                task.onError(throwable);
              } else {
                task.onSuccess();
              }
            }))
        .toList();

    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
  }

  private static void logMetrics(String operation, int itemCount, long durationMs,
      boolean success) {
    log.info("AsyncUtil.{} - items: {}, duration: {}ms, success: {}",
        operation, itemCount, durationMs, success);
  }
}
