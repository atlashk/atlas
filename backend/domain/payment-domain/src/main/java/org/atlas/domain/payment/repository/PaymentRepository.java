package org.atlas.domain.payment.repository;

import org.atlas.domain.payment.entity.PaymentEntity;

public interface PaymentRepository {

  void save(PaymentEntity paymentEntity);
}
