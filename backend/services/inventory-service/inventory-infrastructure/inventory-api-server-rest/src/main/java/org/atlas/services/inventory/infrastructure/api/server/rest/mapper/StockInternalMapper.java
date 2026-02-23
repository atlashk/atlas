package org.atlas.services.inventory.infrastructure.api.server.rest.mapper;

import org.atlas.libs.framework.internal.inventory.model.RetrieveStockListInput;
import org.atlas.services.inventory.infrastructure.api.server.rest.model.internal.RetrieveStockListRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StockInternalMapper {

  StockInternalMapper INSTANCE = Mappers.getMapper(StockInternalMapper.class);

  // Request --> Input
  // -----------------------------------------------------------------------------------------------

  RetrieveStockListInput toRetrieveStockListInput(RetrieveStockListRequest request);
}
