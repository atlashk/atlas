package org.atlas.infrastructure.persistence.jpa.adapter.payment.repository;

import java.util.Optional;
import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepository;
import org.atlas.infrastructure.persistence.jpa.adapter.payment.entity.JpaPayment;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPaymentRepository extends JpaBaseRepository<JpaPayment, Integer> {

  Optional<JpaPayment> findByOrderId(Integer orderId);
}
