package org.atlas.payment.api.server.rest.front.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.atlas.payment.application.model.RetrievePaymentNextActionOutput;
import org.atlas.payment.application.service.PaymentService;
import org.atlas.common.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.common.framework.context.Contexts;
import org.atlas.payment.api.server.rest.front.mapper.PaymentMapper;
import org.atlas.payment.api.server.rest.front.model.RetrievePaymentNextActionResponse;
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
  @Operation(summary = "Get payment next action")
  public ApiResponseWrapper<RetrievePaymentNextActionResponse> getPaymentNextAction(
      @Parameter(name = "orderId", description = "Order ID associated with the payment", example = "1")
      @PathVariable Integer orderId) throws Exception {
    RetrievePaymentNextActionOutput output = paymentService.retrievePaymentNextAction(orderId,
        Contexts.getUserId());
    RetrievePaymentNextActionResponse responseData =
        PaymentMapper.INSTANCE.toRetrievePaymentNextActionResponse(output);
    return ApiResponseWrapper.success(responseData);
  }
}
