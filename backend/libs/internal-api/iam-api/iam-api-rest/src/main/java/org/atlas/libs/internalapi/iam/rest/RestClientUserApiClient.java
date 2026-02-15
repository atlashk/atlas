package org.atlas.libs.internalapi.iam.rest;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.internalapi.iam.client.UserApiClient;
import org.atlas.libs.framework.internalapi.iam.model.RetrieveUserListInput;
import org.atlas.libs.framework.internalapi.iam.model.UserOutput;
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

  @Value("${app.internal-api.rest.iam-service.base-url:http://localhost:8081}")
  private String baseUrl;

  @Override
  public List<UserOutput> call(RetrieveUserListInput request) {
    String url = String.format("%s/api/internal/users/list", baseUrl);
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
