package org.atlas.infrastructure.api.server.rest.impl.product.internal.mapper;

import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.usecase.internal.model.InternalListProductInput;
import org.atlas.framework.internalapi.product.model.ProductResponse;
import org.atlas.infrastructure.api.server.rest.impl.product.internal.model.InternalListProductRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InternalProductMapper {

  InternalProductMapper INSTANCE = Mappers.getMapper(InternalProductMapper.class);

  InternalListProductInput toInternalListProductInput(InternalListProductRequest request);

  ProductResponse toProductResponse(Product product);
}
