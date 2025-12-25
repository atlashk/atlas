package org.atlas.infrastructure.internalapi.user.rest.restclient;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.internalapi.user.CartApiClient;
import org.atlas.framework.internalapi.user.model.CartResponse;
import org.atlas.framework.internalapi.user.model.GetCartRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Retry(name = "default")
@CircuitBreaker(name = "default")
@Bulkhead(name = "default")
@RequiredArgsConstructor
public class RestClientCartApiClient implements CartApiClient {

  private final RestClient restClient;
  @Value("${app.api-client.rest.user-service.base-url:http://localhost:8081}")
  private String baseUrl;

  @Override
  public CartResponse call(GetCartRequest request) {
    String url = String.format("%s/api/internal/carts?userId=%d", baseUrl, request.getUserId());
    ApiResponseWrapper<CartResponse> apiResponseWrapper = restClient.get()
        .uri(url)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .toEntity(new ParameterizedTypeReference<ApiResponseWrapper<CartResponse>>() {
        })
        .getBody();
    assert apiResponseWrapper != null;
    return apiResponseWrapper.getData();
  }
}
