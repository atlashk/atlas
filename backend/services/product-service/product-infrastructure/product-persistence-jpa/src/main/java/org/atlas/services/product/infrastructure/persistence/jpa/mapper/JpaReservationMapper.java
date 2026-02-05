package org.atlas.services.product.infrastructure.persistence.jpa.mapper;

import org.atlas.services.product.domain.entity.ReservationEntity;
import org.atlas.services.product.infrastructure.persistence.jpa.entity.JpaReservation;
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

  JpaReservation toJpaReservation(ReservationEntity reservation);

  ReservationEntity toReservation(JpaReservation jpaReservation);
}
