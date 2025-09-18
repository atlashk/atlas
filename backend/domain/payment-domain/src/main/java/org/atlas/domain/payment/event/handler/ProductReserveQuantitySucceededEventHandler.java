package org.atlas.domain.payment.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.payment.entity.PaymentEntity;
import org.atlas.domain.payment.repository.PaymentRepository;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.product.ProductReserveQuantitySucceededEvent;
import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.atlas.framework.lock.LockPort;
import org.atlas.framework.messaging.ExternalMessagePublisherPort;

@DomainEventHandler(type = DomainEventType.PRODUCT_RESERVE_QUANTITY_SUCCEEDED)
@RequiredArgsConstructor
@Slf4j
public class ProductReserveQuantitySucceededEventHandler {

  private final PaymentRepository paymentRepository;
  private final ApplicationConfigPort applicationConfigPort;
  private final ExternalMessagePublisherPort externalMessagePublisherPort;
  private final LockPort lockPort;

  public void handle(ProductReserveQuantitySucceededEvent event) {

  }

  private PaymentEntity createPaymentEntity(ProductReserveQuantitySucceededEvent event) {
    PaymentEntity paymentEntity = new PaymentEntity();
    paymentEntity.setOrderId(event.getOrder().getOrderId());
    paymentEntity.setUserId(event.getOrder().getUser().getId());
    paymentEntity.setAmount(event.getOrder().getAmount());
    return paymentEntity;
  }
}
