package org.atlas.payment.persistence.jpa.repository;

import java.util.Optional;
import org.atlas.common.infrastructure.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.payment.persistence.jpa.entity.JpaPaymentGateway;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPaymentGatewayRepository extends JpaBaseRepository<JpaPaymentGateway, Integer> {

  Optional<JpaPaymentGateway> findByCode(String code);
}
