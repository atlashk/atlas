package org.atlas.infrastructure.auth.client.springsecurityjwt;

import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
import org.atlas.framework.auth.client.model.CreateUserRequest;
import org.atlas.infrastructure.api.client.rest.feign.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "spring-security-jwt",
    url = "${app.auth-client.spring-security-jwt.base-url:http://localhost:8091}",
    configuration = FeignConfig.class
)
public interface SpringSecurityJwtFeignClient {

  @PostMapping("/api/auth/register")
  ApiResponseWrapper<Void> register(@RequestBody CreateUserRequest request);
}
