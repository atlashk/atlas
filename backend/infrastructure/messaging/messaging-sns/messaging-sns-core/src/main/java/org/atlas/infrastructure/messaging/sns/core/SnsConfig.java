package org.atlas.infrastructure.messaging.sns.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
@Slf4j
public class SnsConfig implements DisposableBean {

  private SnsClient snsClient;

  @Bean
  public SnsClient snsClient() {
    this.snsClient = SnsClient.builder()
        .build();
    log.info("Initialized SNS client");
    return this.snsClient;
  }

  @Override
  public void destroy() {
    if (this.snsClient != null) {
      this.snsClient.close();
      log.info("Closed SNS client");
    }
  }
}
