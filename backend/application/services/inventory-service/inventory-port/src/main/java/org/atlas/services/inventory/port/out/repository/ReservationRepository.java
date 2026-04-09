package org.atlas.services.inventory.port.out.repository;

import java.util.Optional;
import org.atlas.services.inventory.domain.entity.ReservationEntity;
import org.atlas.services.inventory.domain.entity.ReservationStatus;

public interface ReservationRepository {

  Optional<ReservationEntity> findByOrderIdAndProductId(String orderId, String productId);

  void insert(ReservationEntity reservation);

  void update(ReservationEntity reservation);
  
  void updateStatus(String orderId, ReservationStatus status);
}
