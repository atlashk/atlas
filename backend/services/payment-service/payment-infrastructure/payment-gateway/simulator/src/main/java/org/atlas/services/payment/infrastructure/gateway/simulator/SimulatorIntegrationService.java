package org.atlas.services.payment.infrastructure.gateway.simulator;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.shared.payment.PaymentStatus;
import org.atlas.libs.framework.http.HttpStatusCode;
import org.atlas.libs.framework.util.JsonUtil;
import org.atlas.libs.framework.util.RandomUtil;
import org.atlas.services.payment.domain.entity.nextaction.UsePaymentElement;
import org.atlas.services.payment.port.out.gateway.exception.PaymentGatewayException;
import org.atlas.services.payment.port.out.gateway.method.Card;
import org.atlas.services.payment.port.out.gateway.model.CreateExternalPaymentRequest;
import org.atlas.services.payment.port.out.gateway.model.CreateExternalPaymentResponse;
import org.atlas.services.payment.port.out.gateway.model.HandleWebhookRequest;
import org.atlas.services.payment.port.out.gateway.model.HandleWebhookResponse;
import org.atlas.services.payment.port.out.gateway.model.HandleWebhookResponse.Result;
import org.atlas.services.payment.port.out.gateway.service.PaymentGatewayIntegrationService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "simulator")
public class SimulatorIntegrationService implements PaymentGatewayIntegrationService {

  private final SimulatorProps simulatorProps;
  private final RestClient restClient;
  private final ScheduledExecutorService scheduledExecutorService;

  private static final String SIGNATURE_HEADER_NAME = "x-simulator-signature";

  @Override
  public CreateExternalPaymentResponse createPayment(CreateExternalPaymentRequest request)
      throws PaymentGatewayException {
    log.info("Creating simulated payment for paymentId={}, amount={}, currency={}",
        request.getPaymentId(), request.getAmount(), request.getCurrency());

    try {
      // Generate a simulated transaction ID
      String transactionId = generateTransactionId();

      // Simulate async webhook call with random delay (1-3 seconds)
      scheduleWebhookCall(request.getPaymentId(), transactionId);

      // Build the payment element for frontend
      UsePaymentElement nextAction = UsePaymentElement.builder()
          .publishableKey("pk-test-1234")
          .clientSecret("cs-test-1234")
          .build();

      return CreateExternalPaymentResponse.builder()
          .success(true)
          .transactionId(transactionId)
          .nextAction(nextAction)
          .build();
    } catch (Exception e) {
      log.error("Failed to create simulated payment for paymentId={}", request.getPaymentId(), e);
      return CreateExternalPaymentResponse.builder()
          .success(false)
          .errorCode("SIMULATOR_ERROR")
          .errorMessage("Failed to create simulated payment: " + e.getMessage())
          .build();
    }
  }

  @Override
  public HandleWebhookResponse handleWebhook(HandleWebhookRequest request)
      throws PaymentGatewayException {
    // Verify signature
    String receivedSignature = request.getHeaders().get(SIGNATURE_HEADER_NAME);
    if (receivedSignature == null || receivedSignature.trim().isEmpty()) {
      log.error("Missing {} header in webhook request", SIGNATURE_HEADER_NAME);
      return HandleWebhookResponse.builder()
          .responseStatus(HttpStatusCode.BAD_REQUEST.getCode())
          .responseBody(Map.of(HandleWebhookResponse.BODY_FIELD_ERROR,
              SIGNATURE_HEADER_NAME + " header is required"))
          .build();
    }

    // Generate expected signature and compare
    String expectedSignature = generateSignature(request.getRawPayload());
    if (!expectedSignature.equals(receivedSignature)) {
      log.error("Signature verification failed. Expected: {}, Received: {}",
          expectedSignature, receivedSignature);
      return HandleWebhookResponse.builder()
          .responseStatus(HttpStatusCode.BAD_REQUEST.getCode())
          .responseBody(Map.of(HandleWebhookResponse.BODY_FIELD_ERROR,
              "Failed to verify webhook signature"))
          .build();
    }

    log.info("Webhook signature verified successfully");

    // Parse the simulated webhook payload
    SimulatorWebhookPayload payload = JsonUtil
        .toObject(request.getRawPayload(), SimulatorWebhookPayload.class);

    Result result = Result.builder()
        .paymentId(payload.getPaymentId())
        .paymentMethod(payload.getPaymentMethod())
        .paymentMethodDetails(
            JsonUtil.toObject(payload.getPaymentMethodDetails(), Card.class))
        .status(payload.getStatus())
        .error(payload.getError())
        .cancellationReason(payload.getCancellationReason())
        .build();

    return HandleWebhookResponse.builder()
        .result(result)
        .responseStatus(HttpStatusCode.OK.getCode())
        .build();
  }

  private void scheduleWebhookCall(String paymentId, String transactionId) {
    int delaySeconds = RandomUtil.randomInt(1, 2);
    scheduledExecutorService.schedule(() -> {
      log.info("Executing scheduled webhook call for paymentId={}, transactionId={}",
          paymentId, transactionId);
      sendWebhook(paymentId);
    }, delaySeconds, TimeUnit.SECONDS);
  }

  private void sendWebhook(String paymentId) {
    // Create webhook payload
    SimulatorWebhookPayload payload = SimulatorWebhookPayload.builder()
        .paymentId(paymentId)
        .paymentMethod("card")
        .paymentMethodDetails("{\"brand\":\"visa\",\"last4\":\"1234\"}")
        .status(PaymentStatus.SUCCEEDED)
        .build();

    // Send webhook
    log.info("Sending simulated webhook to: {} for paymentId={}",
        simulatorProps.getWebhookUrl(), paymentId);

    String payloadJson = JsonUtil.toJson(payload);

    ResponseEntity<?> response = restClient.post()
        .uri(simulatorProps.getWebhookUrl())
        .contentType(MediaType.APPLICATION_JSON)
        .header(SIGNATURE_HEADER_NAME, generateSignature(payloadJson))
        .body(payloadJson)
        .retrieve()
        .toEntity(new ParameterizedTypeReference<>() {
        });

    if (response.getStatusCode().is2xxSuccessful()) {
      log.info("Webhook sent successfully for paymentId={}, status={}",
          paymentId, response.getStatusCode());
    } else {
      log.warn("Webhook returned non-success status for paymentId={}, status={}",
          paymentId, response.getStatusCode());
    }
  }

  private String generateTransactionId() {
    return "sim_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
  }

  private String generateSignature(String payloadJson) {
    // Simple signature generation for simulation purposes
    // In a real payment gateway, this would be a proper HMAC signature
    return "sim_" + Integer.toHexString(payloadJson.hashCode());
  }
}
