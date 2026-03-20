package org.atlas.libs.framework.observability.tracing;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TracingService {

  private final Tracer tracer;

  public @Nullable String getCurrentTraceId() {
    TraceContext context = this.tracer.currentTraceContext().context();
    return context != null ? context.traceId() : null;
  }

  public @Nullable String getCurrentSpanId() {
    TraceContext context = this.tracer.currentTraceContext().context();
    return context != null ? context.spanId() : null;
  }

  public void joinSpan(String traceId, String parentSpanId, String childSpanName, Runnable task) {
    TraceContext parentContext = tracer.traceContextBuilder()
        .traceId(traceId)
        .spanId(parentSpanId)
        .sampled(true)
        .build();
    Span newSpan = tracer.spanBuilder()
        .setParent(parentContext)
        .name(childSpanName)
        .start();
    try (Tracer.SpanInScope scope = tracer.withSpan(newSpan)) {
      task.run();
    } finally {
      newSpan.end();
    }
  }
}
