package org.atlas.domain.payment.repository;

import org.atlas.domain.payment.entity.PaymentEventEntity;

public interface PaymentEventRepository {

  void insert(PaymentEventEntity paymentEvent);

  void update(PaymentEventEntity paymentEvent);
}
