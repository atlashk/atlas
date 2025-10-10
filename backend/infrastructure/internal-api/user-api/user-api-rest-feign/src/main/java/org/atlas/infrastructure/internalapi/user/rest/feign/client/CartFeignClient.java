package org.atlas.infrastructure.internalapi.user.rest.feign.client;

import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.internalapi.user.model.CartResponse;
import org.atlas.infrastructure.api.client.rest.feign.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "user-service",
    url = "${app.api-client.rest.user-service.base-url:http://localhost:8081}",
    configuration = FeignConfig.class
)
public interface CartFeignClient {

  @GetMapping("/api/internal/carts")
  ApiResponseWrapper<CartResponse> getCart(@RequestParam("userId") Integer userId);
}
