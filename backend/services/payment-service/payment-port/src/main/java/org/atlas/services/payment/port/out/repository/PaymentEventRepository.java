package org.atlas.services.payment.port.out.repository;

import java.util.Optional;
import org.atlas.services.payment.domain.entity.PaymentEventEntity;

public interface PaymentEventRepository {

  Optional<PaymentEventEntity> findById(Integer id);

  void insert(PaymentEventEntity paymentEvent);

  void update(PaymentEventEntity paymentEvent);
}
