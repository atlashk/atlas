package org.atlas.services.payment.infrastructure.persistence.jpa.repository;

import java.util.Optional;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.payment.infrastructure.persistence.jpa.entity.JpaPaymentGatewayEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPaymentGatewayRepository extends JpaBaseRepository<JpaPaymentGatewayEntity, Integer> {

  Optional<JpaPaymentGatewayEntity> findByCode(String code);
}
