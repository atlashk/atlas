package org.atlas.infrastructure.api.server.rest.impl.payment.front.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.payment.usecase.front.handler.GetPaymentTrackingUseCaseHandler;
import org.atlas.domain.payment.usecase.front.model.GetPaymentTrackingInput;
import org.atlas.domain.payment.usecase.front.model.GetPaymentTrackingOutput;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.rest.impl.payment.front.model.PaymentTrackingResponse;
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

  private final GetPaymentTrackingUseCaseHandler getPaymentTrackingUseCaseHandler;

  @GetMapping(value = "/{sagaId}/tracking", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get order status")
  public ApiResponseWrapper<PaymentTrackingResponse> getPaymentTracking(
      @Parameter(name = "sagaId", description = "Saga ID associated with the payment", example = "1")
      @PathVariable("sagaId") Integer sagaId) throws Exception {
    GetPaymentTrackingInput input = new GetPaymentTrackingInput(sagaId);
    GetPaymentTrackingOutput output = getPaymentTrackingUseCaseHandler.handle(input);
    PaymentTrackingResponse response = ObjectMapperUtil.getInstance()
        .map(output, PaymentTrackingResponse.class);
    return ApiResponseWrapper.success(response);
  }
}
