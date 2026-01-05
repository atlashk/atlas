package org.atlas.common.infrastructure.observability.tracing.zipkin;

import io.micrometer.context.ContextSnapshotFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TracingConfig {

  @Bean
  public ContextSnapshotFactory contextSnapshotFactory() {
    return ContextSnapshotFactory.builder().build();
  }
}
