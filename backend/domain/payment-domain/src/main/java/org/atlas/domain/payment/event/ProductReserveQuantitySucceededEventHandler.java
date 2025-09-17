package org.atlas.domain.payment.event;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.payment.service.PaymentIntentService;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.product.ProductReserveQuantitySucceededEvent;
import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.atlas.framework.messaging.ExternalMessagePublisherPort;

@DomainEventHandler(type = DomainEventType.PRODUCT_RESERVE_QUANTITY_SUCCEEDED)
@RequiredArgsConstructor
@Slf4j
public class ProductReserveQuantitySucceededEventHandler {

  private final ApplicationConfigPort applicationConfigPort;
  private final ExternalMessagePublisherPort messagePublisherPort;
  private final PaymentIntentService paymentIntentService;

  public void handle(ProductReserveQuantitySucceededEvent event) {
    log.info("Handling ProductReserveQuantitySucceededEvent for order: {}", event.getOrderId());
    
    try {
      // Get order information to create payment intent
      // For now, we'll use mock data - in real implementation, we'd call order service
      Integer orderId = event.getOrderId();
      Integer userId = 1; // TODO: Get from order service or event
      BigDecimal amount = new BigDecimal("100.00"); // TODO: Get from order service
      
      // Create payment intent using the service
      paymentIntentService.createPaymentIntent(orderId, userId, amount);
      
      log.info("Successfully created payment intent for order: {}", orderId);
      
    } catch (Exception e) {
      log.error("Failed to handle ProductReserveQuantitySucceededEvent for order: {}", 
          event.getOrderId(), e);
      // TODO: Implement proper error handling and compensation
    }
  }
}
