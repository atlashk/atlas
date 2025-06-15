package org.atlas.infrastructure.internalapi.product.rest.feign;

import java.util.List;
import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
import org.atlas.framework.internalapi.product.model.ListProductRequest;
import org.atlas.framework.internalapi.product.model.ProductResponse;
import org.atlas.infrastructure.api.client.rest.feign.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "product-service",
    url = "${app.api-client.rest.product-service.base-url:http://localhost:8082}",
    configuration = FeignConfig.class
)
public interface ProductFeignClient {

  @PostMapping("/api/internal/products/list")
  ApiResponseWrapper<List<ProductResponse>> listProduct(@RequestBody ListProductRequest request);
}
