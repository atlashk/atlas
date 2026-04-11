package org.atlas.services.payment.port.out.repository;

import java.util.Optional;
import org.atlas.services.payment.domain.entity.PaymentEvent;

public interface PaymentEventRepository {

  Optional<PaymentEvent> findById(Integer id);

  void insert(PaymentEvent paymentEvent);

  void update(PaymentEvent paymentEvent);
}
