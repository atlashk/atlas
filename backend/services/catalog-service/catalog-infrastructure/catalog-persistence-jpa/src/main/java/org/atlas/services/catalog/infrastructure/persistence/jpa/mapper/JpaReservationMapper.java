package org.atlas.services.catalog.infrastructure.persistence.jpa.mapper;

import org.atlas.services.product.domain.entity.ReservationEntity;
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

  JpaReservationEntity toJpaReservation(ReservationEntity reservation);

  ReservationEntity toReservation(JpaReservationEntity jpaReservation);
}
