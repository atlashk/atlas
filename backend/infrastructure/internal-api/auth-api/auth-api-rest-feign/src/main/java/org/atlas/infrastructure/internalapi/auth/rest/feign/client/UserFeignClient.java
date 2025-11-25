package org.atlas.infrastructure.internalapi.auth.rest.feign.client;

import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.internalapi.auth.model.CreateUserRequest;
import org.atlas.infrastructure.api.client.rest.feign.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "auth-server",
    url = "${app.api-client.rest.auth-server.base-url:http://localhost:8091}",
    configuration = FeignConfig.class
)
public interface UserFeignClient {

  @PostMapping("/api/internal/users")
  ApiResponseWrapper<Void> createUser(@RequestBody CreateUserRequest request);
}
