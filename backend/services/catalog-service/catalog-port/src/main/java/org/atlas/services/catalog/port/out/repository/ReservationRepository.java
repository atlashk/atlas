package org.atlas.services.catalog.port.out.repository;

import java.util.Optional;

public interface ReservationRepository {

  Optional<ReservationEntity> findByOrderIdAndProductId(String orderId, String productId);

  void insert(ReservationEntity reservation);

  void delete(ReservationEntity reservation);
}
