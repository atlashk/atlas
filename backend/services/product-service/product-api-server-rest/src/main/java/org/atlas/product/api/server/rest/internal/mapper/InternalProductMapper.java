package org.atlas.product.api.server.rest.internal.mapper;

import org.atlas.product.application.internal.model.InternalRetrieveProductListInput;
import org.atlas.product.domain.entity.Product;
import org.atlas.common.framework.internalapi.product.model.ProductResponse;
import org.atlas.product.api.server.rest.internal.model.InternalRetrieveProductListRequest;
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
