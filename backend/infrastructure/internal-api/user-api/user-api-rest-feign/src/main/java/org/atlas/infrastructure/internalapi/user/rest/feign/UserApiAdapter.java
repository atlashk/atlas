package org.atlas.infrastructure.internalapi.user.rest.feign;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
import org.atlas.framework.internalapi.user.UserApiPort;
import org.atlas.framework.internalapi.user.model.ListUserRequest;
import org.atlas.framework.internalapi.user.model.UserResponse;
import org.springframework.stereotype.Component;

@Component
@Retry(name = "default")
@CircuitBreaker(name = "default")
@Bulkhead(name = "default")
@RequiredArgsConstructor
public class UserApiAdapter implements UserApiPort {

  private final UserFeignClient feignClient;

  @Override
  public List<UserResponse> call(ListUserRequest request) {
    ApiResponseWrapper<List<UserResponse>> apiResponseWrapper = feignClient.listUser(request);
    return apiResponseWrapper.getData();
  }
}
