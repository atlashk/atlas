package org.atlas.common.framework.observability.metrics;

public interface ApiLatencyMetricsCollector extends MetricsCollector {

  void collect(String service, String endpoint, String method, int httpStatus, String channel,
      long elapsedTimeMs);

  @Override
  default String metricName() {
    return "api.latency.ms";
  }
}
