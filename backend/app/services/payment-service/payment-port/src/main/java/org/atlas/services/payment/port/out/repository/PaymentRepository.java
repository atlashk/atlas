package org.atlas.services.payment.port.out.repository;

import java.util.Optional;
import org.atlas.services.payment.domain.entity.Payment;

public interface PaymentRepository {

  Optional<Payment> findById(String id);

  Optional<Payment> findByOrderId(String orderId);

  void insert(Payment payment);

  void update(Payment payment);
}
