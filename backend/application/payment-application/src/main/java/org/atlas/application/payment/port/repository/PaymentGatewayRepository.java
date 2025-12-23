package org.atlas.application.payment.port.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.domain.payment.entity.PaymentGateway;

public interface PaymentGatewayRepository {

  List<PaymentGateway> findAll();

  Optional<PaymentGateway> findById(Integer id);

  Optional<PaymentGateway> findByCode(String code);
}
