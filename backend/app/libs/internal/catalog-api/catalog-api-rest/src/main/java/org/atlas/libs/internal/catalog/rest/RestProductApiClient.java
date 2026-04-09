package org.atlas.libs.internal.catalog.rest;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.rest.ApiResponseWrapper;
import org.atlas.libs.framework.internal.catalog.client.ProductApiClient;
import org.atlas.libs.framework.internal.catalog.model.ProductOutput;
import org.atlas.libs.framework.internal.catalog.model.RetrieveProductListInput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class RestProductApiClient implements ProductApiClient {

  private final RestClient restClient;

  @Value("${app.internal.rest.catalog-service.base-url:http://localhost:8082}")
  private String catalogServiceBaseUrl;

  @Override
  public List<ProductOutput> call(RetrieveProductListInput request) {
    String url = String.format("%s/api/products/internal/list", catalogServiceBaseUrl);
    ApiResponseWrapper<List<ProductOutput>> apiResponseWrapper = restClient.post()
        .uri(url)
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve()
        .toEntity(new ParameterizedTypeReference<ApiResponseWrapper<List<ProductOutput>>>() {
        })
        .getBody();
    assert apiResponseWrapper != null;
    return apiResponseWrapper.getData();
  }
}
