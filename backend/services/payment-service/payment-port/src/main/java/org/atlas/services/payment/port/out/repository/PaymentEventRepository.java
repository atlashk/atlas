package org.atlas.services.payment.port.out.repository;

import org.atlas.services.payment.domain.entity.PaymentEvent;

public interface PaymentEventRepository {

  void insert(PaymentEvent paymentEvent);

  void update(PaymentEvent paymentEvent);
}
