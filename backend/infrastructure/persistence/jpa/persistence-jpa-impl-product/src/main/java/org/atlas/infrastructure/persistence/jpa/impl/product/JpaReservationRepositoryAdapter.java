package org.atlas.infrastructure.persistence.jpa.impl.product;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.Reservation;
import org.atlas.domain.product.repository.ReservationRepository;
import org.atlas.infrastructure.persistence.jpa.impl.product.entity.JpaReservation;
import org.atlas.infrastructure.persistence.jpa.impl.product.mapper.JpaReservationMapper;
import org.atlas.infrastructure.persistence.jpa.impl.product.repository.JpaReservationRepository;
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
