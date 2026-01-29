package org.atlas.services.payment.persistence.jpa.repository;

import java.util.Optional;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.payment.persistence.jpa.entity.JpaPaymentGateway;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPaymentGatewayRepository extends JpaBaseRepository<JpaPaymentGateway, Integer> {

  Optional<JpaPaymentGateway> findByCode(String code);
}
