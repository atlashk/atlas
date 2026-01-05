package org.atlas.payment.application.port.repository;

import org.atlas.payment.domain.entity.PaymentEvent;

public interface PaymentEventRepository {

  void insert(PaymentEvent paymentEvent);

  void update(PaymentEvent paymentEvent);
}
