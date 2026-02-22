package org.atlas.services.product.port.out.repository;

import java.util.Optional;
import org.atlas.services.product.domain.entity.ReservationEntity;

public interface ReservationRepository {

  Optional<ReservationEntity> findByOrderIdAndProductId(String orderId, String productId);

  void insert(ReservationEntity reservation);

  void delete(ReservationEntity reservation);
}
