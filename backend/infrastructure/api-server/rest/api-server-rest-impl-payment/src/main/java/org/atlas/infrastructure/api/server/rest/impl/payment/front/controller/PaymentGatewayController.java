package org.atlas.infrastructure.api.server.rest.impl.payment.front.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.payment.entity.PaymentGateway;
import org.atlas.domain.payment.usecase.front.handler.ListPaymentGatewayUseCaseHandler;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.rest.impl.payment.front.mapper.PaymentGatewayMapper;
import org.atlas.infrastructure.api.server.rest.impl.payment.front.model.PaymentGatewayResponse;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment-gateways")
@Validated
@RequiredArgsConstructor
public class PaymentGatewayController {

  private final ListPaymentGatewayUseCaseHandler listPaymentGatewayUseCaseHandler;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of available payment gateways")
  public ApiResponseWrapper<List<PaymentGatewayResponse>> listPaymentMethod() throws Exception {
    List<PaymentGateway> paymentGateways = listPaymentGatewayUseCaseHandler.handle();

    List<PaymentGatewayResponse> responseData = ObjectMapperUtil.mapList(paymentGateways,
        PaymentGatewayMapper.INSTANCE::toPaymentGatewayResponse);
    return ApiResponseWrapper.success(responseData);
  }
}
