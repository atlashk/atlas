package org.atlas.infrastructure.messaging.sns.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@Slf4j
public class SqsConfig implements DisposableBean {

  private SqsClient sqsClient;

  @Bean
  public SqsClient sqsClient() {
    this.sqsClient = SqsClient.builder()
        .build();
    log.info("Initialized SQS client");
    return this.sqsClient;
  }

  @Override
  public void destroy() {
    if (this.sqsClient != null) {
      this.sqsClient.close();
      log.info("Closed SQS client");
    }
  }
}
