package org.atlas.services.order.infrastructure.scheduler.spring;

import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.atlas.services.order.application.order.task.CancelExpiredOrderTask;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CancelExpiredOrderTaskScheduler {

  private final CancelExpiredOrderTask cancelExpiredOrderTask;

  @Scheduled(cron = "0 0/15 * * * *")
  @SchedulerLock(
      name = "CancelExpiredOrderTask",
      lockAtLeastFor = "PT50S",
      lockAtMostFor = "PT2M"
  )
  public void run() {
    cancelExpiredOrderTask.execute();
  }
}
