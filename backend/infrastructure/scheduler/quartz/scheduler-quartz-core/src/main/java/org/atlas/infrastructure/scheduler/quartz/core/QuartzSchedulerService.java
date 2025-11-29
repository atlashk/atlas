package org.atlas.infrastructure.scheduler.quartz.core;

import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.scheduler.SchedulerService;
import org.atlas.framework.uuid.UUIDGenerator;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;


/**
 * Quartz-based implementation of SchedulePort.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class QuartzSchedulerService implements SchedulerService, InitializingBean,
    DisposableBean {

  private final Scheduler scheduler;

  @Override
  public void afterPropertiesSet() throws Exception {
    try {
      scheduler.start();
      log.info("Quartz scheduler started successfully");
    } catch (SchedulerException e) {
      log.error("Failed to start Quartz scheduler", e);
      throw e;
    }
  }

  @Override
  public void destroy() throws Exception {
    try {
      scheduler.shutdown(true); // Wait for jobs to complete
      log.info("Quartz scheduler stopped successfully");
    } catch (SchedulerException e) {
      log.error("Failed to stop Quartz scheduler", e);
      throw e;
    }
  }

  @Override
  public String scheduleOnce(Runnable task, long delay) throws SchedulerException {
    log.info("Scheduling one-time task with delay: {}ms", delay);
    return schedule(task, buildOneTimeTrigger(delay));
  }

  @Override
  public String scheduleFixedRate(Runnable task, long initialDelay, long period)
      throws SchedulerException {
    log.info("Scheduling fixed-rate task with initialDelay: {}ms, period: {}ms", initialDelay,
        period);
    return schedule(task, buildFixedRateTrigger(initialDelay, period));
  }

  @Override
  public String scheduleCron(Runnable task, String cronExpression) throws SchedulerException {
    log.info("Scheduling cron task with expression: {}", cronExpression);
    return schedule(task, buildCronTrigger(cronExpression));
  }

  @Override
  public boolean cancelTask(String taskId) throws SchedulerException {
    JobKey jobKey = new JobKey(taskId, QuartzConstant.DEFAULT_GROUP);
    boolean deleted = scheduler.deleteJob(jobKey);
    if (deleted) {
      log.info("Task {} canceled successfully", taskId);
    } else {
      log.warn("Task {} not found in scheduler for cancellation", taskId);
    }
    return deleted;
  }

  private String schedule(Runnable task, Trigger trigger) throws SchedulerException {
    String taskId = UUIDGenerator.generate();
    JobKey jobKey = new JobKey(taskId, QuartzConstant.DEFAULT_GROUP);
    TriggerKey triggerKey = new TriggerKey(taskId, QuartzConstant.DEFAULT_GROUP);
    try {
      if (scheduler.checkExists(jobKey)) {
        log.warn("Task with ID {} already exists, rescheduling with new trigger", taskId);
        scheduler.rescheduleJob(triggerKey, trigger);
      } else {
        JobDetail job = JobBuilder.newJob(RunnableJob.class)
            .withIdentity(jobKey)
            .storeDurably()
            .build();
        job.getJobDataMap().put(QuartzConstant.RUNNABLE_DATA_KEY, task);
        scheduler.scheduleJob(job, trigger);
      }
      log.info("Scheduled task {} successfully", taskId);
      return taskId;
    } catch (SchedulerException e) {
      log.error("Failed to schedule task {}: {}", taskId, e.getMessage(), e);
      throw e;
    }
  }

  private Trigger buildOneTimeTrigger(long delay) {
    String triggerId = UUIDGenerator.generate();
    log.debug("Building one-time trigger {} with delay: {}ms", triggerId, delay);
    return TriggerBuilder.newTrigger()
        .withIdentity(triggerId, QuartzConstant.DEFAULT_GROUP)
        .startAt(new Date(System.currentTimeMillis() + delay))
        .withSchedule(SimpleScheduleBuilder.simpleSchedule()
            .withRepeatCount(0)
            .withMisfireHandlingInstructionNextWithExistingCount())
        .build();
  }

  private Trigger buildFixedRateTrigger(long initialDelay, long period) {
    String triggerId = UUIDGenerator.generate();
    log.debug("Building fixed-rate trigger {} with initialDelay: {}ms, period: {}ms", triggerId,
        initialDelay, period);
    return TriggerBuilder.newTrigger()
        .withIdentity(triggerId, QuartzConstant.DEFAULT_GROUP)
        .startAt(new Date(System.currentTimeMillis() + initialDelay))
        .withSchedule(SimpleScheduleBuilder.simpleSchedule()
            .withIntervalInMilliseconds(period)
            .repeatForever()
            .withMisfireHandlingInstructionFireNow())
        .build();
  }

  private Trigger buildCronTrigger(String cronExpression) throws SchedulerException {
    String triggerId = UUIDGenerator.generate();
    log.debug("Building cron trigger {} with expression: {}", triggerId, cronExpression);
    try {
      return TriggerBuilder.newTrigger()
          .withIdentity(triggerId, QuartzConstant.DEFAULT_GROUP)
          .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)
              .withMisfireHandlingInstructionFireAndProceed())
          .build();
    } catch (RuntimeException e) {
      log.error("Invalid cron expression: {}", cronExpression, e);
      throw new SchedulerException("Invalid cron expression: " + cronExpression, e);
    }
  }
}
