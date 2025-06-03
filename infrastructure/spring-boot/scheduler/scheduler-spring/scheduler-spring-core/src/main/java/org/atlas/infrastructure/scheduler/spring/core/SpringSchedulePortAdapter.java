package org.atlas.infrastructure.scheduler.spring.core;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.scheduler.SchedulerPort;
import org.atlas.framework.util.UUIDGenerator;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpringSchedulePortAdapter implements SchedulerPort {

  private final TaskScheduler taskScheduler;
  private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

  @Override
  public String scheduleOnce(Runnable task, long delay) {
    String taskId = UUIDGenerator.generate();
    log.info("Scheduling one-time task with ID: {}, delay: {}ms", taskId, delay);
    try {
      ScheduledFuture<?> future = taskScheduler.schedule(task, Instant.now().plusMillis(delay));
      scheduledTasks.put(taskId, future);
      log.debug("Task {} scheduled successfully", taskId);
      return taskId;
    } catch (Exception e) {
      log.error("Failed to schedule task {} with delay {}ms", taskId, delay, e);
      throw new RuntimeException("Scheduling failed for task " + taskId, e);
    }
  }

  @Override
  public String scheduleFixedRate(Runnable task, long initialDelay, long period) {
    String taskId = UUIDGenerator.generate();
    log.info("Scheduling fixed-rate task with ID: {}, initialDelay: {}ms, period: {}ms", taskId,
        initialDelay, period);
    try {
      ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(task,
          Instant.now().plusMillis(initialDelay), Duration.ofMillis(period));
      scheduledTasks.put(taskId, future);
      log.debug("Fixed-rate task {} scheduled successfully", taskId);
      return taskId;
    } catch (Exception e) {
      log.error("Failed to schedule fixed-rate task {} with initialDelay {}ms, period {}ms", taskId,
          initialDelay, period, e);
      throw new RuntimeException("Scheduling failed for task " + taskId, e);
    }
  }

  @Override
  public String scheduleCron(Runnable task, String cronExpression) {
    log.warn("Cron scheduling attempted but not supported for task with cronExpression: {}",
        cronExpression);
    throw new UnsupportedOperationException(
        "Cron scheduling is not supported in Spring SchedulePort. Use Quartz for cron-based scheduling.");
  }

  @Override
  public boolean cancelTask(String taskId) {
    log.info("Attempting to cancel task with ID: {}", taskId);
    ScheduledFuture<?> future = scheduledTasks.remove(taskId);
    if (future != null) {
      future.cancel(false);
      log.info("Task {} canceled successfully", taskId);
      return true;
    }
    log.warn("No task found with ID: {} to cancel", taskId);
    return false;
  }
}
