package org.atlas.libs.observability.metrics.prometheus;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.observability.metrics.ApiLatencyMetricsCollector;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PrometheusApiLatencyMetricsCollector implements ApiLatencyMetricsCollector {

  private final MeterRegistry meterRegistry;

  @Override
  public void collect(String service, String endpoint, String method, int httpStatus,
      long elapsedTimeMs) {
    Tags tags = Tags.of(
        "service", service,
        "endpoint", endpoint,
        "method", method,
        "http_status", String.valueOf(httpStatus)
    );
    meterRegistry.timer(metricName(), tags)
        .record(elapsedTimeMs, TimeUnit.MILLISECONDS);
  }
}
