package org.atlas.services.payment.port.out.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.services.payment.domain.entity.PaymentGatewayEntity;

public interface PaymentGatewayRepository {

  List<PaymentGatewayEntity> findAll();

  Optional<PaymentGatewayEntity> findById(Integer id);

  Optional<PaymentGatewayEntity> findByCode(String code);
}
