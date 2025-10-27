package org.atlas.domain.payment.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.domain.payment.entity.PaymentGatewayEntity;

public interface PaymentGatewayRepository {

  List<PaymentGatewayEntity> findAll();

  Optional<PaymentGatewayEntity> findById(String id);

  Optional<PaymentGatewayEntity> findByCode(String code);
}
