package org.atlas.services.order.infrastructure.scheduler.quartz.config;

import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.config.ApplicationConfigService;
import org.atlas.services.order.infrastructure.scheduler.quartz.job.CancelExpiredOrderJob;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OrderQuartzJobConfig {

  private final ApplicationConfigService applicationConfigService;

  @Bean
  public JobDetail cancelExpiredOrderJobDetail() {
    return JobBuilder.newJob()
        .ofType(CancelExpiredOrderJob.class)
        .withIdentity(CancelExpiredOrderJob.class.getSimpleName(),
            applicationConfigService.getApplicationName())
        .storeDurably()
        .build();
  }

  @Bean
  public Trigger cancelExpiredOrderTrigger(JobDetail cancelExpiredOrderJobDetail) {
    return TriggerBuilder.newTrigger()
        .forJob(cancelExpiredOrderJobDetail)
        .withIdentity(CancelExpiredOrderJob.class.getSimpleName(),
            applicationConfigService.getApplicationName())
        .withSchedule(CronScheduleBuilder.cronSchedule("0 * * * * ?")
            .withMisfireHandlingInstructionFireAndProceed())
        .build();
  }
}
