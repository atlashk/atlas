package org.atlas.infrastructure.api.server.rest.impl.payment.webhook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Webhook;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.payment.shared.PaymentGateway;
import org.atlas.domain.payment.usecase.webhook.handler.WebhookHandler;
import org.atlas.framework.payment.model.WebhookResponse;
import org.atlas.infrastructure.api.server.rest.impl.payment.webhook.util.WebhookResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhook/stripe")
@Validated
@RequiredArgsConstructor
public class StripeWebhookController {

  private final WebhookHandler webhookHandler;

  @PostMapping
  @Webhook(name = "stripe", operation = @Operation(summary = "Handle Stripe Webhook"))
  public ResponseEntity<Map<String, Object>> handleStripeWebhook(
      @RequestBody String rawPayload,
      @RequestHeader Map<String, String> headers
  ) {
    WebhookResponse response = webhookHandler.handle(PaymentGateway.STRIPE, rawPayload, headers);
    return WebhookResponseUtil.convert(response);
  }
}
