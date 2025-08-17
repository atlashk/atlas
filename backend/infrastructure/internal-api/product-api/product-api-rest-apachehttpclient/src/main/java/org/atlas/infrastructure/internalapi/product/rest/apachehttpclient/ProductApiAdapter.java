package org.atlas.infrastructure.internalapi.product.rest.apachehttpclient;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
import org.atlas.framework.internalapi.product.ProductApiPort;
import org.atlas.framework.internalapi.product.model.ListProductRequest;
import org.atlas.framework.internalapi.product.model.ProductResponse;
import org.atlas.infrastructure.api.client.rest.apachehttpclient.HttpClientService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Retry(name = "default")
@CircuitBreaker(name = "default")
@Bulkhead(name = "default")
@RequiredArgsConstructor
public class ProductApiAdapter implements ProductApiPort {

  @Value("${app.api-client.rest.product-service.base-url:http://localhost:8082}")
  private String baseUrl;

  private final HttpClientService service;

  @Override
  @SuppressWarnings("unchecked")
  public List<ProductResponse> call(ListProductRequest request) {
    String url = String.format("%s/api/internal/products/list", baseUrl);
    ApiResponseWrapper<List<ProductResponse>> apiResponseWrapper =
        service.doPost(url, null, request, ApiResponseWrapper.class);
    return apiResponseWrapper.getData();
  }
}
