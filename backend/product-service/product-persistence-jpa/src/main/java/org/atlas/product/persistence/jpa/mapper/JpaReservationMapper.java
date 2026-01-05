package org.atlas.product.persistence.jpa.mapper;

import org.atlas.product.domain.entity.Reservation;
import org.atlas.product.persistence.jpa.entity.JpaReservation;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JpaReservationMapper {

  JpaReservationMapper INSTANCE = Mappers.getMapper(JpaReservationMapper.class);

  JpaReservation toJpaReservation(Reservation reservation);

  Reservation toReservation(JpaReservation jpaReservation);
}
