package org.atlas.infrastructure.schedule.quartz.impl.outbox.config;

import lombok.RequiredArgsConstructor;
import org.atlas.framework.config.ApplicationConfigService;
import org.atlas.infrastructure.schedule.quartz.impl.outbox.job.RelayOutboxMessageJob;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class QuartzJobConfig {

  private final ApplicationConfigService applicationConfigService;

  @Bean
  public JobDetail relayOutboxMessageJobDetail() {
    return JobBuilder.newJob()
        .ofType(RelayOutboxMessageJob.class)
        .withIdentity(RelayOutboxMessageJob.class.getSimpleName(),
            applicationConfigService.getApplicationName())
        .storeDurably()
        .build();
  }

  @Bean
  public Trigger relayOutboxMessageTrigger(JobDetail relayOutboxMessageJobDetail) {
    return TriggerBuilder.newTrigger()
        .forJob(relayOutboxMessageJobDetail)
        .withIdentity(RelayOutboxMessageJob.class.getSimpleName(),
            applicationConfigService.getApplicationName())
        // Run every 5 seconds
        .withSchedule(CronScheduleBuilder.cronSchedule("*/5 * * * * ?")
            .withMisfireHandlingInstructionFireAndProceed()) // Handle misfires
        .build();
  }
}
