package org.atlas.domain.payment.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.domain.payment.entity.PaymentEntity;

public interface PaymentRepository {

  List<PaymentEntity> findByOrderIdIn(List<Integer> orderIds);

  Optional<PaymentEntity> findById(Integer id);

  Optional<PaymentEntity> findBySagaId(Integer sagaId);

  void insert(PaymentEntity payment);

  void update(PaymentEntity payment);
}
