package org.atlas.libs.observability.opentelemetry.metrics;

import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.jvm.convention.otel.OpenTelemetryJvmClassLoadingMeterConventions;
import io.micrometer.core.instrument.binder.jvm.convention.otel.OpenTelemetryJvmCpuMeterConventions;
import io.micrometer.core.instrument.binder.jvm.convention.otel.OpenTelemetryJvmMemoryMeterConventions;
import io.micrometer.core.instrument.binder.jvm.convention.otel.OpenTelemetryJvmThreadMeterConventions;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenTelemetryMetricsConfig {

  @Bean
  public OpenTelemetryJvmCpuMeterConventions openTelemetryJvmCpuMeterConventions() {
    return new OpenTelemetryJvmCpuMeterConventions(Tags.empty());
  }

  @Bean
  public ProcessorMetrics processorMetrics() {
    return new ProcessorMetrics(
        List.of(),
        new OpenTelemetryJvmCpuMeterConventions(Tags.empty())
    );
  }

  @Bean
  public JvmMemoryMetrics jvmMemoryMetrics() {
    return new JvmMemoryMetrics(
        List.of(),
        new OpenTelemetryJvmMemoryMeterConventions(Tags.empty())
    );
  }

  @Bean
  public JvmThreadMetrics jvmThreadMetrics() {
    return new JvmThreadMetrics(
        List.of(),
        new OpenTelemetryJvmThreadMeterConventions(Tags.empty())
    );
  }

  @Bean
  public ClassLoaderMetrics classLoaderMetrics() {
    return new ClassLoaderMetrics(
        new OpenTelemetryJvmClassLoadingMeterConventions()
    );
  }
}