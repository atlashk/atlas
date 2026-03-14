package org.atlas.libs.scheduler.quartz.listener.job;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.scheduler.quartz.QuartzUtil;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoggingJobListener implements JobListener {

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void jobToBeExecuted(JobExecutionContext context) {
    log.info("Job {} is about to start execution at {}",
        QuartzUtil.getJobName(context),
        LocalDateTime.now());
  }

  @Override
  public void jobExecutionVetoed(JobExecutionContext context) {
  }

  @Override
  public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
    if (jobException == null) {
      log.info("Job {} completed execution at {}, next fire time {}",
          QuartzUtil.getJobName(context),
          LocalDateTime.now(),
          context.getNextFireTime());
    } else {
      log.error("Job {} failed execution at {} with exception {}",
          QuartzUtil.getJobName(context),
          LocalDateTime.now(),
          jobException.getMessage(),
          jobException);
    }
  }
}
