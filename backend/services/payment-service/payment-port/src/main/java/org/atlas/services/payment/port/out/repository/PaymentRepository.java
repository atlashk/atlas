package org.atlas.services.payment.port.out.repository;

import java.util.Optional;
import org.atlas.services.payment.domain.entity.PaymentEntity;

public interface PaymentRepository {

  Optional<PaymentEntity> findById(String id);

  Optional<PaymentEntity> findByOrderId(String orderId);

  void insert(PaymentEntity payment);

  void update(PaymentEntity payment);
}
