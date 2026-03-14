package org.atlas.services.order.infrastructure.scheduler.quartz.job;

import lombok.RequiredArgsConstructor;
import org.atlas.services.order.application.order.task.CancelExpiredOrderTask;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class CancelExpiredOrderJob extends QuartzJobBean {

  private final CancelExpiredOrderTask task;

  @Override
  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    try {
      task.execute();
    } catch (Exception e) {
      throw new JobExecutionException(e);
    }
  }
}
