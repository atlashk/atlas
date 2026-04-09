package org.atlas.services.payment.infrastructure.persistence.jpa.repository;

import java.util.Optional;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.payment.infrastructure.persistence.jpa.entity.JpaPaymentEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPaymentRepository extends JpaBaseRepository<JpaPaymentEntity, String> {

  Optional<JpaPaymentEntity> findByOrderId(String orderId);
}
