package org.atlas.services.inventory.infrastructure.api.server.rest.mapper;

import org.atlas.services.inventory.infrastructure.api.server.rest.model.RetrieveStockResponse;
import org.atlas.services.inventory.port.in.model.StockOutput;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StockAdminMapper {

  StockAdminMapper INSTANCE = Mappers.getMapper(StockAdminMapper.class);

  // Output -> Response
  // -----------------------------------------------------------------------------------------------

  RetrieveStockResponse toRetrieveStockResponse(StockOutput output);
}

