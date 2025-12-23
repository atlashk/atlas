package org.atlas.infrastructure.persistence.jpa.adapter.payment.repository;

import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepository;
import org.atlas.infrastructure.persistence.jpa.adapter.payment.entity.JpaPaymentEvent;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPaymentEventRepository extends JpaBaseRepository<JpaPaymentEvent, Integer> {

}
