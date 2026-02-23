package org.atlas.services.inventory.infrastructure.persistence.jpa.repository;

import java.util.Optional;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.inventory.infrastructure.persistence.jpa.entity.JpaReservationEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaReservationRepository extends JpaBaseRepository<JpaReservationEntity, Integer> {

  Optional<JpaReservationEntity> findByOrderIdAndProductId(String orderId, String productId);
}
