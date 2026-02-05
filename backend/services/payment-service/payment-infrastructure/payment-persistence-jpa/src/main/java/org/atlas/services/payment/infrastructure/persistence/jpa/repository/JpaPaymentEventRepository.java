package org.atlas.services.payment.infrastructure.persistence.jpa.repository;

import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.payment.infrastructure.persistence.jpa.entity.JpaPaymentEventEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPaymentEventRepository extends JpaBaseRepository<JpaPaymentEventEntity, Integer> {

}
