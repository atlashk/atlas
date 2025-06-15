package org.atlas.infrastructure.auth.client.springsecurityjwt;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.auth.client.AuthClientPort;
import org.atlas.framework.auth.client.model.CreateUserRequest;
import org.springframework.stereotype.Component;

@Component
@Retry(name = "default")
@CircuitBreaker(name = "default")
@Bulkhead(name = "default")
@RequiredArgsConstructor
public class SpringSecurityJwtAuthClientAdapter implements AuthClientPort {

  private final SpringSecurityJwtFeignClient feignClient;

  @Override
  public void createUser(CreateUserRequest request) {
    feignClient.register(request);
  }
}
