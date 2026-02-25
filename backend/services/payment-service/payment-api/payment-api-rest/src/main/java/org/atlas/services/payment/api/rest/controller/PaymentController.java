package org.atlas.services.payment.api.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.services.payment.api.rest.mapper.PaymentMapper;
import org.atlas.services.payment.api.rest.model.RetrievePaymentNextActionResponse;
import org.atlas.services.payment.port.in.model.RetrievePaymentNextActionOutput;
import org.atlas.services.payment.port.in.service.PaymentService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@Validated
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;

  @GetMapping(value = "/{orderId}/next-action", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve payment next action")
  public ApiResponseWrapper<RetrievePaymentNextActionResponse> retrievePaymentNextAction(
      @Parameter(name = "orderId", description = "Order ID associated with the payment", example = "1")
      @PathVariable String orderId) throws Exception {
    RetrievePaymentNextActionOutput output = paymentService.retrievePaymentNextAction(orderId);
    RetrievePaymentNextActionResponse responseData =
        PaymentMapper.INSTANCE.toRetrievePaymentNextActionResponse(output);
    return ApiResponseWrapper.success(responseData);
  }
}
