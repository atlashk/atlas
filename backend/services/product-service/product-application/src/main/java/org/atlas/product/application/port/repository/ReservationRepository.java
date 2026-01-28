package org.atlas.product.application.port.repository;

import java.util.Optional;
import org.atlas.product.domain.entity.Reservation;

public interface ReservationRepository {

  Optional<Reservation> findByOrderIdAndProductId(Integer orderId, Integer productId);

  void insert(Reservation reservation);

  void delete(Reservation reservation);
}
