package org.atlas.infrastructure.api.server.rest.adapter.product.internal.mapper;

import org.atlas.application.product.internal.model.InternalRetrieveProductListInput;
import org.atlas.domain.product.entity.Product;
import org.atlas.framework.internalapi.product.model.ProductResponse;
import org.atlas.infrastructure.api.server.rest.adapter.product.internal.model.InternalRetrieveProductListRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InternalProductMapper {

  InternalProductMapper INSTANCE = Mappers.getMapper(InternalProductMapper.class);

  InternalRetrieveProductListInput toInternalRetrieveProductListInput(
      InternalRetrieveProductListRequest request);

  ProductResponse toProductResponse(Product product);
}
