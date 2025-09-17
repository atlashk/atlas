package org.atlas.domain.payment.service;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.payment.entity.PaymentIntentEntity;
import org.atlas.domain.payment.repository.PaymentIntentRepository;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.domain.event.contract.payment.PaymentIntentCreatedEvent;
import org.atlas.framework.domain.service.DomainService;
import org.atlas.framework.messaging.ExternalMessagePublisherPort;

@DomainService
@RequiredArgsConstructor
@Slf4j
public class PaymentIntentService {

  private final PaymentIntentRepository paymentIntentRepository;
  private final ApplicationConfigPort applicationConfigPort;
  private final ExternalMessagePublisherPort messagePublisherPort;
  // TODO: Add StripeService when implementing actual Stripe integration

  public PaymentIntentEntity createPaymentIntent(Integer orderId, Integer userId, BigDecimal amount) {
    log.info("Creating payment intent for order: {}, user: {}, amount: {}", orderId, userId, amount);
    
    // Check if payment intent already exists for this order (idempotency)
    if (paymentIntentRepository.existsByOrderId(orderId)) {
      log.warn("Payment intent already exists for order: {}", orderId);
      return paymentIntentRepository.findByOrderId(orderId).orElse(null);
    }

    try {
      // TODO: Create actual Stripe Payment Intent
      // For now, we'll simulate the creation
      String stripePaymentIntentId = "pi_" + System.currentTimeMillis(); // Mock Stripe ID
      String clientSecret = stripePaymentIntentId + "_secret_" + System.currentTimeMillis();
      
      // Create PaymentIntent entity
      PaymentIntentEntity paymentIntent = new PaymentIntentEntity();
      paymentIntent.setStripePaymentIntentId(stripePaymentIntentId);
      paymentIntent.setOrderId(orderId);
      paymentIntent.setUserId(userId);
      paymentIntent.setAmount(amount);
      paymentIntent.setCurrency("USD");
      paymentIntent.setStatus("requires_payment_method");
      paymentIntent.setClientSecret(clientSecret);
      paymentIntent.setDescription("Payment for order #" + orderId);
      
      // Save to database
      paymentIntentRepository.insert(paymentIntent);
      
      // Publish event
      publishPaymentIntentCreatedEvent(paymentIntent);
      
      log.info("Payment intent created successfully: {}", paymentIntent.getId());
      return paymentIntent;
      
    } catch (Exception e) {
      log.error("Failed to create payment intent for order: {}", orderId, e);
      throw new RuntimeException("Failed to create payment intent", e);
    }
  }

  private void publishPaymentIntentCreatedEvent(PaymentIntentEntity paymentIntent) {
    PaymentIntentCreatedEvent event = new PaymentIntentCreatedEvent(
        applicationConfigPort.getApplicationName()
    );
    
    event.setPaymentId(paymentIntent.getId());
    event.setOrderId(paymentIntent.getOrderId());
    event.setStripePaymentIntentId(paymentIntent.getStripePaymentIntentId());
    event.setAmount(paymentIntent.getAmount());
    event.setCurrency(paymentIntent.getCurrency());
    event.setClientSecret(paymentIntent.getClientSecret());
    event.setStatus(paymentIntent.getStatus());
    
    messagePublisherPort.publish(event);
    log.info("Published PaymentIntentCreatedEvent for payment intent: {}", paymentIntent.getId());
  }
}