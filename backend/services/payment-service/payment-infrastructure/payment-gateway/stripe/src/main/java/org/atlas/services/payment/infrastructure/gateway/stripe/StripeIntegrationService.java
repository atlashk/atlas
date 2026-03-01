package org.atlas.services.payment.infrastructure.gateway.stripe;

import static org.atlas.services.payment.port.out.gateway.model.HandleWebhookResponse.BODY_FIELD_ERROR;
import com.stripe.StripeClient;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.currency.CurrencyUtil;
import org.atlas.libs.framework.domain.shared.payment.PaymentStatus;
import org.atlas.libs.framework.http.HttpStatusCode;
import org.atlas.libs.framework.json.JsonUtil;
import org.atlas.libs.framework.util.ExceptionUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.services.payment.domain.entity.nextaction.UsePaymentElement;
import org.atlas.services.payment.port.out.gateway.exception.PaymentGatewayException;
import org.atlas.services.payment.port.out.gateway.method.Card;
import org.atlas.services.payment.port.out.gateway.model.CreateExternalPaymentRequest;
import org.atlas.services.payment.port.out.gateway.model.CreateExternalPaymentResponse;
import org.atlas.services.payment.port.out.gateway.model.HandleWebhookRequest;
import org.atlas.services.payment.port.out.gateway.model.HandleWebhookResponse;
import org.atlas.services.payment.port.out.gateway.model.HandleWebhookResponse.Result;
import org.atlas.services.payment.port.out.gateway.service.PaymentGatewayIntegrationService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "payment.stripe")
public class StripeIntegrationService implements PaymentGatewayIntegrationService {

  private final StripeClient stripeClient;
  private final StripeProps stripeProps;

  private static final List<String> SUPPORTED_EVENT_TYPE = Arrays.asList(
      StripeEventType.PAYMENT_INTENT_SUCCEEDED,
      StripeEventType.PAYMENT_INTENT_PAYMENT_FAILED,
      StripeEventType.PAYMENT_INTENT_CANCELED
  );

  @Override
  public CreateExternalPaymentResponse createPayment(CreateExternalPaymentRequest request)
      throws PaymentGatewayException {
    CreateExternalPaymentResponse response = new CreateExternalPaymentResponse();
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
              .putAllMetadata(Map.of("paymentId", request.getPaymentId()))
              .build();
      PaymentIntent paymentIntent = stripeClient.v1()
          .paymentIntents()
          .create(params);
      log.info("Created new PaymentIntent {} successfully: amount={}, currency={}",
          paymentIntent.getId(), request.getAmount(), request.getCurrency());

      // Build next action
      UsePaymentElement nextAction = UsePaymentElement.builder()
          .publishableKey(stripeProps.getPublishableKey())
          .clientSecret(paymentIntent.getClientSecret())
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
  public HandleWebhookResponse handleWebhook(HandleWebhookRequest request)
      throws PaymentGatewayException {
    HandleWebhookResponse response = new HandleWebhookResponse();
    HandleWebhookResponse.Result result = new Result();

    // Skip unsupported event types
    String eventType = JsonUtil.getInstance().getAsString(request.getRawPayload(), "type");
    if (!SUPPORTED_EVENT_TYPE.contains(eventType)) {
      response.setResponseStatus(HttpStatusCode.BAD_REQUEST.getCode());
      response.setResponseBody(Map.of(BODY_FIELD_ERROR, "Unsupported event type"));
      return response;
    }

    // Parse event object from raw payload and verify signature
    Event event;
    String sigHeader = request.getHeaders().get("stripe-signature");
    if (StringUtil.isBlank(sigHeader)) {
      response.setResponseStatus(HttpStatusCode.BAD_REQUEST.getCode());
      response.setResponseBody(Map.of(BODY_FIELD_ERROR, "Stripe signature header is required"));
      return response;
    }
    try {
      event = Webhook.constructEvent(request.getRawPayload(), sigHeader,
          stripeProps.getWebhookEndpointSecret());
    } catch (SignatureVerificationException e) {
      response.setResponseStatus(HttpStatusCode.BAD_REQUEST.getCode());
      response.setResponseBody(
          Map.of(BODY_FIELD_ERROR, "Failed to verify webhook signature: " + e.getMessage()));
      return response;
    }

    // Extract payment_intent object
    // https://docs.stripe.com/api/payment_intents/object
    String rawPaymentIntentJson = event.getDataObjectDeserializer().getRawJson();

    // Extract payment ID from event metadata
    String metadata = JsonUtil.getInstance().getAsString(rawPaymentIntentJson, "metadata");
    if (StringUtil.isBlank(metadata)) {
      response.setResponseStatus(HttpStatusCode.BAD_REQUEST.getCode());
      response.setResponseBody(
          Map.of(BODY_FIELD_ERROR, "Invalid webhook event data: Missing metadata"));
      return response;
    }
    String paymentId = JsonUtil.getInstance().getAsString(metadata, "paymentId");
    result.setPaymentId(paymentId);

    // Extract payment method
    String paymentMethod = JsonUtil.getInstance().getAsString(metadata, "payment_method");
    if (StringUtil.isBlank(paymentMethod)) {
      response.setResponseStatus(HttpStatusCode.BAD_REQUEST.getCode());
      response.setResponseBody(
          Map.of(BODY_FIELD_ERROR, "Invalid webhook event data: Missing payment_method"));
      return response;
    }
    try {
      PaymentMethod paymentMethodModel = PaymentMethod.retrieve(paymentMethod);
      if ("card".equalsIgnoreCase(paymentMethodModel.getType())) {
        result.setPaymentMethodDetails(Card.builder()
            .brand(paymentMethodModel.getCard().getBrand())
            .last4(paymentMethodModel.getCard().getLast4())
            .build());
      }
    } catch (StripeException e) {
      response.setResponseStatus(HttpStatusCode.BAD_REQUEST.getCode());
      response.setResponseBody(
          Map.of(BODY_FIELD_ERROR, "Payment method is unavailable: " + e.getMessage()));
      return response;
    }

    // Handle different event types
    switch (event.getType()) {
      case StripeEventType.PAYMENT_INTENT_SUCCEEDED -> result.setStatus(PaymentStatus.SUCCEEDED);
      case StripeEventType.PAYMENT_INTENT_PAYMENT_FAILED -> {
        result.setStatus(PaymentStatus.FAILED);
        String lastPaymentError = JsonUtil.getInstance()
            .getAsString(metadata, "last_payment_error");
        if (StringUtil.isNotBlank(lastPaymentError)) {
          String errorCode = JsonUtil.getInstance().getAsString(lastPaymentError, "error_code");
          String errorMessage = JsonUtil.getInstance().getAsString(lastPaymentError, "message");
          result.setError(ExceptionUtil.buildErrorMessage(errorCode, errorMessage));
        }
      }
      case StripeEventType.PAYMENT_INTENT_CANCELED -> {
        result.setStatus(PaymentStatus.CANCELED);
        result.setCancellationReason(
            JsonUtil.getInstance().getAsString(rawPaymentIntentJson, "cancellation_reason"));
      }
      default -> {
        log.info("Unknown webhook event type: {}", event.getType());
        result.setStatus(PaymentStatus.UNKNOWN);
      }
    }

    response.setResult(result);
    response.setResponseStatus(HttpStatusCode.OK.getCode());

    return response;
  }
}
