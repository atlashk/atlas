package org.atlas.infrastructure.api.server.rest.impl.payment.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.payment.entity.PaymentEntity;
import org.atlas.domain.payment.usecase.internal.handler.InternalListPaymentUseCaseHandler;
import org.atlas.domain.payment.usecase.internal.model.InternalListPaymentInput;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.internalapi.payment.model.PaymentResponse;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.rest.impl.payment.internal.model.InternalListPaymentRequest;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/payments")
@Validated
@RequiredArgsConstructor
public class InternalPaymentController {

  private final InternalListPaymentUseCaseHandler internalListPaymentUseCaseHandler;

  @Operation(summary = "List payments", description = "Retrieves a list of payments based on the provided criteria.")
  @PostMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseWrapper<List<PaymentResponse>> listPayment(
      @Parameter(description = "Request object containing the criteria for listing payments.", required = true)
      @Valid @RequestBody InternalListPaymentRequest request) throws Exception {
    InternalListPaymentInput input = ObjectMapperUtil.getInstance()
        .map(request, InternalListPaymentInput.class);
    List<PaymentEntity> paymentEntities = internalListPaymentUseCaseHandler.handle(input);
    List<PaymentResponse> paymentResponses = ObjectMapperUtil.getInstance()
        .mapList(paymentEntities, PaymentResponse.class);
    return ApiResponseWrapper.success(paymentResponses);
  }
}
