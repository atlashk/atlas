package org.atlas.services.payment.application.port.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.services.payment.domain.entity.PaymentGateway;

public interface PaymentGatewayRepository {

  List<PaymentGateway> findAll();

  Optional<PaymentGateway> findById(Integer id);

  Optional<PaymentGateway> findByCode(String code);
}
