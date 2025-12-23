package org.atlas.application.product.port.repository;

import java.util.Optional;
import org.atlas.domain.product.entity.Reservation;

public interface ReservationRepository {

  Optional<Reservation> findByOrderIdAndProductId(Integer orderId, Integer productId);

  void insert(Reservation reservation);

  void delete(Reservation reservation);
}
