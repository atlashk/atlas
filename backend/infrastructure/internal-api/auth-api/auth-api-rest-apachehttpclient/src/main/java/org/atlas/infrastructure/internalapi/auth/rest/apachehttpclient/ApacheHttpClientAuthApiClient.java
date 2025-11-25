package org.atlas.infrastructure.internalapi.auth.rest.apachehttpclient;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.internalapi.auth.AuthApiClient;
import org.atlas.framework.internalapi.auth.model.CreateUserRequest;
import org.atlas.infrastructure.api.client.rest.apachehttpclient.HttpClientService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Retry(name = "default")
@CircuitBreaker(name = "default")
@Bulkhead(name = "default")
@RequiredArgsConstructor
public class ApacheHttpClientAuthApiClient implements AuthApiClient {

  private final HttpClientService service;

  @Value("${app.api-client.rest.auth-server.base-url:http://localhost:8091}")
  private String baseUrl;

  @Override
  public void createUser(CreateUserRequest request) {
    String url = String.format("%s/api/internal/users", baseUrl);
    service.doPost(url, null, request, ApiResponseWrapper.class);
  }
}
