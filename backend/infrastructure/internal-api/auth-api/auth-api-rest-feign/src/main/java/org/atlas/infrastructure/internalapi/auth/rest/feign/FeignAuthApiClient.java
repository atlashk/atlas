package org.atlas.infrastructure.internalapi.auth.rest.feign;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.internalapi.auth.AuthApiClient;
import org.atlas.framework.internalapi.auth.model.CreateUserRequest;
import org.atlas.infrastructure.internalapi.auth.rest.feign.client.UserFeignClient;
import org.springframework.stereotype.Component;

@Component
@Retry(name = "default")
@CircuitBreaker(name = "default")
@Bulkhead(name = "default")
@RequiredArgsConstructor
public class FeignAuthApiClient implements AuthApiClient {

  private final UserFeignClient feignClient;

  @Override
  public void createUser(CreateUserRequest request) {
    feignClient.createUser(request);
  }
}
