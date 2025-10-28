package org.atlas.infrastructure.persistence.jpa.impl.product.repository;

import java.util.Optional;
import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepository;
import org.atlas.infrastructure.persistence.jpa.impl.product.entity.JpaReservation;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaReservationRepository extends JpaBaseRepository<JpaReservation, Integer> {

  Optional<JpaReservation> findByOrderIdAndProductId(Integer orderId, Integer productId);
}
