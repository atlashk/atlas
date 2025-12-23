package org.atlas.infrastructure.persistence.jpa.adapter.product;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.application.product.port.repository.ReservationRepository;
import org.atlas.domain.product.entity.Reservation;
import org.atlas.infrastructure.persistence.jpa.adapter.product.entity.JpaReservation;
import org.atlas.infrastructure.persistence.jpa.adapter.product.mapper.JpaReservationMapper;
import org.atlas.infrastructure.persistence.jpa.adapter.product.repository.JpaReservationRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaReservationRepositoryAdapter implements ReservationRepository {

  private final JpaReservationRepository jpaReservationRepository;

  @Override
  public Optional<Reservation> findByOrderIdAndProductId(Integer orderId, Integer productId) {
    return jpaReservationRepository.findByOrderIdAndProductId(orderId, productId)
        .map(JpaReservationMapper.INSTANCE::toReservation);
  }

  @Override
  public void insert(Reservation reservation) {
    JpaReservation jpaReservation = JpaReservationMapper.INSTANCE.toJpaReservation(reservation);
    jpaReservationRepository.insert(jpaReservation);
    reservation.setId(jpaReservation.getId());
  }

  @Override
  public void delete(Reservation reservation) {
    jpaReservationRepository.deleteById(reservation.getId());
  }
}
