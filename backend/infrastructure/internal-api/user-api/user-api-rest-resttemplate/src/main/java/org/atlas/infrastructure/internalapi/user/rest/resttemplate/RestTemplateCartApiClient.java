package org.atlas.infrastructure.internalapi.user.rest.resttemplate;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.internalapi.user.CartApiClient;
import org.atlas.framework.internalapi.user.model.CartResponse;
import org.atlas.framework.internalapi.user.model.GetCartRequest;
import org.atlas.infrastructure.api.client.rest.resttemplate.RestTemplateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Retry(name = "default")
@CircuitBreaker(name = "default")
@Bulkhead(name = "default")
@RequiredArgsConstructor
public class RestTemplateCartApiClient implements CartApiClient {

  private final RestTemplateService service;
  @Value("${app.api-client.rest.user-service.base-url:http://localhost:8081}")
  private String baseUrl;

  @Override
  @SuppressWarnings("unchecked")
  public CartResponse call(GetCartRequest request) {
    String url = String.format("%s/api/internal/carts/%s", baseUrl, request.getUserId());
    ApiResponseWrapper<CartResponse> apiResponseWrapper =
        service.doGet(
            url,
            Map.of("userId", String.valueOf(request.getUserId())),
            null,
            ApiResponseWrapper.class
        );
    return apiResponseWrapper.getData();
  }
}
