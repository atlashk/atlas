package org.atlas.services.catalog.application.product.mapper;

import org.atlas.libs.framework.internal.product.model.ProductOutput;
import org.atlas.services.catalog.domain.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductInternalMapper {

  ProductInternalMapper INSTANCE = Mappers.getMapper(ProductInternalMapper.class);

  // Entity --> Output
  // -----------------------------------------------------------------------------------------------

  ProductOutput toProductOutput(ProductEntity product);
}
