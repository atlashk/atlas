package org.atlas.services.product.persistence.jpa.adapter;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.services.product.application.port.repository.ReservationRepository;
import org.atlas.services.product.domain.entity.Reservation;
import org.atlas.services.product.persistence.jpa.entity.JpaReservation;
import org.atlas.services.product.persistence.jpa.mapper.JpaReservationMapper;
import org.atlas.services.product.persistence.jpa.repository.JpaReservationRepository;
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
