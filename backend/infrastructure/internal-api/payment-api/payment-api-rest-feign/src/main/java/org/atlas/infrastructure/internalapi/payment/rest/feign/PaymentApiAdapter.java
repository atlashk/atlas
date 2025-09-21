package org.atlas.infrastructure.internalapi.payment.rest.feign;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.internalapi.payment.PaymentApiPort;
import org.atlas.framework.internalapi.payment.model.ListPaymentRequest;
import org.atlas.framework.internalapi.payment.model.PaymentResponse;
import org.springframework.stereotype.Component;

@Component
@Retry(name = "default")
@CircuitBreaker(name = "default")
@Bulkhead(name = "default")
@RequiredArgsConstructor
public class PaymentApiAdapter implements PaymentApiPort {

  private final PaymentFeignClient feignClient;

  @Override
  public List<PaymentResponse> call(ListPaymentRequest request) {
    ApiResponseWrapper<List<PaymentResponse>> apiResponseWrapper = feignClient.listPayment(request);
    return apiResponseWrapper.getData();
  }
}
