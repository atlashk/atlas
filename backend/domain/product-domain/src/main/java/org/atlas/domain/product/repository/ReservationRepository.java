package org.atlas.domain.product.repository;

import java.util.Optional;
import org.atlas.domain.product.entity.ReservationEntity;

public interface ReservationRepository {

  Optional<ReservationEntity> findByOrderIdAndProductId(Integer orderId, Integer productId);

  void insert(ReservationEntity reservationEntity);

  void delete(ReservationEntity reservationEntity);
}
