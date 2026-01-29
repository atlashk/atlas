package org.atlas.services.product.persistence.jpa.mapper;

import org.atlas.services.product.domain.entity.Reservation;
import org.atlas.services.product.persistence.jpa.entity.JpaReservation;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface JpaReservationMapper {

  JpaReservationMapper INSTANCE = Mappers.getMapper(JpaReservationMapper.class);

  JpaReservation toJpaReservation(Reservation reservation);

  Reservation toReservation(JpaReservation jpaReservation);
}
