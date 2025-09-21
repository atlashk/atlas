package org.atlas.domain.payment.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.domain.payment.entity.PaymentEntity;

public interface PaymentRepository {

  List<PaymentEntity> findByIdIn(List<Integer> ids);

  Optional<PaymentEntity> findById(Integer id);

  void insert(PaymentEntity paymentEntity);

  void update(PaymentEntity paymentEntity);
}
