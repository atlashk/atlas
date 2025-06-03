package org.atlas.infrastructure.scheduler.quartz.core.listener;

import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.util.DateUtil;
import org.atlas.infrastructure.scheduler.quartz.core.QuartzUtil;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JobLoggingListener implements JobListener {

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void jobToBeExecuted(JobExecutionContext context) {
    log.info("Job {} is about to start execution at {}",
        QuartzUtil.getJobName(context),
        DateUtil.now());
  }

  @Override
  public void jobExecutionVetoed(JobExecutionContext context) {
    log.info("Job {} execution was vetoed", QuartzUtil.getJobName(context));
  }

  @Override
  public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
    if (jobException == null) {
      log.info("Job {} completed execution at {}, next fire time {}",
          QuartzUtil.getJobName(context),
          DateUtil.now(),
          context.getNextFireTime());
    } else {
      log.error("Job {} failed execution at {} with exception {}",
          QuartzUtil.getJobName(context),
          DateUtil.now(),
          jobException.getMessage(),
          jobException);
    }
  }
}
