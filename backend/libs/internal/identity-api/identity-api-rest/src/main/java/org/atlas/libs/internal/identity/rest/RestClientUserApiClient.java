package org.atlas.libs.internal.identity.rest;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.internal.identity.client.UserApiClient;
import org.atlas.libs.framework.internal.identity.model.RetrieveUserListInput;
import org.atlas.libs.framework.internal.identity.model.UserOutput;
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
public class RestClientUserApiClient implements UserApiClient {

  private final RestClient restClient;

  @Value("${app.internal.rest.identity-service.base-url:http://localhost:8081}")
  private String identityServiceBaseUrl;

  @Override
  public List<UserOutput> call(RetrieveUserListInput request) {
    String url = String.format("%s/api/users/internal/list", identityServiceBaseUrl);
    ApiResponseWrapper<List<UserOutput>> apiResponseWrapper = restClient.post()
        .uri(url)
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve()
        .toEntity(new ParameterizedTypeReference<ApiResponseWrapper<List<UserOutput>>>() {
        })
        .getBody();
    assert apiResponseWrapper != null;
    return apiResponseWrapper.getData();
  }
}
