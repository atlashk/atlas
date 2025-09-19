package org.atlas.domain.payment.usecase.webhook.handler;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.payment.entity.WebhookEventEntity;
import org.atlas.domain.payment.repository.WebhookEventRepository;
import org.atlas.domain.payment.shared.enums.PaymentGateway;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.handler.UseCaseHandler;
import org.atlas.framework.error.AppError;
import org.atlas.framework.payment.PaymentGatewayPort;
import org.atlas.framework.payment.PaymentGatewayService;

@UseCaseHandler
@RequiredArgsConstructor
public class WebhookHandler {

  private final WebhookEventRepository webhookEventRepository;
  private final PaymentGatewayService paymentGatewayService;

  public Map<String, Object> handle(PaymentGateway paymentGateway,
      Map<String, Object> payload, Map<String, String> headers) {
    PaymentGatewayPort paymentGatewayPort = paymentGatewayService.getPaymentGatewayPort(paymentGateway)
        .orElseThrow(() -> new DomainException(AppError.PAYMENT_GATEWAY_NOT_SUPPORTED));


    return paymentGatewayPort.handleWebhook(payload, headers);
  }
}
