package org.atlas.domain.payment.repository;

import java.util.Optional;
import org.atlas.domain.payment.entity.PaymentEntity;

public interface PaymentRepository {

  Optional<PaymentEntity> findById(Integer id);

  void save(PaymentEntity paymentEntity);
}
