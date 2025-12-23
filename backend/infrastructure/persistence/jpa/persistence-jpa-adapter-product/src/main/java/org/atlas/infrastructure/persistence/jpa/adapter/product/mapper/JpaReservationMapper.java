package org.atlas.infrastructure.persistence.jpa.adapter.product.mapper;

import org.atlas.domain.product.entity.Reservation;
import org.atlas.infrastructure.persistence.jpa.adapter.product.entity.JpaReservation;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JpaReservationMapper {

  JpaReservationMapper INSTANCE = Mappers.getMapper(JpaReservationMapper.class);

  JpaReservation toJpaReservation(Reservation reservation);

  Reservation toReservation(JpaReservation jpaReservation);
}
