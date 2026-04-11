package org.atlas.services.inventory.port.out.repository;

import java.util.Optional;
import org.atlas.services.inventory.domain.entity.Reservation;
import org.atlas.services.inventory.domain.entity.ReservationStatus;

public interface ReservationRepository {

  Optional<Reservation> findByOrderIdAndProductId(String orderId, String productId);

  void insert(Reservation reservation);

  void update(Reservation reservation);
  
  void updateStatus(String orderId, ReservationStatus status);
}
