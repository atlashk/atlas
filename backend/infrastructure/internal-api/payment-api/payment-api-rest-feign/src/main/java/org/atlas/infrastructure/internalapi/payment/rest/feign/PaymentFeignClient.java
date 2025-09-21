package org.atlas.infrastructure.internalapi.payment.rest.feign;

import java.util.List;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.internalapi.payment.model.ListPaymentRequest;
import org.atlas.framework.internalapi.payment.model.PaymentResponse;
import org.atlas.infrastructure.api.client.rest.feign.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "payment-service",
    url = "${app.api-client.rest.payment-service.base-url:http://localhost:8084}",
    configuration = FeignConfig.class
)
public interface PaymentFeignClient {

  @PostMapping("/api/internal/payments/list")
  ApiResponseWrapper<List<PaymentResponse>> listPayment(@RequestBody ListPaymentRequest request);
}
