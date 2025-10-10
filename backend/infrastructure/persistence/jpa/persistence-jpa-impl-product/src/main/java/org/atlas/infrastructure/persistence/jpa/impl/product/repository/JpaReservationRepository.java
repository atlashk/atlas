package org.atlas.infrastructure.persistence.jpa.impl.product.repository;

import java.util.Optional;
import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepository;
import org.atlas.infrastructure.persistence.jpa.impl.product.entity.JpaReservationEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaReservationRepository extends JpaBaseRepository<JpaReservationEntity, Integer> {

  Optional<JpaReservationEntity> findByOrderIdAndProductId(Integer orderId, Integer productId);
}
