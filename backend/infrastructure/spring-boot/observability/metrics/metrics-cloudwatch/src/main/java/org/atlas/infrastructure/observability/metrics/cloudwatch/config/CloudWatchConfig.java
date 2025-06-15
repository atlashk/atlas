package org.atlas.infrastructure.observability.metrics.cloudwatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

@Configuration
public class CloudWatchConfig {

  @Bean
  public CloudWatchClient cloudWatchClient() {
    return CloudWatchClient.builder()
        .build();
  }

  @Bean
  public CloudWatchAsyncClient cloudWatchAsyncClient() {
    return CloudWatchAsyncClient.builder()
        .build();
  }
}
