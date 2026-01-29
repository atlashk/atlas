package org.atlas.services.payment.persistence.jpa.repository;

import java.util.Optional;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.payment.persistence.jpa.entity.JpaPayment;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPaymentRepository extends JpaBaseRepository<JpaPayment, Integer> {

  Optional<JpaPayment> findByOrderId(Integer orderId);
}
