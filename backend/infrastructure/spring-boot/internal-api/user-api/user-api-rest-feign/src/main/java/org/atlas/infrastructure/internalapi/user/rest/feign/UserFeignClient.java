package org.atlas.infrastructure.internalapi.user.rest.feign;

import java.util.List;
import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
import org.atlas.framework.internalapi.user.model.ListUserRequest;
import org.atlas.framework.internalapi.user.model.UserResponse;
import org.atlas.infrastructure.api.client.rest.feign.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "user-service",
    url = "${app.api-client.rest.user-service.base-url:http://localhost:8081}",
    configuration = FeignConfig.class
)
public interface UserFeignClient {

  @PostMapping("/api/internal/users/list")
  ApiResponseWrapper<List<UserResponse>> listUser(@RequestBody ListUserRequest input);
}
