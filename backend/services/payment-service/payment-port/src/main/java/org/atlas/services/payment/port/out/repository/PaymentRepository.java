package org.atlas.services.payment.port.out.repository;

import java.util.Optional;
import org.atlas.services.payment.domain.entity.Payment;

public interface PaymentRepository {

  Optional<Payment> findById(Integer id);

  Optional<Payment> findByOrderId(Integer orderId);

  void insert(Payment payment);

  void update(Payment payment);
}
