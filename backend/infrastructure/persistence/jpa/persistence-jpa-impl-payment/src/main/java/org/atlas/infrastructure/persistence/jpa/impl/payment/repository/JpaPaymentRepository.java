package org.atlas.infrastructure.persistence.jpa.impl.payment.repository;

import java.util.List;
import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepository;
import org.atlas.infrastructure.persistence.jpa.impl.payment.entity.JpaPaymentEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPaymentRepository extends JpaBaseRepository<JpaPaymentEntity, Integer> {

  List<JpaPaymentEntity> findByOrderIdIn(List<Integer> orderIds);
}
