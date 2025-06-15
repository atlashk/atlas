package org.atlas.infrastructure.api.server.rest.core.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.atlas.framework.util.StringUtil;
import org.atlas.infrastructure.api.server.rest.core.util.IpAddressUtil;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(0)
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

  private static final Pattern FILTERED_PATHS = Pattern.compile("^/api(/.*)?$");
  private static final int MAX_PAYLOAD_LENGTH = 1024 * 1024; // 1 MB in bytes

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    
    // Check if this is a multipart request
    String contentType = request.getContentType();
    boolean isMultipartRequest = contentType != null && contentType.toLowerCase().startsWith("multipart/");
    
    if (isMultipartRequest) {
      // For multipart requests, log without caching the body to avoid interfering with Spring's multipart resolver
      logRequestWithoutBody(request);
      filterChain.doFilter(request, response);
    } else {
      // For non-multipart requests, use the existing caching mechanism
      CachedHttpServletRequest cachedRequest = new CachedHttpServletRequest(request);
      logRequest(cachedRequest, MAX_PAYLOAD_LENGTH);
      filterChain.doFilter(cachedRequest, response);
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    return !FILTERED_PATHS.matcher(request.getRequestURI()).matches();
  }

  public void logRequest(CachedHttpServletRequest request, int maxPayloadLength) {
    String ipAddress = IpAddressUtil.getIpAddress(request);
    String method = request.getMethod();
    String fullUrl = getFullUrl(request);
    Map<String, String> headers = getHeaders(request);
    String body = getBody(request, maxPayloadLength);
    String parameters = getParameters(request);
    log.info(
        "Incoming request:\nIP address: {}\nMethod = {}\nURL = {}\nHeaders = {}\nBody = {}\nParameters = {}",
        ipAddress, method, fullUrl, headers, body, parameters);
  }

  private void logRequestWithoutBody(HttpServletRequest request) {
    String ipAddress = IpAddressUtil.getIpAddress(request);
    String method = request.getMethod();
    String fullUrl = getFullUrl(request);
    Map<String, String> headers = getHeaders(request);
    String parameters = getParameters(request);
    log.info(
        "Incoming request (multipart):\nIP address: {}\nMethod = {}\nURL = {}\nHeaders = {}\nBody = [MULTIPART DATA - NOT LOGGED]\nParameters = {}",
        ipAddress, method, fullUrl, headers, parameters);
  }

  private String getFullUrl(HttpServletRequest request) {
    StringBuffer requestUrl = request.getRequestURL();
    String queryString = request.getQueryString();
    if (queryString != null) {
      requestUrl.append("?").append(queryString);
    }
    return requestUrl.toString();
  }

  private Map<String, String> getHeaders(HttpServletRequest request) {
    Map<String, String> headers = new HashMap<>();
    Collections.list(request.getHeaderNames())
        .forEach(headerName -> headers.put(headerName, request.getHeader(headerName)));
    return headers;
  }

  private String getBody(HttpServletRequest request, int maxPayloadLength) {
    try {
      String body = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
      return StringUtil.limit(body, maxPayloadLength);
    } catch (IOException e) {
      log.error("Failed to cache request body", e);
      return StringUtil.EMPTY;
    }
  }

  private String getParameters(HttpServletRequest request) {
    Map<String, String[]> parameterMap = request.getParameterMap();
    if (parameterMap == null) {
      return "";
    }
    StringJoiner joiner = new StringJoiner("&");
    for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
      String key = entry.getKey();
      String[] values = entry.getValue();
      String combinedValues = Stream.of(values)
          .map(this::urlEncode)
          .collect(Collectors.joining(","));
      joiner.add(urlEncode(key) + "=" + combinedValues);
    }
    return joiner.toString();
  }

  // Helper method to handle URL encoding. E.g. `http://example.com/profile?name=John Doe & Sons` needs encoding to
  // `http://example.com/profile?name=John%20Doe%20%26%20Sons`
  private String urlEncode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
