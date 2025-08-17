package org.atlas.infrastructure.api.server.rest.core.metrics;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.observability.metrics.ApiLatencyMetricsCollector;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class ApiLatencyFilter extends OncePerRequestFilter {

  private static final Pattern FILTERED_PATHS = Pattern.compile("^/api(?!/actuator).*");

  private final ApplicationConfigPort applicationConfigPort;
  private final @Nullable ApiLatencyMetricsCollector apiLatencyMetricsCollector;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    long start = System.currentTimeMillis();
    try {
      filterChain.doFilter(request, response);
    } finally {
      long elapsedTimeMs = System.currentTimeMillis() - start;
      String service = applicationConfigPort.getApplicationName();
      String endpoint = request.getRequestURI();
      String method = request.getMethod();
      int httpStatus = response.getStatus();
      String channel = Optional.ofNullable(request.getHeader("X-Channel")).orElse("unknown");
      try {
        apiLatencyMetricsCollector.collect(service, endpoint, method, httpStatus, channel, elapsedTimeMs);
        log.debug("Collected API Latency metrics: service={}, endpoint={}, method={}, httpStatus={}, channel={}, elapsedTimeMs={}",
            service, endpoint, method, httpStatus, channel, elapsedTimeMs);
      } catch (Exception e) {
        log.error("Failed to collect API latency metrics: service={}, endpoint={}, method={}, httpStatus={}, channel={}, elapsedTimeMs={}",
            service, endpoint, method, httpStatus, channel, elapsedTimeMs, e);
      }
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    return apiLatencyMetricsCollector == null ||
        !FILTERED_PATHS.matcher(request.getRequestURI()).matches();
  }
}
