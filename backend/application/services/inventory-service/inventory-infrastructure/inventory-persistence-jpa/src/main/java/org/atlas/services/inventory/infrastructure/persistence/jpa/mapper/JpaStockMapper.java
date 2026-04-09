package org.atlas.services.inventory.infrastructure.persistence.jpa.mapper;

import org.atlas.services.inventory.domain.entity.StockEntity;
import org.atlas.services.inventory.infrastructure.persistence.jpa.entity.JpaOptimisticStockEntity;
import org.atlas.services.inventory.infrastructure.persistence.jpa.entity.JpaStockEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface JpaStockMapper {

  JpaStockMapper INSTANCE = Mappers.getMapper(JpaStockMapper.class);

  // JPA entity --> Domain entity
  // -----------------------------------------------------------------------------------------------

  StockEntity toStock(JpaStockEntity jpaStock);

  StockEntity toStock(JpaOptimisticStockEntity jpaOptimisticStock);

  // Domain entity --> JPA entity
  // -----------------------------------------------------------------------------------------------

  JpaStockEntity toJpaStock(StockEntity stock);

  void merge(StockEntity stock, @MappingTarget JpaStockEntity jpaStock);
}
