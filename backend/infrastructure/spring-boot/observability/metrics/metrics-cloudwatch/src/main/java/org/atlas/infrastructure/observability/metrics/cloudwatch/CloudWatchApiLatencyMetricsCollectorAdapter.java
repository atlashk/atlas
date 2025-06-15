package org.atlas.infrastructure.observability.metrics.cloudwatch;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.observability.metrics.ApiLatencyMetricsCollector;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;

@Component
@RequiredArgsConstructor
public class CloudWatchApiLatencyMetricsCollectorAdapter implements ApiLatencyMetricsCollector {

  private final CloudWatchClient cloudWatchClient;

  @Override
  public void collect(String service, String endpoint, String method, int httpStatus, String channel,
      long elapsedTimeMs) {
    MetricDatum datum = MetricDatum.builder()
        .metricName(metricName())
        .unit(StandardUnit.MILLISECONDS)
        .value((double) elapsedTimeMs)
        .dimensions(
            Dimension.builder().name("endpoint").value(endpoint).build(),
            Dimension.builder().name("method").value(method).build(),
            Dimension.builder().name("http_status").value(String.valueOf(httpStatus)).build(),
            Dimension.builder().name("channel").value(channel).build()
        )
        .timestamp(Instant.now())
        .build();

    PutMetricDataRequest request = PutMetricDataRequest.builder()
        .namespace(service)
        .metricData(datum)
        .build();

    cloudWatchClient.putMetricData(request);
  }
}
