package org.atlas.common.infrastructure.scheduler.quartz.listener;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.common.framework.collection.CollectionUtil;
import org.quartz.JobListener;
import org.quartz.ListenerManager;
import org.quartz.TriggerListener;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class QuartzListenerRegistrar implements InitializingBean {

  private final SchedulerFactoryBean schedulerFactoryBean;
  private final List<JobListener> jobListeners;
  private final List<TriggerListener> triggerListeners;

  @Override
  public void afterPropertiesSet() throws Exception {
    ListenerManager listenerManager = schedulerFactoryBean.getScheduler()
        .getListenerManager();

    if (CollectionUtil.isNotEmpty(jobListeners)) {
      for (JobListener jobListener : jobListeners) {
        listenerManager.addJobListener(jobListener);
        log.info("Registered job listener: {}", jobListener.getName());
      }
    }

    if (CollectionUtil.isNotEmpty(triggerListeners)) {
      for (TriggerListener triggerListener : triggerListeners) {
        listenerManager.addTriggerListener(triggerListener);
        log.info("Registered trigger listener: {}", triggerListener.getName());
      }
    }
  }
}
