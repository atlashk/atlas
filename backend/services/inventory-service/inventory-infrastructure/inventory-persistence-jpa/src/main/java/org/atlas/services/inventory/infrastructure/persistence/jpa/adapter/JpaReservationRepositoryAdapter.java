package org.atlas.services.inventory.infrastructure.persistence.jpa.adapter;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.services.inventory.infrastructure.persistence.jpa.entity.JpaReservationEntity;
import org.atlas.services.inventory.port.out.repository.ReservationRepository;
import org.atlas.services.inventory.domain.entity.ReservationEntity;
import org.atlas.services.inventory.infrastructure.persistence.jpa.mapper.JpaReservationMapper;
import org.atlas.services.inventory.infrastructure.persistence.jpa.repository.JpaReservationRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaReservationRepositoryAdapter implements ReservationRepository {

  private final JpaReservationRepository jpaReservationRepository;

  @Override
  public Optional<ReservationEntity> findByOrderIdAndProductId(String orderId, String productId) {
    return jpaReservationRepository.findByOrderIdAndProductId(orderId, productId)
        .map(JpaReservationMapper.INSTANCE::toReservation);
  }

  @Override
  public void insert(ReservationEntity reservation) {
    JpaReservationEntity jpaReservation = JpaReservationMapper.INSTANCE.toJpaReservation(reservation);
    jpaReservationRepository.insert(jpaReservation);
    reservation.setId(jpaReservation.getId());
  }

  @Override
  public void delete(ReservationEntity reservation) {
    jpaReservationRepository.deleteById(reservation.getId());
  }
}
