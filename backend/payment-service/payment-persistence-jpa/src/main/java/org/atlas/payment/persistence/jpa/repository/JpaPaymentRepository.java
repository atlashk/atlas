package org.atlas.payment.persistence.jpa.repository;

import java.util.Optional;
import org.atlas.common.infrastructure.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.payment.persistence.jpa.entity.JpaPayment;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPaymentRepository extends JpaBaseRepository<JpaPayment, Integer> {

  Optional<JpaPayment> findByOrderId(Integer orderId);
}
