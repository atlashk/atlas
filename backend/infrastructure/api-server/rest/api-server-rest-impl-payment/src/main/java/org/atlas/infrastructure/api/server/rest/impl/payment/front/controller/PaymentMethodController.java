package org.atlas.infrastructure.api.server.rest.impl.payment.front.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.payment.shared.PaymentMethod;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment-methods")
@Validated
@RequiredArgsConstructor
public class PaymentMethodController {

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of available payment methods")
  public ApiResponseWrapper<List<String>> listPaymentMethod() throws Exception {
    List<String> paymentMethods = Arrays.stream(PaymentMethod.values())
        .map(PaymentMethod::name)
        .toList();
    return ApiResponseWrapper.success(paymentMethods);
  }
}
