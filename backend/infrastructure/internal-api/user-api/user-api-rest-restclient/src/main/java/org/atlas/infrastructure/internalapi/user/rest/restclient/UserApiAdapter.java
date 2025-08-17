package org.atlas.infrastructure.internalapi.user.rest.restclient;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
import org.atlas.framework.internalapi.user.UserApiPort;
import org.atlas.framework.internalapi.user.model.ListUserRequest;
import org.atlas.framework.internalapi.user.model.UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Retry(name = "default")
@CircuitBreaker(name = "default")
@Bulkhead(name = "default")
@RequiredArgsConstructor
public class UserApiAdapter implements UserApiPort {

  @Value("${app.api-client.rest.user-service.base-url:http://localhost:8081}")
  private String baseUrl;

  private final RestClient restClient;

  @Override
  public List<UserResponse> call(ListUserRequest request) {
    String url = String.format("%s/api/internal/users/list", baseUrl);
    ApiResponseWrapper<List<UserResponse>> apiResponseWrapper = restClient.post()
        .uri(url)
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve()
        .toEntity(new ParameterizedTypeReference<ApiResponseWrapper<List<UserResponse>>>() {
        })
        .getBody();
    assert apiResponseWrapper != null;
    return apiResponseWrapper.getData();
  }
}
