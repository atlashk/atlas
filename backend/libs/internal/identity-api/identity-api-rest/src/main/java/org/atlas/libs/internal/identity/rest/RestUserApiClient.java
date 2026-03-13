package org.atlas.libs.internal.identity.rest;

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
@RequiredArgsConstructor
public class RestUserApiClient implements UserApiClient {

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
