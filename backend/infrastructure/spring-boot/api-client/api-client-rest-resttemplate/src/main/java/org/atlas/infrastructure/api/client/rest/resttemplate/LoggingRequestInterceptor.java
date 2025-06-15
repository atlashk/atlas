package org.atlas.infrastructure.api.client.rest.resttemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

@Slf4j
public class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {

  private static final int MAX_PAYLOAD_SIZE = 1024 * 1024; // 1 MB in bytes

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body,
      ClientHttpRequestExecution execution) throws IOException {
    final String requestId = requestId();
    logRequest(requestId, request, body);
    ClientHttpResponse response = execution.execute(request, body);
    logResponse(requestId, response);
    return response;
  }

  private String requestId() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  private void logRequest(String requestId, HttpRequest request, byte[] body) {
    if (body.length > 0) {
      log.info("[{}] Request: Method = {}, URL = {}, Headers = {}, Request body = {}",
          requestId,
          request.getMethod(),
          request.getURI(),
          request.getHeaders(),
          new String(body, StandardCharsets.UTF_8));
    } else {
      log.info("[{}] Request: Method = {}, URL = {}, Headers = {}",
          requestId,
          request.getMethod(),
          request.getURI(),
          request.getHeaders());
    }
  }

  private void logResponse(String requestId, ClientHttpResponse response) throws IOException {
    String responseBody = readAndLimitResponseBody(response);
    log.info("[{}] ApiResponseWrapper: HTTP status code = {}, ApiResponseWrapper body = {}",
        requestId,
        response.getStatusCode(),
        responseBody);
  }

  private String readAndLimitResponseBody(ClientHttpResponse response) throws IOException {
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
      StringBuilder responseBodyBuilder = new StringBuilder();
      int totalBytesRead = 0;
      String line;
      while ((line = reader.readLine()) != null) {
        responseBodyBuilder.append(line);
        totalBytesRead += line.getBytes(StandardCharsets.UTF_8).length;
        if (totalBytesRead >= MAX_PAYLOAD_SIZE) {
          responseBodyBuilder.append(" ... (truncated)");
          break; // Stop reading further when reaching the maximum allowed size
        }
      }
      return responseBodyBuilder.toString();
    }
  }
}

