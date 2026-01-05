package org.atlas.payment.persistence.jpa.repository;

import org.atlas.common.infrastructure.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.payment.persistence.jpa.entity.JpaPaymentEvent;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPaymentEventRepository extends JpaBaseRepository<JpaPaymentEvent, Integer> {

}
