package org.atlas.infrastructure.internalapi.user.rest.resttemplate;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
import org.atlas.framework.internalapi.user.UserApiPort;
import org.atlas.framework.internalapi.user.model.ListUserRequest;
import org.atlas.framework.internalapi.user.model.UserResponse;
import org.atlas.infrastructure.api.client.rest.resttemplate.RestTemplateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Retry(name = "default")
@CircuitBreaker(name = "default")
@Bulkhead(name = "default")
@RequiredArgsConstructor
public class UserApiAdapter implements UserApiPort {

  @Value("${app.api-client.rest.user-service.base-url:http://localhost:8081}")
  private String baseUrl;

  private final RestTemplateService service;

  @Override
  @SuppressWarnings("unchecked")
  public List<UserResponse> call(ListUserRequest request) {
    String url = String.format("%s/api/internal/users/list", baseUrl);
    ApiResponseWrapper<List<UserResponse>> apiResponseWrapper =
        service.doPost(url, null, request, ApiResponseWrapper.class);
    return apiResponseWrapper.getData();
  }
}
