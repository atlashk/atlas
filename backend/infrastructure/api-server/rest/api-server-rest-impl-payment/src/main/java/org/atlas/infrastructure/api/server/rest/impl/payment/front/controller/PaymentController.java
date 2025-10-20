package org.atlas.infrastructure.api.server.rest.impl.payment.front.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.payment.usecase.front.handler.GetPaymentNextActionUseCaseHandler;
import org.atlas.domain.payment.usecase.front.model.GetPaymentNextActionInput;
import org.atlas.domain.payment.usecase.front.model.GetPaymentNextActionOutput;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.rest.impl.payment.front.model.GetPaymentNextActionResponse;
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

  private final GetPaymentNextActionUseCaseHandler getPaymentNextActionUseCaseHandler;

  @GetMapping(value = "/{orderId}/next-action", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get order status")
  public ApiResponseWrapper<GetPaymentNextActionResponse> getPaymentNextAction(
      @Parameter(name = "orderId", description = "Order ID associated with the payment", example = "1")
      @PathVariable("orderId") Integer orderId) throws Exception {
    GetPaymentNextActionInput input = new GetPaymentNextActionInput(orderId);
    GetPaymentNextActionOutput output = getPaymentNextActionUseCaseHandler.handle(input);
    GetPaymentNextActionResponse response = ObjectMapperUtil.getInstance()
        .map(output, GetPaymentNextActionResponse.class);
    return ApiResponseWrapper.success(response);
  }
}
