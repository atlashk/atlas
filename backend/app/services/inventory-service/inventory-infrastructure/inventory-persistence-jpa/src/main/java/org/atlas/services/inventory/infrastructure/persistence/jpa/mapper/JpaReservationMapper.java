package org.atlas.services.inventory.infrastructure.persistence.jpa.mapper;

import org.atlas.services.inventory.domain.entity.Reservation;
import org.atlas.services.inventory.infrastructure.persistence.jpa.entity.JpaReservationEntity;
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

  JpaReservationEntity toJpaReservation(Reservation reservation);

  Reservation toReservation(JpaReservationEntity jpaReservation);
}
