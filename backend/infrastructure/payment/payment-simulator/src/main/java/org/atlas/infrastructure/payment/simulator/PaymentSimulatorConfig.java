package org.atlas.infrastructure.payment.simulator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.util.ExecutorUtil;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class PaymentSimulatorConfig implements DisposableBean {

  private ScheduledExecutorService scheduledExecutorService;

  @Bean
  public ScheduledExecutorService scheduledExecutorService() {
    this.scheduledExecutorService = Executors.newScheduledThreadPool(5);
    log.info("Initialized ScheduledExecutorService with 5 threads for payment simulator");
    return this.scheduledExecutorService;
  }

  @Override
  public void destroy() {
    ExecutorUtil.gracefulShutdown(scheduledExecutorService);
  }
}
