package org.atlas.services.product.infrastructure.persistence.jpa.repository;

import java.util.Optional;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.product.infrastructure.persistence.jpa.entity.JpaReservation;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaReservationRepository extends JpaBaseRepository<JpaReservation, Integer> {

  Optional<JpaReservation> findByOrderIdAndProductId(String orderId, String productId);
}
