package org.atlas.framework.scheduler;

public interface SchedulerPort {

  /**
   * Schedule a task to run once after a specified delay.
   * @param task the task to execute
   * @param delay the delay before execution (in milliseconds)
   * @return a task ID or handle for managing the task
   */
  String scheduleOnce(Runnable task, long delay) throws Exception;

  /**
   * Schedule a task to run at a fixed rate, regardless of task execution time.
   * @param task the task to execute
   * @param initialDelay the initial delay before first execution (in milliseconds)
   * @param period the period between the start of executions (in milliseconds)
   * @return a task ID or handle for managing the task
   */
  String scheduleFixedRate(Runnable task, long initialDelay, long period) throws Exception;

  /**
   * Schedule a task using a cron expression.
   * @param task the task to execute
   * @param cronExpression the cron expression (e.g., "0 0 12 * * ?")
   * @return a task ID or handle for managing the task
   */
  String scheduleCron(Runnable task, String cronExpression) throws Exception;

  /**
   * Cancel a scheduled task by its ID.
   * @param taskId the ID of the task to cancel
   * @return true if the task was canceled, false otherwise
   */
  boolean cancelTask(String taskId) throws Exception;
}
