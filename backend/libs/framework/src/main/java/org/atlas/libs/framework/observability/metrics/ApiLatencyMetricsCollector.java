package org.atlas.libs.framework.observability.metrics;

public interface ApiLatencyMetricsCollector extends MetricsCollector {

  void collect(String service, String endpoint, String method, int httpStatus, long elapsedTimeMs);

  @Override
  default String metricName() {
    return "api_latency_ms";
  }
}
