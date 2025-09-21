package org.atlas.infrastructure.payment.stripe;

import com.google.gson.JsonObject;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.payment.shared.PaymentStatus;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.error.AppError;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.payment.PaymentGatewayPort;
import org.atlas.framework.payment.exception.PaymentGatewayException;
import org.atlas.framework.payment.model.CreatePaymentRequest;
import org.atlas.framework.payment.model.CreatePaymentResponse;
import org.atlas.framework.payment.model.PaymentResult;
import org.atlas.framework.payment.model.WebhookResponse;
import org.atlas.framework.util.StringUtil;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "payment.stripe")
public class StripePaymentGatewayAdapter implements PaymentGatewayPort {

  private final StripeService stripeService;

  @Override
  public CreatePaymentResponse createPayment(CreatePaymentRequest request)
      throws PaymentGatewayException {
    StripePaymentMethod method;
    try {
      method = StripePaymentMethod.valueOf(request.getMethod().getType());
    } catch (IllegalArgumentException e) {
      throw new DomainException(AppError.PAYMENT_METHOD_NOT_SUPPORTED);
    }

    CreatePaymentResponse response = new CreatePaymentResponse();
    try {
      PaymentIntent paymentIntent = stripeService.createPaymentIntent(
          request.getAmount(),
          request.getCurrency(),
          method,
          Map.of("paymentId", String.valueOf(request.getPaymentId()))
      );
      Map<String, Object> data = new HashMap<>();
      data.put("clientSecret", paymentIntent.getClientSecret());
      response.setSuccess(true);
      response.setData(data);
    } catch (StripeException e) {
      response.setSuccess(false);
      response.setErrorCode(e.getCode());
      response.setErrorMessage(e.getMessage());
    }
    return response;
  }

  @Override
  public PaymentStatus getPaymentStatus(String transactionId) throws PaymentGatewayException {
    try {
      PaymentIntent paymentIntent = stripeService.retrievePaymentIntent(transactionId);
      return switch (paymentIntent.getStatus()) {
        case "succeeded" -> PaymentStatus.SUCCEEDED;
        case "requires_payment_method", "requires_confirmation", "requires_action",
             "processing", "requires_capture" -> PaymentStatus.CREATED;
        case "canceled" -> PaymentStatus.CANCELED;
        default -> PaymentStatus.FAILED;
      };
    } catch (StripeException e) {
      throw new PaymentGatewayException(e);
    }
  }

  /**
   * Refer <a href="https://docs.stripe.com/api/events/object"></a>
   */
  @Override
  public WebhookResponse handleWebhook(Map<String, Object> payload,
      Map<String, String> headers) throws PaymentGatewayException {
    WebhookResponse response = new WebhookResponse();

    // Verify signature
    if (!verifySignature(payload, headers)) {
      log.error("Invalid webhook signature");
      response.setResponseStatus(400);
      return response;
    }

    PaymentResult paymentResult = new PaymentResult();
    try {
      Event event = JsonUtil.getInstance().toObject((LinkedHashMap<?, ?>) payload, Event.class);

      // Extract payment_intent object
      // https://docs.stripe.com/api/payment_intents/object
      EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
      if (dataObjectDeserializer.getObject().isEmpty()) {
        log.error("Invalid webhook event data: Missing object");
        response.setResponseStatus(400);
        return response;
      }
      StripeObject stripeObject = dataObjectDeserializer.getObject().get();
      JsonObject object = stripeObject.getRawJsonObject();

      // Payment ID
      if (!object.has("metadata")) {
        log.error("Invalid webhook event data: Missing metadata");
        response.setResponseStatus(400);
        return response;
      }
      JsonObject metadata = object.getAsJsonObject("metadata");
      paymentResult.setPaymentId(metadata.get("paymentId").getAsInt());

      // Handle different event types
      switch (event.getType()) {
        case "payment_intent.succeeded" -> paymentResult.setStatus(PaymentStatus.SUCCEEDED);
        case "payment_intent.payment_failed" -> {
          paymentResult.setStatus(PaymentStatus.FAILED);
          if (object.has("last_payment_error")) {
            JsonObject lastPaymentError = object.getAsJsonObject("last_payment_error");
            paymentResult.setErrorCode(lastPaymentError.get("code").getAsString());
            paymentResult.setErrorMessage(lastPaymentError.get("message").getAsString());
          }
        }
        case "payment_intent.canceled" -> {
          paymentResult.setStatus(PaymentStatus.CANCELED);
          paymentResult.setCancellationReason(object.get("cancellation_reason").getAsString());
        }
        default -> {
          log.info("Unknown webhook event type: {}", event.getType());
          paymentResult.setStatus(PaymentStatus.UNKNOWN);
        }
      }

      response.setPaymentResult(paymentResult);
      response.setResponseStatus(200);
    } catch (Exception e) {
      response.setResponseStatus(500);
    }

    return response;
  }

  private boolean verifySignature(Map<String, Object> payload, Map<String, String> headers)
      throws PaymentGatewayException {
    try {
      String sigHeader = headers.get("stripe-signature");
      if (StringUtil.isBlank(sigHeader)) {
        log.error("Stripe signature header is empty");
        return false;
      }

      String payloadJson = JsonUtil.getInstance().toJson(payload);

      return stripeService.verifyWebhookSignature(payloadJson, sigHeader);
    } catch (Exception e) {
      throw new PaymentGatewayException(e);
    }
  }
}
