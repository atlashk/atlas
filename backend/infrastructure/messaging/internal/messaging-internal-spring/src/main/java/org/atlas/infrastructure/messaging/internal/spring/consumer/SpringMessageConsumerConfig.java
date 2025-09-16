package org.atlas.infrastructure.messaging.internal.spring.consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class SpringMessageConsumerConfig {

  @Bean
  public TaskExecutor springMessageConsumerTaskExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setCorePoolSize(0); // No core threads
    taskExecutor.setMaxPoolSize(Integer.MAX_VALUE); // Maximum pool size
    taskExecutor.setQueueCapacity(0); // Use a SynchronousQueue-like behavior
    taskExecutor.setKeepAliveSeconds(60); // Threads will be terminated after 60 seconds of inactivity
    taskExecutor.setAllowCoreThreadTimeOut(true); // Allow core threads to time out
    taskExecutor.setThreadNamePrefix("SpringMessageConsumerTaskExecutor-");
    taskExecutor.initialize();
    return taskExecutor;
  }
}
