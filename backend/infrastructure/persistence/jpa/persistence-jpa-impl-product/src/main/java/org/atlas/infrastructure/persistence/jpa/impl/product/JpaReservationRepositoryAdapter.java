package org.atlas.infrastructure.persistence.jpa.impl.product;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.ReservationEntity;
import org.atlas.domain.product.repository.ReservationRepository;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.infrastructure.persistence.jpa.impl.product.entity.JpaReservationEntity;
import org.atlas.infrastructure.persistence.jpa.impl.product.repository.JpaReservationRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaReservationRepositoryAdapter implements ReservationRepository {

  private final JpaReservationRepository jpaReservationRepository;

  @Override
  public Optional<ReservationEntity> findByOrderIdAndProductId(Integer orderId, Integer productId) {
    return jpaReservationRepository.findByOrderIdAndProductId(orderId, productId)
        .map(jpaReservation -> ObjectMapperUtil.getInstance()
            .map(jpaReservation, ReservationEntity.class));
  }

  @Override
  public void insert(ReservationEntity reservation) {
    JpaReservationEntity jpaReservation = ObjectMapperUtil.getInstance()
        .map(reservation, JpaReservationEntity.class);
    jpaReservationRepository.insert(jpaReservation);
    reservation.setId(jpaReservation.getId());
  }

  @Override
  public void delete(ReservationEntity reservation) {
    jpaReservationRepository.deleteById(reservation.getId());
  }
}
