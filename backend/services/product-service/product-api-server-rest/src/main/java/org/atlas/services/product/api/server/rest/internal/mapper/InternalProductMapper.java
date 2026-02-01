package org.atlas.services.product.api.server.rest.internal.mapper;

import org.atlas.libs.framework.internalapi.catalog.model.ProductResponse;
import org.atlas.services.product.api.server.rest.internal.model.InternalRetrieveProductListRequest;
import org.atlas.services.product.application.internal.model.InternalRetrieveProductListInput;
import org.atlas.services.product.domain.entity.Product;
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
