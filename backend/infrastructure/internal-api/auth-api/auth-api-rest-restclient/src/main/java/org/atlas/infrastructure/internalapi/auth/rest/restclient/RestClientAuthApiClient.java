package org.atlas.infrastructure.internalapi.auth.rest.restclient;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.internalapi.auth.AuthApiClient;
import org.atlas.framework.internalapi.auth.model.CreateUserRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Retry(name = "default")
@CircuitBreaker(name = "default")
@Bulkhead(name = "default")
@RequiredArgsConstructor
public class RestClientAuthApiClient implements AuthApiClient {

  private final RestClient restClient;

  @Value("${app.api-client.rest.auth-server.base-url:http://localhost:8091}")
  private String baseUrl;

  @Override
  public void createUser(CreateUserRequest request) {
    String url = String.format("%s/api/internal/users", baseUrl);
    restClient.post()
        .uri(url)
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve()
        .toBodilessEntity();
  }
}
