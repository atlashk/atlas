package org.atlas.infrastructure.internalapi.payment.rest.resttemplate;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.internalapi.payment.PaymentApiPort;
import org.atlas.framework.internalapi.payment.model.ListPaymentRequest;
import org.atlas.framework.internalapi.payment.model.PaymentResponse;
import org.atlas.infrastructure.api.client.rest.resttemplate.RestTemplateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Retry(name = "default")
@CircuitBreaker(name = "default")
@Bulkhead(name = "default")
@RequiredArgsConstructor
public class PaymentApiAdapter implements PaymentApiPort {

  private final RestTemplateService service;
  @Value("${app.api-client.rest.payment-service.base-url:http://localhost:8082}")
  private String baseUrl;

  @Override
  @SuppressWarnings("unchecked")
  public List<PaymentResponse> call(ListPaymentRequest request) {
    String url = String.format("%s/api/internal/payments/list", baseUrl);
    ApiResponseWrapper<List<PaymentResponse>> apiResponseWrapper =
        service.doPost(url, null, request, ApiResponseWrapper.class);
    return apiResponseWrapper.getData();
  }
}
