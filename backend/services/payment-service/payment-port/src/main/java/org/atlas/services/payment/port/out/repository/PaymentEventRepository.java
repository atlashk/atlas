package org.atlas.services.payment.port.out.repository;

import org.atlas.services.payment.domain.entity.PaymentEventEntity;

public interface PaymentEventRepository {

  void insert(PaymentEventEntity paymentEvent);

  void update(PaymentEventEntity paymentEvent);
}
