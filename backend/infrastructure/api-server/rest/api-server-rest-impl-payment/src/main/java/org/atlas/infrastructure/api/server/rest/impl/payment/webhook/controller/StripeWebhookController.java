package org.atlas.infrastructure.api.server.rest.impl.payment.webhook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Webhook;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.payment.shared.PaymentGateway;
import org.atlas.domain.payment.usecase.webhook.handler.WebhookHandler;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments/webhook/stripe")
@Validated
@RequiredArgsConstructor
public class StripeWebhookController {

  private final WebhookHandler webhookHandler;

  @Webhook(
      name = "orderCreatedWebhook",
      operation = @Operation(
          summary = "Handle Stripe Webhook",
          description = "Handles incoming webhooks from Stripe."
      )
  )
  @PostMapping
  public Map<String, Object> handleStripeWebhook(
      @RequestBody Map<String, Object> payload,
      @RequestHeader Map<String, String> headers) {
    return webhookHandler.handle(PaymentGateway.STRIPE, payload, headers);
  }
}
