package org.atlas.application.payment.port.repository;

import java.util.Optional;
import org.atlas.domain.payment.entity.Payment;

public interface PaymentRepository {

  Optional<Payment> findById(Integer id);

  Optional<Payment> findByOrderId(Integer orderId);

  void insert(Payment payment);

  void update(Payment payment);
}
