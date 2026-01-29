package org.atlas.services.payment.api.server.rest.front.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.util.ObjectMapperUtil;
import org.atlas.services.payment.api.server.rest.front.mapper.PaymentGatewayMapper;
import org.atlas.services.payment.api.server.rest.front.model.PaymentGatewayResponse;
import org.atlas.services.payment.application.service.PaymentGatewayService;
import org.atlas.services.payment.domain.entity.PaymentGateway;
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

  private final PaymentGatewayService paymentGatewayService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of available payment gateways")
  public ApiResponseWrapper<List<PaymentGatewayResponse>> listPaymentMethod() throws Exception {
    List<PaymentGateway> paymentGateways = paymentGatewayService.retrievePaymentGatewayList();
    List<PaymentGatewayResponse> responseData = ObjectMapperUtil.mapList(paymentGateways,
        PaymentGatewayMapper.INSTANCE::toPaymentGatewayResponse);
    return ApiResponseWrapper.success(responseData);
  }
}
