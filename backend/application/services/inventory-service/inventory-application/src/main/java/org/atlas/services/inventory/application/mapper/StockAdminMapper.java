package org.atlas.services.inventory.application.mapper;

import org.atlas.services.inventory.domain.entity.StockEntity;
import org.atlas.services.inventory.port.in.model.StockOutput;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StockAdminMapper {

  StockAdminMapper INSTANCE = Mappers.getMapper(StockAdminMapper.class);
  
  StockOutput toProductStockOutput(StockEntity stock);
}
