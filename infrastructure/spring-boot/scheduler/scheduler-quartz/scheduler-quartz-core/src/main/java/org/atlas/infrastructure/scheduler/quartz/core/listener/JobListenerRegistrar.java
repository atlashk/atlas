package org.atlas.infrastructure.scheduler.quartz.core.listener;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.quartz.JobListener;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
@RequiredArgsConstructor
public class JobListenerRegistrar implements InitializingBean {

  private final SchedulerFactoryBean schedulerFactoryBean;
  private final List<JobListener> jobListeners;

  @Override
  public void afterPropertiesSet() throws Exception {
    if (CollectionUtils.isNotEmpty(jobListeners)) {
      for (JobListener jobListener : jobListeners) {
        schedulerFactoryBean.getScheduler()
            .getListenerManager()
            .addJobListener(jobListener);
      }
    }
  }
}
