package org.atlas.infrastructure.schedule.quartz.adapter.outbox.job;

import lombok.RequiredArgsConstructor;
import org.atlas.framework.messaging.outbox.RelayOutboxMessageTask;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class RelayOutboxMessageJob extends QuartzJobBean {

  private final RelayOutboxMessageTask task;

  @Override
  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    try {
      task.execute();
    } catch (Exception e) {
      throw new JobExecutionException(e);
    }
  }
}
