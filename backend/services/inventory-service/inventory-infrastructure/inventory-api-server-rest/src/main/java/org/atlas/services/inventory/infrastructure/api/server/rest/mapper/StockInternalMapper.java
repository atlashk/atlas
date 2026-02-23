package org.atlas.services.inventory.infrastructure.api.server.rest.mapper;

import org.atlas.libs.framework.internal.product.model.ProductOutput;
import org.atlas.services.inventory.infrastructure.api.server.rest.model.internal.RetrieveStockListRequest;
import org.atlas.services.product.port.in.internal.model.InternalRetrieveProductListInput;
import org.atlas.services.inventory.domain.entity.StockEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StockInternalMapper {

  StockInternalMapper INSTANCE = Mappers.getMapper(StockInternalMapper.class);

  InternalRetrieveProductListInput toInternalRetrieveProductListInput(
      RetrieveStockListRequest request);

  ProductOutput toProductResponse(StockEntity product);
}
