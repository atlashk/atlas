package org.atlas.services.catalog.api.rest.product.mapper;

import org.atlas.services.catalog.domain.entity.ProductEntity;
import org.atlas.services.catalog.api.rest.product.model.ProductResponse;
import org.atlas.services.catalog.api.rest.product.model.RetrieveProductListRequest;
import org.atlas.services.catalog.port.in.product.model.RetrieveProductListInput;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

  ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

  // Request --> Entity
  // -----------------------------------------------------------------------------------------------

  RetrieveProductListInput toRetrieveProductListInput(RetrieveProductListRequest request);

  // Entity/Output --> Response
  // -----------------------------------------------------------------------------------------------

  ProductResponse toProductResponse(ProductEntity product);
}
