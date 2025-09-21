package org.atlas.infrastructure.internalapi.payment.rest.restclient;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.internalapi.payment.PaymentApiPort;
import org.atlas.framework.internalapi.payment.model.ListPaymentRequest;
import org.atlas.framework.internalapi.payment.model.PaymentResponse;
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
public class PaymentApiAdapter implements PaymentApiPort {

  private final RestClient restClient;
  @Value("${app.api-client.rest.payment-service.base-url:http://localhost:8084}")
  private String baseUrl;

  @Override
  public List<PaymentResponse> call(ListPaymentRequest request) {
    String url = String.format("%s/api/internal/payments/list", baseUrl);
    ApiResponseWrapper<List<PaymentResponse>> apiResponseWrapper = restClient.post()
        .uri(url)
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve()
        .toEntity(new ParameterizedTypeReference<ApiResponseWrapper<List<PaymentResponse>>>() {
        })
        .getBody();
    assert apiResponseWrapper != null;
    return apiResponseWrapper.getData();
  }
}
