package org.atlas.infrastructure.internalapi.product.rest.feign;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.internalapi.product.ProductApiClient;
import org.atlas.framework.internalapi.product.model.ListProductRequest;
import org.atlas.framework.internalapi.product.model.ProductResponse;
import org.atlas.infrastructure.internalapi.auth.rest.feign.client.ProductFeignClient;
import org.springframework.stereotype.Component;

@Component
@Retry(name = "default")
@CircuitBreaker(name = "default")
@Bulkhead(name = "default")
@RequiredArgsConstructor
public class FeignProductApiClient implements ProductApiClient {

  private final ProductFeignClient feignClient;

  @Override
  public List<ProductResponse> call(ListProductRequest request) {
    ApiResponseWrapper<List<ProductResponse>> apiResponseWrapper = feignClient.listProduct(request);
    return apiResponseWrapper.getData();
  }
}
