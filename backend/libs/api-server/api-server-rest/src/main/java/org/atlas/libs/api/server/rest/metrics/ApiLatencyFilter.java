package org.atlas.libs.api.server.rest.metrics;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.config.ApplicationConfigService;
import org.atlas.libs.framework.measurement.StopWatch;
import org.atlas.libs.framework.observability.metrics.ApiLatencyMetricsCollector;
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

  private final ApplicationConfigService applicationConfigService;
  private final @Nullable ApiLatencyMetricsCollector apiLatencyMetricsCollector;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    try {
      filterChain.doFilter(request, response);
    } finally {
      stopWatch.stop();
      long elapsedTimeMs = stopWatch.getElapsedTimeMs();

      String service = applicationConfigService.getApplicationName();
      String endpoint = request.getRequestURI();
      String method = request.getMethod();
      int httpStatus = response.getStatus();
      try {
        apiLatencyMetricsCollector.collect(service, endpoint, method, httpStatus, elapsedTimeMs);
        log.debug(
            "Collected API Latency metrics: service={}, endpoint={}, method={}, httpStatus={}, elapsedTimeMs={}",
            service, endpoint, method, httpStatus, elapsedTimeMs);
      } catch (Exception e) {
        log.error(
            "Failed to collect API latency metrics: service={}, endpoint={}, method={}, httpStatus={}, elapsedTimeMs={}",
            service, endpoint, method, httpStatus, elapsedTimeMs, e);
      }
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    return apiLatencyMetricsCollector == null ||
        !FILTERED_PATHS.matcher(request.getRequestURI()).matches();
  }
}
