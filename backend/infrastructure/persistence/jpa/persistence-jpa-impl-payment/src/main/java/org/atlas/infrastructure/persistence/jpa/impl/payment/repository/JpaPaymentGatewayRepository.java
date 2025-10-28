package org.atlas.infrastructure.persistence.jpa.impl.payment.repository;

import java.util.Optional;
import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepository;
import org.atlas.infrastructure.persistence.jpa.impl.payment.entity.JpaPaymentGateway;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPaymentGatewayRepository extends JpaBaseRepository<JpaPaymentGateway, Integer> {

  Optional<JpaPaymentGateway> findByCode(String code);
}
