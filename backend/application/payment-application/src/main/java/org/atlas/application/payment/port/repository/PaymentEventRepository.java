package org.atlas.application.payment.port.repository;

import org.atlas.domain.payment.entity.PaymentEvent;

public interface PaymentEventRepository {

  void insert(PaymentEvent paymentEvent);

  void update(PaymentEvent paymentEvent);
}
