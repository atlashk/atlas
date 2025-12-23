package org.atlas.infrastructure.api.server.rest.adapter.payment.webhook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Webhook;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.application.payment.service.PaymentWebhookService;
import org.atlas.framework.payment.model.HandleWebhookResponse;
import org.atlas.infrastructure.api.server.rest.adapter.payment.webhook.util.WebhookResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhook")
@Validated
@RequiredArgsConstructor
public class PaymentWebhookController {

  private final PaymentWebhookService paymentWebhookService;

  @PostMapping("/simulator")
  @Webhook(name = "simulator", operation = @Operation(summary = "Handle simulator webhook"))
  public ResponseEntity<Map<String, Object>> handleSimulatorWebhook(
      @RequestBody String rawPayload,
      @RequestHeader Map<String, String> headers
  ) {
    HandleWebhookResponse handleWebhookResponse = paymentWebhookService.handle(
        "simulator", rawPayload, headers);
    return WebhookResponseUtil.resolve(handleWebhookResponse);
  }

  @PostMapping("/stripe")
  @Webhook(name = "stripe", operation = @Operation(summary = "Handle Stripe webhook"))
  public ResponseEntity<Map<String, Object>> handleStripeWebhook(
      @RequestBody String rawPayload,
      @RequestHeader Map<String, String> headers
  ) {
    HandleWebhookResponse handleWebhookResponse = paymentWebhookService.handle(
        "stripe", rawPayload, headers);
    return WebhookResponseUtil.resolve(handleWebhookResponse);
  }
}
