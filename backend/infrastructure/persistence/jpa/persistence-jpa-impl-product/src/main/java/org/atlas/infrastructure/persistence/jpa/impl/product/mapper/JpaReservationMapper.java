package org.atlas.infrastructure.persistence.jpa.impl.product.mapper;

import org.atlas.domain.product.entity.Reservation;
import org.atlas.infrastructure.persistence.jpa.impl.product.entity.JpaReservation;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(builder = @Builder(disableBuilder = true))
public interface JpaReservationMapper {

  JpaReservationMapper INSTANCE = Mappers.getMapper(JpaReservationMapper.class);

  JpaReservation toJpaReservation(Reservation reservation);

  Reservation toReservation(JpaReservation jpaReservation);
}
