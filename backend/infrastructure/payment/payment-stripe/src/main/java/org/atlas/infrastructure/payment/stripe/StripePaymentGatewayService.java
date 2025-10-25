package org.atlas.infrastructure.payment.stripe;

import com.stripe.StripeClient;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.payment.shared.PaymentGateway;
import org.atlas.domain.payment.shared.PaymentStatus;
import org.atlas.framework.http.HttpStatusCode;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.lock.LockService;
import org.atlas.framework.payment.PaymentGatewayService;
import org.atlas.framework.payment.exception.PaymentGatewayException;
import org.atlas.framework.payment.model.CreatePaymentRequest;
import org.atlas.framework.payment.model.CreatePaymentResponse;
import org.atlas.framework.payment.model.PaymentResult;
import org.atlas.framework.payment.model.WebhookResponse;
import org.atlas.framework.payment.model.nextaction.UsePaymentElement;
import org.atlas.framework.payment.model.nextaction.UsePaymentElement.Provider;
import org.atlas.framework.util.CurrencyUtil;
import org.atlas.framework.util.StringUtil;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "payment.stripe")
public class StripePaymentGatewayService implements PaymentGatewayService {

  private final StripeClient stripeClient;
  private final StripeProps stripeProps;
  private final LockService lockService;

  private static final List<String> SUPPORTED_EVENT_TYPE = Arrays.asList(
      StripeEventType.PAYMENT_INTENT_SUCCEEDED,
      StripeEventType.PAYMENT_INTENT_PAYMENT_FAILED,
      StripeEventType.PAYMENT_INTENT_CANCELED
  );

  @Override
  public PaymentGateway supports() {
    return PaymentGateway.STRIPE;
  }

  @Override
  public CreatePaymentResponse createPayment(CreatePaymentRequest request)
      throws PaymentGatewayException {
    CreatePaymentResponse response = new CreatePaymentResponse();
    try {
      // Create payment intent
      PaymentIntentCreateParams params =
          PaymentIntentCreateParams.builder()
              .setAmount(
                  CurrencyUtil.getAmountInSmallestUnit(request.getAmount(), request.getCurrency()))
              .setCurrency(request.getCurrency())
              .setAutomaticPaymentMethods(
                  PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                      .setEnabled(true)
                      .build()
              )
              .putAllMetadata(Map.of("paymentId", String.valueOf(request.getPaymentId())))
              .build();
      PaymentIntent paymentIntent = stripeClient.v1()
          .paymentIntents()
          .create(params);
      log.info("Created new PaymentIntent {} successfully: amount={}, currency={}",
          paymentIntent.getId(), request.getAmount(), request.getCurrency());

      // Build next action
      UsePaymentElement nextAction = UsePaymentElement.builder()
          .provider(Provider.STRIPE)
          .clientSecret(paymentIntent.getClientSecret())
          .publishableKey(stripeProps.getPublishableKey())
          .build();

      response.setSuccess(true);
      response.setTransactionId(paymentIntent.getId());
      response.setNextAction(nextAction);
    } catch (StripeException e) {
      response.setSuccess(false);
      response.setErrorCode(e.getCode());
      response.setErrorMessage(e.getMessage());
    }
    return response;
  }

  /**
   * Refer <a href="https://docs.stripe.com/api/events/object"></a>
   */
  @Override
  public WebhookResponse handleWebhook(String rawPayload,
      Map<String, String> headers) throws PaymentGatewayException {
    WebhookResponse response = new WebhookResponse();

    // Skip unsupported event types
    String eventType = JsonUtil.getInstance().getAsString(rawPayload, "type");
    if (!SUPPORTED_EVENT_TYPE.contains(eventType)) {
      response.setResponseStatus(HttpStatusCode.BAD_REQUEST.getCode());
      return response;
    }

    // Parse event object from raw payload and verify signature
    Event event;
    String sigHeader = headers.get("stripe-signature");
    if (StringUtil.isBlank(sigHeader)) {
      log.error("Stripe signature header is required");
      response.setResponseStatus(HttpStatusCode.BAD_REQUEST.getCode());
      return response;
    }
    try {
      event = Webhook.constructEvent(rawPayload, sigHeader, stripeProps.getWebhookEndpointSecret());
    } catch (SignatureVerificationException e) {
      log.error("Failed to verify webhook signature: {}", e.getMessage(), e);
      response.setResponseStatus(HttpStatusCode.BAD_REQUEST.getCode());
      return response;
    }

    // Extract payment_intent object
    // https://docs.stripe.com/api/payment_intents/object
    String rawPaymentIntentJson = event.getDataObjectDeserializer().getRawJson();

    // Extract payment ID from event metadata
    String metadata = JsonUtil.getInstance().getAsString(rawPaymentIntentJson, "metadata");
    if (StringUtil.isBlank(metadata)) {
      log.error("Invalid webhook event data: Missing metadata");
      response.setResponseStatus(HttpStatusCode.BAD_REQUEST.getCode());
      return response;
    }
    Integer paymentId = JsonUtil.getInstance().getAsInt(metadata, "paymentId");

    PaymentResult paymentResult = new PaymentResult();
    String lockKey = "payment:webhook:" + paymentId;
    Duration waitTime = Duration.ofMinutes(5);
    Duration leaseTime = Duration.ofDays(7);
    try {
      // Webhook idempotency
      boolean acquiredLock = lockService.acquireLock(lockKey, waitTime, leaseTime);
      if (!acquiredLock) {
        log.error("The webhook event of payment {} has been already processing", paymentId);
        response.setResponseStatus(HttpStatusCode.CONFLICT.getCode());
        return response;
      }

      paymentResult.setPaymentId(paymentId);

      // Handle different event types
      switch (event.getType()) {
        case StripeEventType.PAYMENT_INTENT_SUCCEEDED ->
            paymentResult.setStatus(PaymentStatus.SUCCEEDED);
        case StripeEventType.PAYMENT_INTENT_PAYMENT_FAILED -> {
          paymentResult.setStatus(PaymentStatus.FAILED);
          String lastPaymentError = JsonUtil.getInstance()
              .getAsString(metadata, "last_payment_error");
          if (StringUtil.isNotBlank(lastPaymentError)) {
            paymentResult.setErrorCode(
                JsonUtil.getInstance().getAsString(lastPaymentError, "error_code"));
            paymentResult.setErrorMessage(
                StringUtil.sanitizeErrorMessage(
                    JsonUtil.getInstance().getAsString(lastPaymentError, "message")));
          }
        }
        case StripeEventType.PAYMENT_INTENT_CANCELED -> {
          paymentResult.setStatus(PaymentStatus.CANCELED);
          paymentResult.setCancellationReason(
              JsonUtil.getInstance().getAsString(rawPaymentIntentJson, "cancellation_reason"));
        }
        default -> {
          log.info("Unknown webhook event type: {}", event.getType());
          paymentResult.setStatus(PaymentStatus.UNKNOWN);
        }
      }

      response.setPaymentResult(paymentResult);
      response.setResponseStatus(HttpStatusCode.OK.getCode());
    } catch (Exception e) {
      log.error("Failed to handle webhook event: {}", e.getMessage(), e);
      response.setResponseStatus(HttpStatusCode.INTERNAL_SERVER_ERROR.getCode());
    } finally {
      lockService.releaseLock(lockKey);
    }

    return response;
  }
}
