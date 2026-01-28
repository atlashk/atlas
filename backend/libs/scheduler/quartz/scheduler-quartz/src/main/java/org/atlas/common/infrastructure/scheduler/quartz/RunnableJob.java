package org.atlas.common.infrastructure.scheduler.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

public class RunnableJob implements Job {

  @Override
  public void execute(JobExecutionContext context) {
    Runnable runnable = (Runnable) context.getJobDetail()
        .getJobDataMap()
        .get(QuartzConstant.RUNNABLE_DATA_KEY);
    if (runnable != null) {
      runnable.run();
    }
  }
}
