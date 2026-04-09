package org.atlas.libs.api.server.rest.observability.tracing;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.observability.tracing.TracingService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
class TraceIdFilter extends OncePerRequestFilter {

  private final TracingService tracingService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String traceId = tracingService.getCurrentTraceId();
    if (traceId != null) {
      response.setHeader("X-Trace-Id", traceId);
    }
    filterChain.doFilter(request, response);
  }
}
